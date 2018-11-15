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
package org.ogema.channels.tests.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import org.junit.Assert;
import org.ogema.core.channelmanager.ChannelConfiguration;
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

public class TestDriver implements ChannelDriver {

	public volatile int channelAddedCalled = 0;
	public volatile int channelRemovedCalled = 0;
	public volatile int readChannelsCalled = 0;
	public volatile int resetCalled = 0;
	public volatile int writeChannelsCalled = 0;
	public volatile int listenChannelsCalled = 0;

	public boolean enableDiscoverDevices = true;
	public boolean enableDiscoverChannels = true;

	private final String driverName;
	private final String driverDescription;
	
	private final HashMap<ChannelLocator, Value> writeValues = new HashMap<ChannelLocator, Value>();
	private final HashMap<ChannelLocator, Value> readValues = new HashMap<ChannelLocator, Value>();
	
	List<Device> deviceList = new LinkedList<Device>();
	
	public class Device {
		public Device(DeviceLocator dl) {
			this.deviceLocator = dl;
			this.channelLocators = new LinkedList<ChannelLocator>();
		}

		public void addChannel(ChannelLocator channel) {
			channelLocators.add(channel);
		}

		DeviceLocator deviceLocator;
		List<ChannelLocator> channelLocators;
	}



	public TestDriver(String driverName, String description) {
		this.driverName = driverName;
		this.driverDescription = description;
	}

	public Device addDevice(DeviceLocator deviceLocator) {
		Device device = new Device(deviceLocator);

		deviceList.add(device);

		return device;

	}

	@Override
	public String getDriverId() {
		return driverName;
	}

	@Override
	public String getDescription() {
		return driverDescription;
	}

	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		List<DeviceLocator> deviceList = new ArrayList<DeviceLocator>(2);

		if (!enableDiscoverDevices) {
			throw new UnsupportedOperationException();
		}

		if (this.deviceList.size() == 0) {
			throw new UnsupportedOperationException("discoverDevices");
		}

		for (Device device : this.deviceList) {

			if (device.deviceLocator.getInterfaceName().equals(interfaceId)) {
				// System.out.println("Add device " + device.deviceLocator + " at interface " + interfaceId);

				deviceList.add(device.deviceLocator);
			}
		}

		float progressStep = 1.f / deviceList.size();

		float progress = 0.f;

		if (deviceList.size() == 0) {
			throw new NoSuchInterfaceException(interfaceId);
		}

		for (DeviceLocator deviceLocator : deviceList) {
			listener.deviceFound(deviceLocator);
			progress += progressStep;
			listener.progress(progress);
		}

