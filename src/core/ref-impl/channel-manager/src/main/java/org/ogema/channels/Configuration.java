/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.channels;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This class represents an active channel as used by the apps. 
 * For every opened channel a configuration instance is created.
 * This is separate from Channel, because a channel might be opened twice by different apps.
 *  
 * @author pau
 *
 */
class Configuration {

	/** type of configuration. Determined by the sampling interval. */
	enum Type {
		ONDEMAND, // configuration updated on demand
		LISTEN, // listen channel
		POLLED // polled by reader thread
	}
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final LogLimiter logLimiter = new LogLimiter(logger);
	
	/** the absolute time (in millis) of the next sampling of this channel */
	private long nextSamplingTime = 0;

	private final ChannelConfigurationImpl configuration;
	private final Channel channel;
	private final Type type;
	private final boolean read;
	private final boolean write;

	private List<ChannelEventListener> updateListeners = new ArrayList<ChannelEventListener>();
	private List<ChannelEventListener> changedListeners = new ArrayList<ChannelEventListener>();
	
	private List<SampledValueContainer> svcList = new ArrayList<SampledValueContainer>();
	
	// the right to access the channel was checked in ChannelManagerImpl.addChannel
	Configuration(Channel channel, ChannelConfigurationImpl configuration) {
		long samplingPeriod;
		
		this.configuration = configuration;
		this.channel = channel;
		
		read = this.configuration.getDirection() == Direction.DIRECTION_INOUT 
				|| this.configuration.getDirection() == Direction.DIRECTION_INPUT;
		
		write = this.configuration.getDirection() == Direction.DIRECTION_INOUT 
				|| this.configuration.getDirection() == Direction.DIRECTION_OUTPUT;
		
		samplingPeriod = this.configuration.getSamplingPeriod();
		
		// output channels are of type ONDEMAND only!
		if (!read)
			this.type = Type.ONDEMAND;
		// what kind of configuration is this?
		else if (samplingPeriod == ChannelConfiguration.LISTEN_FOR_UPDATE)
			this.type = Type.LISTEN;
		else if (samplingPeriod > 0)
			this.type = Type.POLLED;
		else
			this.type = Type.ONDEMAND;
		
		// set sampling time only for polled channels
		if (this.type == Type.POLLED) {

			// set first sampling time
			nextSamplingTime = System.currentTimeMillis();
			
			// round down to the next sampling period value
			nextSamplingTime -= nextSamplingTime % configuration.getSamplingPeriod();
		}
	}

	public synchronized void addUpdateListener(ChannelEventListener listener) {
		updateListeners.add(listener);		
	}

	public synchronized void addChangedListener(ChannelEventListener listener) {
		changedListeners.add(listener);
	}

	public synchronized void removeUpdateListener(ChannelEventListener listener) {
		updateListeners.remove(listener);
	}

	public synchronized void removeChangedListener(ChannelEventListener listener) {
		changedListeners.remove(listener);
	}

	public synchronized void update(SampledValueContainer svc, EventType type) {
		
		svcList.clear();
		svcList.add(svc);
		
		// we are not allowed to read the channel
		if (!read)
			return;
		
		// call update listeners unconditionally
		if (!updateListeners.isEmpty())
		for (ChannelEventListener listener : updateListeners) {
			try {
				listener.channelEvent(type, svcList);
			} catch (Throwable t) {
				if (logLimiter.check())
					logger.warn("exeption in updateListener for channel {}", svc.getChannelLocator(), t);
			}
		}
		
		// check if the value changed, if yes call changed listeners
		if (!changedListeners.isEmpty())
		if (type != EventType.UPDATED) {
			for (ChannelEventListener listener : changedListeners) {
				try {
					listener.channelEvent(type, svcList);
				} catch (Throwable t) {
					if (logLimiter.check())
						logger.warn("exeption in changedListener for channel {}", svc.getChannelLocator(), t);
				}
			}
		}
	}

	public SampledValue getChannelValue() throws ChannelAccessException {
		
		// are we allowed to read it?
		if (!read) {
			throw new SecurityException("read for configuration " + configuration.toString() + " not allowed.");
		}
		
		// if this is a sampled channel ask driver
		if (type == Type.ONDEMAND) {
			channel.readChannel();
		}
		
		// return last stored value (possibly updated in previous step)
		return channel.getStoredValue();
	}
	
	public void setChannelValue(Value value) throws ChannelAccessException {

		// are we allowed to write it?
		if (!write) {
			throw new SecurityException("write for configuration " + configuration.toString() + " not allowed.");
		}
		
		channel.writeChannel(value);
	}
	
	// return time until next sampling
	// if sampling time is zero, add the channel to the list queued reads
	synchronized long updateSamplingTime(long currentTime,
			List<SampledValueContainer> containers) {

		// is it a sampled channel?
		if (!read || type != Type.POLLED) {
			return Long.MAX_VALUE;
		} else {
			long waittime = nextSamplingTime - currentTime;

			if (waittime > 0)
				return waittime;

			// increment nextSamplingTime until the currentTime is reached
			do {
				nextSamplingTime += configuration.getSamplingPeriod();
			} while (nextSamplingTime < currentTime);

			ChannelLocator last = null;
			
			if (!containers.isEmpty()) {
				last = containers.get(containers.size() - 1).getChannelLocator();
			}
			
			// check if the list already contains the channel
			if (!channel.getChannelLocator().equals(last)) {
				// add Channel to the list
				containers.add(new SampledValueContainer(channel.getChannelLocator()));
			}
			
			return 0;
		}
	}

	public ChannelConfiguration getConfiguration() {
		return configuration;
	}

	Type getType() {
		return type;
	}

	Object getAppID() {
		return configuration.getAppID();
	}
}
