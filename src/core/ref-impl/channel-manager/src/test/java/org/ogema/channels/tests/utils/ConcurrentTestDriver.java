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
package org.ogema.channels.tests.utils;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
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
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

public class ConcurrentTestDriver implements ChannelDriver {

	public final boolean enableDiscoverDevices = true;
	public final boolean enableDiscoverChannels = true;

	private final String driverName;
	private final String driverDescription;
	
	private final Map<ChannelLocator, Value> channelValues;

	public class Device {
		public Device(DeviceLocator dl) {
			this.deviceLocator = dl;
			this.channelLocators = new LinkedList<ChannelLocator>();
		}

		public synchronized void addChannel(ChannelLocator channel) {
			channelLocators.add(channel);
		}

		private final DeviceLocator deviceLocator;
		private final List<ChannelLocator> channelLocators;
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof Device))
				return false;
			return ((Device) obj).deviceLocator.equals(this.deviceLocator);
		}
		
		public int hashCode() {
			return deviceLocator.hashCode();
		};
		
	}

	private final List<Device> deviceList;

	public ConcurrentTestDriver(String driverName, String description) {
		this.driverName = driverName;
		this.driverDescription = description;
		this.channelValues = new ConcurrentHashMap<ChannelLocator, Value>();
		deviceList = Collections.synchronizedList(new LinkedList<Device>());
	}

	public Device addDevice(DeviceLocator deviceLocator) {
		synchronized (deviceList) {
			Device device = new Device(deviceLocator);
			if (!deviceList.contains(device))
				deviceList.add(device);
			return device;
		}
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

		synchronized(deviceList) {
			if (this.deviceList.size() == 0) {
				throw new UnsupportedOperationException("discoverDevices");
			}

			for (Device device : this.deviceList) {
	
				if (device.deviceLocator.getInterfaceName().equals(interfaceId)) {
					// System.out.println("Add device " + device.deviceLocator + " at interface " + interfaceId);
	
					deviceList.add(device.deviceLocator);
				}
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

		synchronized(deviceList) {
			if (this.deviceList.size() == 0) {
				throw new UnsupportedOperationException("discoverDevices");
			}
	
			for (Device device : this.deviceList) {
	
				if (device.deviceLocator.getInterfaceName().equals(interfaceId)) {
					deviceList.add(device.deviceLocator);
				}
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
		synchronized(deviceList) {
			for (Device device : deviceList) {
				if (device.deviceLocator.equals(deviceLocator)) {
					selectedDevice = device;
					break;
				}
			}
		}
		if (selectedDevice != null) {
			List<ChannelLocator> channels;
			synchronized (selectedDevice) {
				channels = selectedDevice.channelLocators;
			}

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
	
	private final Map<ChannelLocator,CountDownLatch> channelReadLatches = new ConcurrentHashMap<>();
	
	public void setExpectedChannelRead(ChannelLocator channel, int nr) {
		channelReadLatches.put(channel, new CountDownLatch(nr));
	}
	
	public void clearExpectedChannelReads() {
		channelReadLatches.clear();
	}
	
	public boolean awaitChannelReads(ChannelLocator channel, long timeout, TimeUnit unit) throws InterruptedException {
		CountDownLatch latch = channelReadLatches.get(channel);
		return latch.await(timeout, unit);
	}
	
	public void assertChannelRead(ChannelLocator channel, long timeout, TimeUnit unit) throws InterruptedException {
		Assert.assertTrue(awaitChannelReads(channel, timeout, unit));
	}
	
	public void assertAllChannelsRead(long timeout, TimeUnit unit) throws InterruptedException {
		for (CountDownLatch l: channelReadLatches.values()) {
			Assert.assertTrue("Channel reading failed",l.await(timeout, unit));
		}
		clearExpectedChannelReads();
	}
	
	public void assertAllChannelsRead() throws InterruptedException {
		assertAllChannelsRead(5, TimeUnit.SECONDS);
	}
	
	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		// System.out.println("readChannels " + this + " : " + readChannelsCalled);

		long timestamp = System.currentTimeMillis();

		for (SampledValueContainer container : channels) {
			ChannelLocator locator = container.getChannelLocator();
//			 System.out.println("READ VALUE " + container.getChannelLocator());

			Value value = channelValues.get(container.getChannelLocator());

			if (value == null) {

				if (container.getSampledValue() == null) {
					value = new IntegerValue(0);

				}
				else {
					try {

						value = new IntegerValue(container.getSampledValue().getValue().getIntegerValue() + 1);
					} catch (IllegalConversionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						value = new IntegerValue(0);
					}
				}
			}
			container.setSampledValue(new SampledValue(value, timestamp, Quality.GOOD));
			CountDownLatch latch = channelReadLatches.get(locator);
			if (latch != null)
				latch.countDown();
//			 System.out.println(container.getChannelLocator() + " new value " + value.getIntegerValue() );
		}
	}

	public class DataSimulationThread implements Runnable {

		private Thread thread = new Thread(this);
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
				

				if (svc != null) {
					SampledValue value = new SampledValue(v, System.currentTimeMillis(), Quality.GOOD);

					for (SampledValueContainer c : svc) {
						c.setSampledValue(value);
					}
				}
				
				if (listener != null)
					listener.channelsUpdated(svc);
				
				if (svc != null) {
					for (SampledValueContainer c : svc) {
						CountDownLatch latch = channelReadLatches.get(c.getChannelLocator());
						
						if (latch != null) {
							latch.countDown();
						}
					}
				}
				
				synchronized(this) {
					try {
						this.wait(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public long listenChannelsCalled;
	public boolean ignoreListenChannels;
	private DataSimulationThread listenThread = new DataSimulationThread();

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		listenChannelsCalled++;
		
		if (!ignoreListenChannels)
			listenThread.setChannels(channels, listener);
	}
	
	private final Map<ChannelLocator,CountDownLatch> channelWriteLatches = new ConcurrentHashMap<>();
	
	public void setExpectedChannelWrites(ChannelLocator channel, int nr) {
		channelWriteLatches.put(channel, new CountDownLatch(nr));
	}

	public void clearExpectedChannelWrites() {
		channelWriteLatches.clear();
	}
	
	public boolean waitForChannelWrites(ChannelLocator cl, long timeout, TimeUnit unit) throws InterruptedException {
		return channelWriteLatches.get(cl).await(timeout, unit);
	}
	
	public void assertAllChannelsWritten(long timeout, TimeUnit unit) throws InterruptedException {
		for (CountDownLatch l: channelWriteLatches.values()) {
			Assert.assertTrue("Channel write failed",l.await(timeout, unit));
		}
		channelWriteLatches.clear();
	}
	
	public void assertAllChannelsWritten() throws InterruptedException {
		assertAllChannelsWritten(3, TimeUnit.SECONDS);
	}
	 
	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException {

		// System.out.println("DRIVER-WRITE_CHANNELS");

		for (ValueContainer channel : channels) {
//			System.out.println("WRITE VALUE " + channel.getChannelLocator());
			channelValues.put(channel.getChannelLocator(), channel.getValue());
			CountDownLatch l = channelWriteLatches.get(channel.getChannelLocator());
			if (l!= null)
				l.countDown();
		}
	}

	@Override
	public void shutdown() {
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
//		System.out.println("WRITE VALUE " + channelLocator);
		channelValues.put(channelLocator, value);
		CountDownLatch l = channelWriteLatches.get(channelLocator);
		if (l!= null)
			l.countDown();
	}

}
