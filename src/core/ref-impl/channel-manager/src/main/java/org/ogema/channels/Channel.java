/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.ogema.channels.Configuration.Type;
import org.ogema.core.application.AppID;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.slf4j.LoggerFactory;

class Channel {

	private boolean closed;
	
	private final Driver driver;
	
	private final ChannelLocator channelLocator;
	
	private final Map<ChannelConfigurationImpl, Configuration> configurations = new ConcurrentHashMap<ChannelConfigurationImpl, Configuration>();
	
	private AtomicReference<SampledValue> value = new AtomicReference<SampledValue>();

	private ReaderThread readerThread;
	private boolean isListening;

	/** Re-usable List for wrapping SampledValue for one-time channel read */
	private List<SampledValueContainer> readList;

	/** Re-usable List for wrapping Value for channel write */
	private List<ValueContainer> writeList;
	
	Channel(Driver driver, ChannelLocator channelLocator) {
		
		// constructor has no global side-effects
		
		this.driver = driver;
		this.channelLocator = channelLocator;
		
		// set default channel value
		value.set(new SampledValue(null, System.currentTimeMillis(), Quality.BAD));
		
		readList = new ArrayList<SampledValueContainer>(1);
		readList.add(new SampledValueContainer(channelLocator));
		
		writeList = new ArrayList<ValueContainer>(1);
		writeList.add(new ValueContainer(channelLocator, null));
	}

	synchronized void close() {
		closed = true;
		
		configurations.clear();
		
		if (readerThread != null) {
			readerThread.remove(this);
			readerThread = null;
		}
		
		if (isListening) {
			driver.removeListenChannel(this);
			isListening = false;
		}
		
		driver.channelClosed(this);
	}
	
	SampledValue getStoredValue() {
		return value.get();
	}
	
	ChannelLocator getChannelLocator() {
		return channelLocator;
	}

	synchronized ChannelConfiguration addConfiguration(ChannelConfigurationImpl channelConfiguration)
			throws ChannelAccessException {
		try {

			if (closed)
				throw new IllegalStateException("Channel '" + channelLocator + "' has been closed");
			
			// first added configuration
			if (configurations.isEmpty()) {
				driver.channelAdded(channelLocator);
			}
			
			Configuration configuration = configurations.get(channelConfiguration);

			if (configuration == null) {
				configuration = new Configuration(this, channelConfiguration);

				// previously no readerThread
				if (configuration.getType() == Type.POLLED && readerThread == null) {
					readerThread = driver.getReaderThread(this);
				}

				// is this channel already added to the list of listen channels?
				else if (configuration.getType() == Type.LISTEN && !isListening) {
					driver.addListenChannel(this);
					isListening = true;
				}

				configurations.put(channelConfiguration, configuration);

				// update the reader thread waittime after adding the new
				// configuration,
				// because otherwise it might check the wait times before the
				// configuration was added
				if (configuration.getType() == Type.POLLED) {
					readerThread.update();
				}
			}

			return configuration.getConfiguration();
		} finally {
			if (configurations.isEmpty()) {
				try {
					close();
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).warn("Failed to close channel after unsuccessful add operation",e);
				}
			}
		}
	}

	synchronized boolean removeConfiguration(ChannelConfiguration configuration) {

		boolean success = false;

		if (closed)
			return false;

		try {
			if (configurations.remove(configuration) != null)
				success = true;

			// check if we still have polled configurations
			if (readerThread != null) {
				boolean needsReaderThread = false;

				for (Configuration c : configurations.values()) {
					if (c.getType() == Type.POLLED) {
						needsReaderThread = true;
						break;
					}
				}

				// don't need to update the reader thread here.
				// If we removed the configuration with the shortest waittime
				// the readerThread now wakes up too early, finds nothing to do
				// and goes to sleep again.

				if (!needsReaderThread) {
					readerThread.remove(this);
					readerThread = null;
				}
			}

			// check if we still need to listen
			if (isListening) {
				boolean needsListening = false;

				for (Configuration c : configurations.values()) {
					if (c.getType() == Type.LISTEN) {
						needsListening = true;
						break;
					}
				}

				if (!needsListening) {
					driver.removeListenChannel(this);
					isListening = false;
				}
			}
		} finally {
			if (configurations.isEmpty()) {
				close();
			}
		}
		
		return success;
	}

	/**
	 * One-time single read of channel.
	 * 
	 * @throws ChannelAccessException
	 */
	void readChannel() throws ChannelAccessException {
		synchronized (readList) {
			readList.get(0).setSampledValue(null);
			
			try {
				driver.readChannels(readList);
			} catch (Exception e) {
				throw new ChannelAccessException("read failed: " + channelLocator, e);
			}
			
			update(readList.get(0));
		}
	}

	/**
	 * One-time single write of channel.
	 * 
	 * @param value
	 * @throws ChannelAccessException
	 */
	void writeChannel(Value value) throws ChannelAccessException {
		synchronized (writeList) {
			writeList.get(0).setValue(value);

			try {	
				driver.writeChannels(writeList);
			} catch (Exception e) {
				throw new ChannelAccessException("write failed: " + channelLocator, e);
			}
		}
	}

	Configuration getConfiguration(ChannelConfiguration configuration) {
		return configurations.get(configuration);
	}
	
	void update(SampledValueContainer svc) {
		EventType type;
		SampledValue newValue;
		SampledValue oldValue;
		
		// no input -> keep old value
		if (svc == null)
			return;
		
		newValue = svc.getSampledValue();
		
		// if there is no new value -> just keep old value
		if (newValue == null)
			return;

		// set new value
		oldValue = value.getAndSet(newValue);
		
		// if there wasn't an old value -> VALUE_CHANGED
		if (oldValue == null) {
			type = EventType.VALUE_CHANGED;
		// an exception for the default startup value -> VALUE_CHANGED
		} else if (oldValue.getValue() == null 
				&& newValue.getValue() != null 
				&& newValue.getQuality() == Quality.GOOD) {
			type = EventType.VALUE_CHANGED;
		// if the quality changed -> QUALITY_CHANGED
		} else if (oldValue.getQuality() == null ? newValue.getQuality() != null : 
				!oldValue.getQuality().equals(newValue.getQuality())) {
			type = EventType.QUALITY_CHANGED;
			
		// if the value changed -> VALUE_CHANGED
		} else if (oldValue.getValue() == null ? newValue.getValue() != null : 
				!oldValue.getValue().equals(newValue.getValue())) {
			type = EventType.VALUE_CHANGED;
		
		// if only the timestamp changed -> UPDATED
		} else {
			type = EventType.UPDATED;
		}
		
		// call listeners
		for (Configuration configuration : configurations.values()) {
			configuration.update(svc, type); // TODO check for null values!
		}
	}
	
	long updateSamplingTime(long currentTime, List<SampledValueContainer> containers) {
		
		long result = Long.MAX_VALUE;
		long waittime;
		
		for(Configuration configuration : configurations.values()) {
			waittime = configuration.updateSamplingTime(currentTime, containers);
			
			if (waittime < result)
				result = waittime;
		}
		
		return result;
	}

	synchronized void removeAppID(AppID appID) {
		List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
		
		// collect all matching configurations into a list
		for (Configuration current : configurations.values()) {
			if (current.getAppID().equals(appID)) {
				list.add(current.getConfiguration());
			}
		}
		
		// remove all matching configurations
		for (ChannelConfiguration configuration : list) {
			removeConfiguration(configuration);
		}
	}
}
