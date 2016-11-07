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
