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
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class executes the periodic polling of the assigned channels.
 * It queries the assigned channels for the smallest waittime of all channels.
 * If a channel has to be serviced it returns a wait time of 0 and adds its ChannelLocator to the list of active channels.
 * During the query the polling interval is updated inside the Channel (the Configuration actually).
 * 
 * Which channels are assigned to which ReaderThread is determined by the ReaderThreadFactory implementation.
 * When the last channel is removed from the ReaderThread it reports this to the Factory. 
 * The Factory has to decide if it terminates the ReaderThread.
 * 
 * @author pau
 *
 */
class ReaderThread implements Runnable {

	/** The timeout join() waits for the thread to terminate. */
	private static final long JOIN_TIMEOUT = 30000;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final LogLimiter logLimiter = new LogLimiter(logger);

	private Thread thread;
	
	/** list of channels added to the reader thread. */
	private Map<ChannelLocator, Channel> channels = new ConcurrentHashMap<ChannelLocator, Channel>();
	
	/** true, if run() should stop */
	private volatile boolean stop;
	
	/** reference to the creating factory */
	private ReaderThreadFactory factory;
	
	/** reference to the driver associated with this reader thread */
	private Driver driver;
	
	/** an opaque handle for the factory to find the reader thread again in its internal data structures */
	private Object key;

	/**
	 * 
	 * @param factory the creating factory 
	 * @param driver the driver that should be polled
	 * @param key opaque handle for use by the factory
	 */
	ReaderThread(ReaderThreadFactory factory, Driver driver, Object key, String name) {
		this.factory = factory;
		
		if (driver == null)
			throw new NullPointerException("driver must not be null.");
		
		this.driver = driver;
		
		this.key = key;
		
		thread = new Thread(this);
		thread.setName(name);
		thread.start();
	}
	
	@Override
	public void run() {
		
		List<SampledValueContainer> list = new ArrayList<SampledValueContainer>();
		
		while(!stop) {
			long waittime = Long.MAX_VALUE;
			long currentTime = System.currentTimeMillis();
			
			list.clear();

			// gather list of channels to poll
			// ConcurrentHashMap does not create a ConcurrentModifcationException
			for (Channel channel : channels.values()) {
				long newTime = channel.updateSamplingTime(currentTime, list);
				
				if (newTime < waittime)
					waittime = newTime;
				
			}
			
			// if there are pending channels
			if (!list.isEmpty()) {
				
				try {
					driver.readChannels(list);
				} catch (Throwable t) {
					if (logLimiter.check())
						logger.warn("driver error", t);
				}
				
				// notify channels of new value
				for (SampledValueContainer svc : list) {
					Channel channel = channels.get(svc.getChannelLocator());
					if (channel != null) {
						channel.update(svc);
					}
				}
				
			}
			else {
				synchronized(this) {
					if (!stop) {
						
						// disable timeout 
						if (channels.isEmpty())
							waittime = 0;
						
						try {
							this.wait(waittime);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				}
			}
		}
		
		logger.debug("ReaderThread {} finished", this);
	}
	
	/**
	 * Add a channel to this reader thread.
	 * This does not wake the thread.
	 * 
	 * @param channel the added channel
	 * @throws ChannelAccessException the driver of the channel does not match the driver of the reader thread
	 */
	void add(Channel channel) throws ChannelAccessException {

		String driverName1 = channel.getChannelLocator().getDeviceLocator().getDriverName();
		String driverName2 = driver.getId();
		
		if (!driverName1.equals(driverName2))
			throw new ChannelAccessException(
					"internal error: driver Mismatch: Reader Thread is for Driver " 
					+ driverName2 
					+ ". Trying to add Channel " 
					+ channel.getChannelLocator());
	
		channels.put(channel.getChannelLocator(), channel);
		//this.notify();
	}
	
	/**
	 * Remove a channel from the reader thread.
	 * If no channel remains, factory.noChannels is called.
	 * 
	 * @param channel the removed channel
	 */
	void remove(Channel channel) {
		channels.remove(channel.getChannelLocator());
		
		// no need to wake the thread. 
		// If the removed channel had the shortest wait time, 
		// the thread will wake up and will find nothing to do. 
		//this.notify();
		
		if (channels.isEmpty()) {
			factory.noChannels(this);
		}
	}
	
	/**
	 * wake thread to poll the channels again.
	 */
	synchronized void update() {
		this.notify();
	}
	
	/**
	 * stop the thread.
	 * waits until the thread has joined.
	 */
	void stop() {
		long starttime;
	
		synchronized(this) {
			stop = true;
			this.notify();
		}
		
		starttime = System.currentTimeMillis();
		
		try {
			thread.join(JOIN_TIMEOUT);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		if (thread.isAlive()) {
			logger.warn("ReaderThread {} did not terminate after {} millis.", 
					this, System.currentTimeMillis() - starttime);
		}
	}
	
	/**
	 * returns the opaque handle given during construction.
	 */
	Object getKey() {
		return key;
	}
}