		listener.finished(true, null);
	}

	public List<DeviceLocator> getDeviceList(String interfaceId, String filter) throws UnsupportedOperationException,
			NoSuchInterfaceException, IOException {
		List<DeviceLocator> deviceList = new ArrayList<DeviceLocator>(2);

		if (!enableDiscoverDevices) {
			throw new UnsupportedOperationException();
		}

		if (this.deviceList.size() == 0) {
			throw new UnsupportedOperationException("discoverDevices");
		}

		for (Device device : this.deviceList) {

			if (device.deviceLocator.getInterfaceName().equals(interfaceId)) {
				deviceList.add(device.deviceLocator);
			}
		}

		if (deviceList.size() == 0) {
			throw new NoSuchInterfaceException(interfaceId);
		}

		return deviceList;
	}

	@Override
	public void startChannelScan(DeviceLocator deviceLocator, ChannelScanListener listener)
			throws UnsupportedOperationException {
		Device selectedDevice = null;
		if (!enableDiscoverChannels) {
			throw new UnsupportedOperationException();
		}

		for (Device device : deviceList) {
			if (device.deviceLocator.equals(deviceLocator)) {
				selectedDevice = device;
				break;
			}
		}
		if (selectedDevice != null) {

			List<ChannelLocator> channels = selectedDevice.channelLocators;

			float progressStep = 1.f / channels.size();

			float progress = 0.f;

			for (ChannelLocator channel : channels) {
				listener.channelFound(channel);

				progress += progressStep;

				listener.progress(progress);
			}

			listener.finished(true);
		}
		else {
			listener.finished(false);
		}
	}
	
	private final Map<ChannelLocator, CountDownLatch> channelReadLatches = new ConcurrentHashMap<>();
	
	public void setExpectedChannelRead(ChannelConfiguration channel, int nr) {
		channelReadLatches.put(channel.getChannelLocator(), new CountDownLatch(nr));
	}
	
	public void clearExpectedChannelReads() {
		channelReadLatches.clear();
	}
	
	public boolean awaitChannelReads(ChannelConfiguration channel, long timeout, TimeUnit unit) throws InterruptedException {
		return channelReadLatches.get(channel.getChannelLocator()).await(timeout, unit);
	}
	
	public void assertChannelRead(ChannelConfiguration channel, long timeout, TimeUnit unit) throws InterruptedException {
		Assert.assertTrue(awaitChannelReads(channel, timeout, unit));
	}
	
	public void assertAllChannelsRead(long timeout, TimeUnit unit) {
		for (CountDownLatch l: channelReadLatches.values()) {
			try {
				Assert.assertTrue(l.await(timeout, unit));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		clearExpectedChannelReads();
	}
	
	public void assertAllChannelsRead() throws InterruptedException {
		assertAllChannelsRead(5, TimeUnit.SECONDS);
	}
	
	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		readChannelsCalled++;
		// System.out.println("readChannels " + this + " : " + readChannelsCalled);

		long timestamp = System.currentTimeMillis();

		for (SampledValueContainer container : channels) {
			ChannelLocator locator = container.getChannelLocator();
			 //System.out.println("READ VALUE " + container.getChannelLocator());

			Value value = null;

			if (writeValues.containsKey(container.getChannelLocator())) {

				value = writeValues.get(container.getChannelLocator());
				
				System.out.println("VALUE " + value.toString());
			}
			else {

				Value previous = readValues.get(locator);
				
				if (previous != null) {
					value = new IntegerValue(previous.getIntegerValue() + 1);
					//System.out.println("VALUE + 1");
				} else {
					value = new IntegerValue(0);
					//System.out.println("VALUE new 0");
				}
				
				readValues.put(locator, value);
			}
			container.setSampledValue(new SampledValue(value, timestamp, Quality.GOOD));
			CountDownLatch latch = channelReadLatches.get(locator);
			if (latch != null)
				latch.countDown();
		}
	}

	public class DataSimulationThread implements Runnable {

		private Thread thread = new Thread();
		private volatile boolean running = true;
		
		List<SampledValueContainer> channels;
		ChannelUpdateListener listener;

		public DataSimulationThread() {
			thread.start();
		}
		
		public void stop() {
			running = false;
			
			synchronized(this) {
				this.notify();
			}
			
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

		public synchronized void setChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener) {
			//assertNotNull(listener);
			assertNotNull(channels);
			
			this.channels = channels;
			this.listener = listener;
			
			this.notify();
		}
		
		@Override
		public void run() {
			Value v = new IntegerValue(3);
			while (running) {
				List<SampledValueContainer> svc;
				ChannelUpdateListener listener;
				
				synchronized (this) {
					svc = this.channels;
					listener = this.listener;
				}
				
				SampledValue value = new SampledValue(v, System.currentTimeMillis(), Quality.GOOD);

				for (SampledValueContainer c : svc) {
					c.setSampledValue(value);
				}

				if (listener != null)
					listener.channelsUpdated(svc);
				
				synchronized(this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		listenChannelsCalled++;
		
		if (!ignoreListenChannels)
			listenThread.setChannels(channels, listener);
	}
	
	private DataSimulationThread listenThread = new DataSimulationThread();
	
	private final Map<ChannelLocator, CountDownLatch> channelWriteLatches = new ConcurrentHashMap<ChannelLocator, CountDownLatch>();
	
	public boolean ignoreListenChannels;
	
	public void setExpectedChannelWrites(ChannelConfiguration channel, int nr) {
		channelWriteLatches.put(channel.getChannelLocator(), new CountDownLatch(nr));
	}

	public void clearExpectedChannelWrites() {
		channelWriteLatches.clear();
	}
	
	public boolean waitForChannelWrites(ChannelConfiguration cl, long timeout, TimeUnit unit) throws InterruptedException {
		return channelWriteLatches.get(cl.getChannelLocator()).await(timeout, unit);
	}
	
	public void assertAllChannelsWritten(long timeout, TimeUnit unit) throws InterruptedException {
		for (CountDownLatch l: channelWriteLatches.values()) {
			Assert.assertTrue(l.await(timeout, unit));
		}
		channelWriteLatches.clear();
	}
	
	public void assertAllChannelsWritten() throws InterruptedException {
		assertAllChannelsWritten(3, TimeUnit.SECONDS);
	}
	 
	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException {

		//System.out.println("DRIVER-WRITE_CHANNELS");

		for (ValueContainer channel : channels) {
			System.out.println("WRITE VALUE " + channel.getChannelLocator());
			writeValues.put(channel.getChannelLocator(), channel.getValue());
			CountDownLatch l = channelWriteLatches.get(channel.getChannelLocator());
			if (l!= null)
				l.countDown();
		}

		writeChannelsCalled++;
	}

	@Override
	public void shutdown() {
		resetCalled++;
		listenThread.stop();
	}

	
	private volatile CountDownLatch channelAddedLatch = null;
	
	public void setExpectedChannelAddeds(int nr) {
		channelAddedLatch = new CountDownLatch(nr);
	}
	
	public boolean waitForChannelsAdded(long timeout, TimeUnit unit) throws InterruptedException {
		return channelAddedLatch.await(timeout, unit);
	}
	
	public void assertChannelsAdded() throws InterruptedException {
		Assert.assertTrue(waitForChannelsAdded(5, TimeUnit.SECONDS));
	}
	
	@Override
	public void channelAdded(ChannelLocator channel) {
		channelAddedCalled++;
		if (channelAddedLatch != null)
			channelAddedLatch.countDown();
	}

	private volatile CountDownLatch channelRemovedLatch = null;
	
	public void setExpectedChannelRemoveds(int nr) {
		channelRemovedLatch = new CountDownLatch(nr);
	}
	
	public boolean waitForChannelsRemoved(long timeout, TimeUnit unit) throws InterruptedException {
		return channelRemovedLatch.await(timeout, unit);
	}
	
	public void assertChannelsRemoved() throws InterruptedException {
		Assert.assertTrue(waitForChannelsRemoved(5, TimeUnit.SECONDS));
	}
	
	@Override
	public void channelRemoved(ChannelLocator channel) {
		channelRemovedCalled++;
		if (channelRemovedLatch != null)
			channelRemovedLatch.countDown();
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		// ignore
	}

	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDeviceListener(DeviceListener listener) {
		// TODO Auto-generated method stub

	}
	
	// TODO this does not update the channel manager channel value, hence leads to test failures...
	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value) throws UnsupportedOperationException,
			IOException, NoSuchDeviceException, NoSuchChannelException {
		System.out.println("WRITE VALUE " + channelLocator);
		writeValues.put(channelLocator, value);
		CountDownLatch l = channelWriteLatches.get(channelLocator);
		if (l!= null)
			l.countDown();
		writeChannelsCalled++;
	}

}
