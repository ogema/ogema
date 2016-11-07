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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchChannelException;
import org.ogema.core.channelmanager.driverspi.NoSuchDeviceException;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

public class ChannelDriverImpl implements ChannelDriver {

	private final String id;
	private final String description;
	
	int addDeviceListenerCount;
	int removeDeviceListenerCount;
	int abortDeviceScanCount;
	int startDeviceScanCount;
	int startChannelScanCount;
	int channelAddedCount;
	int channelRemovedCount;
	int readChannelsCount;
	int writeChannelsCount;
	int listenChannelsCount;
	int getChannelListCount;
	
	List<DeviceLocator> devices = new ArrayList<DeviceLocator>();

	List<DeviceListener> deviceListeners = new ArrayList<DeviceListener>();
	
	List<ChannelLocator> addedChannels = new ArrayList<ChannelLocator>();
	Map<ChannelLocator, AtomicInteger> channels = new HashMap<ChannelLocator, AtomicInteger>();
	
	ChannelScanListener channelScanListener;
	ChannelUpdateListener channelUpdatelistener;
	
	Map<DeviceLocator, List<ChannelLocator>> channelLists = new HashMap<DeviceLocator, List<ChannelLocator>>();
	
	List<SampledValueContainer> listenChannels;
	
	boolean wait;
	boolean async;
	
	UnsupportedOperationException uoe;
	IOException ioe;
	
	

	public ChannelDriverImpl(String id, String description) {
		this.id = id;
		this.description = description;
	}
	
	@Override
	public String getDriverId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		
		startDeviceScanCount++;

		if(uoe != null)
			throw uoe;
		
		for(DeviceLocator deviceLocator : devices) {
			listener.deviceFound(deviceLocator);
		}
		
		if(async)
			return;

		synchronized(this) {
			if (wait) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				listener.finished(false, ioe);
				
			} else {
				listener.finished(ioe == null, ioe);
			}
		}
	}

	@Override
	public synchronized void abortDeviceScan(String interfaceId, String filter) {
		abortDeviceScanCount++;
		wait = false;
		this.notify();
	}

	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		startChannelScanCount++;
		
		if(uoe != null)
			throw uoe;
		
		channelScanListener = listener;

		if(async)
			return;

		for(ChannelLocator current : channelLists.get(device)) {
			listener.channelFound(current);
		}
		
		listener.finished(true);

	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		getChannelListCount++;
		return channelLists.get(device);
	}

	@Override
	public synchronized void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		
		readChannelsCount++;
		
		for (SampledValueContainer container : channels) {
			ChannelLocator current = container.getChannelLocator();
			
			assertNotNull(current);
			assertTrue(addedChannels.contains(current));
			
			AtomicInteger val = this.channels.get(current);
		
			int i = val.getAndIncrement();
			
			container.setSampledValue(new SampledValue(new IntegerValue(i), System.currentTimeMillis(), Quality.GOOD));
		}

	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException, NoSuchDeviceException, NoSuchChannelException, IOException {
		listenChannelsCount++;
		
		listenChannels = channels;
		channelUpdatelistener = listener;
		
	}

	@Override
	public void writeChannels(List<ValueContainer> channels)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		writeChannelsCount++;

		for (ValueContainer container : channels) {
			ChannelLocator current = container.getChannelLocator();
			
			assertNotNull(current);
			assertTrue(addedChannels.contains(current));
			
			AtomicInteger val = this.channels.get(current);
		
			val.set(container.getValue().getIntegerValue());
		}
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		channelAddedCount++;
		
		addedChannels.add(channel);
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		channelRemovedCount++;
		
		assertTrue(addedChannels.remove(channel));
	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		addDeviceListenerCount++;
		deviceListeners.add(listener);
	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		removeDeviceListenerCount++;
		deviceListeners.remove(listener);
	}

	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		// TODO Auto-generated method stub

	}

	public void callDeviceListenersFound(DeviceLocator device) {
		for(DeviceListener listener : deviceListeners) {
			listener.deviceAdded(device);
		}
	}
	
	public void callDeviceListenersRemoved(DeviceLocator device) {
		for(DeviceListener listener : deviceListeners) {
			listener.deviceRemoved(device);
		}
	}

	public void addDevice(DeviceLocator deviceLocator) {
		devices.add(deviceLocator);
	}
}
