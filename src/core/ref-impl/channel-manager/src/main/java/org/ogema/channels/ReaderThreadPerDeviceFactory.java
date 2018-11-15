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

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

/**
 * This ReaderThreadFactory generates one ReaderThread per Device.
 * 
 * @author pau
 *
 */
class ReaderThreadPerDeviceFactory implements ReaderThreadFactory {

	/** reference to the channel manager. Needed to get a driver reference. */
	private ChannelManagerImpl channelManager;
	
	/** list of all reader threads */
	private Map<DeviceLocator, ReaderThread> threads = new HashMap<DeviceLocator, ReaderThread>();
	
	ReaderThreadPerDeviceFactory(ChannelManagerImpl channelManager) {
		this.channelManager = channelManager;
	}
	
	@Override
	public synchronized void noChannels(ReaderThread readerThread) {
		DeviceLocator deviceLocator = (DeviceLocator) readerThread.getKey();
		threads.remove(deviceLocator);
		readerThread.stop();
	}

	@Override
	public synchronized ReaderThread getReaderThread(Channel channel) throws ChannelAccessException {
		DeviceLocator deviceLocator = channel.getChannelLocator().getDeviceLocator();
		Driver driver;
		
		try {
			driver = channelManager.getDriver(deviceLocator.getDriverName());
		} catch (NoSuchDriverException e1) {
			throw new ChannelAccessException(e1);
		}
		
		ReaderThread thread = threads.get(deviceLocator);
		
		if (thread == null) {
			thread = new ReaderThread(this, driver, deviceLocator, "ChannelManagerReaderThread:" + deviceLocator);
			threads.put(deviceLocator, thread);
		}
		
		try {
			thread.add(channel);
		} catch (ChannelAccessException e) {
			thread.remove(channel);
			throw e;
		}
		
		return thread;
	}

}
