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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ogema.core.application.AppID;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver is responsible to hold the ChannelDriver reference, 
 * manages the list of registered listen channels, the list of running device scans 
 * and the list of active channels for the driver.
 * 
 * It implements ChannelUpdateListener as only one listener can be registered per driver.
 * 
 * @author pau
 *
 */
class Driver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** true, when the driver has been closed. Used to prohibit new actions after close() */
	private volatile boolean closed;
	
	/** the driver reference */
	private ChannelDriver driver;

	/** reference to the channel manager. used to get reader threads */
	private ReaderThreadFactory readerThreadFactory;

	private DeviceScanList deviceScanList = new DeviceScanList(this);

	/** map of all active channels on this driver */
	private final ConcurrentMap<ChannelLocator, Channel> channels = new ConcurrentHashMap<ChannelLocator, Channel>();
	
	private final ChannelUpdateListenerList channelUpdateListenerList = new ChannelUpdateListenerList(this, logger);
	
	private final DeviceListenerList deviceListenerList = new DeviceListenerList(this, logger);
	
	Driver(ReaderThreadFactory readerThreadFactory, ChannelDriver driver) {
		this.driver = driver;
		this.readerThreadFactory = readerThreadFactory;
	}
	
	String getId() {
		return driver.getDriverId();
	}
	
	String getDescription() {
		return driver.getDescription();
	}
	
	ChannelDriver getDriver() {
		return driver;
	}
	

	void addListenChannel(Channel channel) throws ChannelAccessException {
		channelUpdateListenerList.addListenChannel(channel);
	}

	void removeListenChannel(Channel channel) {
		channelUpdateListenerList.removeListenChannel(channel);
	}
	
	void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		driver.readChannels(channels);
	}
	
	void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		driver.writeChannels(channels);
	}

	void channelAdded(ChannelLocator channelLocator) {
		driver.channelAdded(channelLocator);
	}

	void channelRemoved(ChannelLocator channelLocator) {
		// FIXME ths leads to NPEs sometimes; in any case, uncontrolled code is called -> catch Exceptions or use separate thread
		driver.channelRemoved(channelLocator);
	}
	
	ChannelConfiguration addConfiguration(ChannelConfigurationImpl configuration) throws ChannelAccessException {

		Channel newChannel;
		Channel channel;
		ChannelConfiguration result = null;
		boolean tryAgain = false;

		do {
			// make channel adding and the closed check uninterruptible
			synchronized (this) {
				if (closed)
					throw new ChannelAccessException("driver has been closed.");

				// assume the channel does not exist
				newChannel = new Channel(this, configuration.getChannelLocator());
				channel = channels.putIfAbsent(newChannel.getChannelLocator(), newChannel);
				
				if (channel == null) {
					channel = newChannel;
				}
			}

			// if the channel is ever closed between putIfAbsent() and addConfiguration() just try again
			try {
				result = channel.addConfiguration(configuration);
			} catch (IllegalStateException e) {
				tryAgain = true;
			}

		} while (tryAgain);
		
		return result;
	}
	
	boolean removeConfiguration(ChannelConfiguration configuration) throws NoSuchChannelException {

		Channel channel;

		synchronized (this) {
			if (closed)
				return false;
	
			channel = channels.get(configuration.getChannelLocator());
		}
		
		if (channel == null)
			throw new NoSuchChannelException(configuration.getChannelLocator());

		return channel.removeConfiguration(configuration);
	}
	
	void channelClosed(Channel channel) {
		if (channels.remove(channel.getChannelLocator()) != null)
			channelRemoved(channel.getChannelLocator());
	}
	
	Configuration getConfiguration(ChannelConfiguration configuration) throws NoSuchChannelException {
		
		// find Channel
		Channel channel = channels.get(configuration.getChannelLocator());
		
		if (channel == null)
			throw new NoSuchChannelException(configuration.getChannelLocator());
		
		// ask Channel
		Configuration result = channel.getConfiguration(configuration);
		
		if (result == null)
			throw new NoSuchChannelException(configuration.getChannelLocator());
		
		return result;
	}

	void getChannels(List<ChannelLocator> channels) {
		channels.addAll(this.channels.keySet());
	}

	ReaderThread getReaderThread(Channel channel) throws ChannelAccessException {
		return readerThreadFactory.getReaderThread(channel);
	}

	synchronized void close() {
		
		closed = true;
		
		deviceScanList.close();
		
		deviceListenerList.close();

		// close and remove all channels
		for (Channel channel : channels.values()) {
			channel.close();
		}
		
		channels.clear();
		
		channelUpdateListenerList.close();
	
		driver = null;
		readerThreadFactory = null;
	}

	List<ChannelLocator> getChannelList(DeviceLocator deviceLocator) {
		return driver.getChannelList(deviceLocator);
	}

	void startChannelScan(DeviceLocator device, ChannelScanListener listener) {
		driver.startChannelScan(device, listener);
	}
	
	void removeAppID(AppID appID) {
		
		// remove device scanners
		deviceScanList.abortDeviceScanForAppID(appID);
		
		// remove device listeners
		deviceListenerList.removeDeviceListenersForApp(appID);

		// remove channels
		synchronized (this) {
			for (Channel channel : channels.values()) {
				channel.removeAppID(appID);
			}
		}
	}

	void removeDeviceListener(AppID appId, DeviceListener listener) {
		deviceListenerList.removeDeviceListener(appId, listener);
	}

	void addDeviceListener(AppID appId, DeviceListener listener) throws ChannelAccessException {
		deviceListenerList.addDeviceListener(appId, listener);
	}

	DeviceScanner startDeviceScan(String interfaceId, String filter, DeviceScanListener listener,
			AppID appID) throws UnsupportedOperationException, IOException, NoSuchInterfaceException {
		return deviceScanList.startDeviceScan(interfaceId, filter, listener, appID);
	}

	boolean abortDeviceScan(String interfaceId, String filter, AppID appID) {
		return deviceScanList.abortDeviceScan(interfaceId, filter, appID);
	}
}
