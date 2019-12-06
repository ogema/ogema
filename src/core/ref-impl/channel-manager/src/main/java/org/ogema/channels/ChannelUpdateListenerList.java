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

import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.slf4j.Logger;

// ChannelUpdateListener
class ChannelUpdateListenerList implements ChannelUpdateListener {

	final private Logger logger;
	
	final private Driver driver;
	
	final private LogLimiter logLimiter;
	
	/** map of registered listening channels */
	private final Map<ChannelLocator, Channel> listenChannels = new ConcurrentHashMap<ChannelLocator, Channel>();
	
	/** container passed to the driver to register the listening channels */
	private List<SampledValueContainer> containers = new ArrayList<SampledValueContainer>();

	ChannelUpdateListenerList(Driver driver, Logger logger) {
		this.driver = driver;
		this.logger = logger;
		logLimiter = new LogLimiter(logger);
	}
	
	@Override
	public synchronized void channelsUpdated(List<SampledValueContainer> channels) {
		for (int i = 0; i < channels.size(); i++) {
			SampledValueContainer svc = channels.get(i);
			Channel channel = listenChannels.get(svc.getChannelLocator());

			if (channel == null)
				logger.debug("received unrequested channel {} in channelsUpdated.", svc.getChannelLocator());
			else
				channel.update(svc);
		}
	}

	@Override
	public void exceptionOccured(Exception e) {
		
		if (logLimiter.check())
			logger.warn("exception in listen channel for driver {}", driver.getId(), e);
	}

	synchronized void addListenChannel(Channel channel) throws ChannelAccessException {
		ChannelDriver driver = this.driver.getDriver();
		List<SampledValueContainer> list;
		
		if (driver != null) {
			// be cautious here. Add the new channel to a local list. 
			// If this fails, install old (hopefully working) list
			
			list = createNewList(channel.getChannelLocator());
			
			try {
				driver.listenChannels(list, this);
				
				// this was ok, now add the new channel
				containers = list;
				listenChannels.put(channel.getChannelLocator(), channel);
				
			} catch (Exception e) {
				// try installing previous list
				try {
					driver.listenChannels(containers, this);
				} catch (Exception ex) {
					logger.warn(
							"Exception while restoring listen channels for driver {}",
							driver.getDriverId(), e);
				}
				
				throw new ChannelAccessException("could not add listen channel " + channel.getChannelLocator(), e);
			}
		}
	}
	
	synchronized void removeListenChannel(Channel channel) {
		ChannelDriver driver = this.driver.getDriver();

		listenChannels.remove(channel.getChannelLocator());
		containers = createNewList(null);

		if (driver != null) {
			try {
				driver.listenChannels(containers, this);
			} catch (Exception e) {
				logger.warn(
						"Exception while removing listen channel for channel {}",
						channel.getChannelLocator(), e);
			}
		}

	}
	
	private List<SampledValueContainer> createNewList(ChannelLocator newElement) {
		
		List<SampledValueContainer> list = new ArrayList<SampledValueContainer>();
		
		// first add preliminary channel
		if (newElement != null)
			list.add(new SampledValueContainer(newElement));
		
		// now add existing list
		for (Channel channel : listenChannels.values()) {
			list.add(new SampledValueContainer(channel.getChannelLocator()));
		}
		
		return list;
	}
	
	synchronized void close() {
		try {
			containers.clear();
			driver.getDriver().listenChannels(containers, null);
		} catch (UnsupportedOperationException e) {
			// ignore silently
		} catch (Exception e) {
			logger.warn(
					"Exception while removing all listen channels for driver {}", driver.getId(), e);
		}
		
		listenChannels.clear();
		
		logger.debug("closed ChannelUpdateListenerList");
	}
}