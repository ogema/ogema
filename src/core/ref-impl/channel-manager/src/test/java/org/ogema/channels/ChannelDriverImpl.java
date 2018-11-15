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
