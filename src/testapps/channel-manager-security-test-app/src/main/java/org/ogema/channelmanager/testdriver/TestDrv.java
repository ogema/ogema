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
package org.ogema.channelmanager.testdriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

public class TestDrv implements ChannelDriver {

	public int channelAddedCalled = 0;
	public int channelRemovedCalled = 0;
	public int readChannelsCalled = 0;
	public int resetCalled = 0;
	public int writeChannelsCalled = 0;
	public int listenChannelsCalled = 0;

	public boolean enableDiscoverDevices = true;
	public boolean enableDiscoverChannels = true;

	private String driverName;
	private String driverDescription;

	private HashMap<ChannelLocator, Value> channelValues;

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

	List<Device> deviceList;

	public TestDrv(String driverName, String description) {
		this.driverName = driverName;
		this.driverDescription = description;
		this.channelValues = new HashMap<ChannelLocator, Value>();
		deviceList = new LinkedList<Device>();
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

		if (this.deviceList.size() == 0)
			throw new UnsupportedOperationException("discoverDevices");

		for (Device device : this.deviceList) {

			if (device.deviceLocator.getInterfaceName().equals(interfaceId)) {
				System.out.println("Add device " + device.deviceLocator + " at interface " + interfaceId);

				deviceList.add(device.deviceLocator);
			}
		}

		float progressStep = 1.f / deviceList.size();

		float progress = 0.f;

		if (deviceList.size() == 0)
			throw new NoSuchInterfaceException(interfaceId);

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

		if (this.deviceList.size() == 0)
			throw new UnsupportedOperationException("discoverDevices");

		for (Device device : this.deviceList) {

			if (device.deviceLocator.getInterfaceName().equals(interfaceId))
				deviceList.add(device.deviceLocator);
		}

		if (deviceList.size() == 0)
			throw new NoSuchInterfaceException(interfaceId);

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
		else
			listener.finished(false);

	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {
		readChannelsCalled++;
		// System.out.println("readChannels " + this + " : " + readChannelsCalled);

		long timestamp = System.currentTimeMillis();

		for (SampledValueContainer container : channels) {

			// System.out.print("\tR " + (container.getChannelLocator()).getChannelAddress());

			Value value = null;

			if (channelValues.containsKey(container.getChannelLocator())) {

				value = channelValues.get(container.getChannelLocator());
			}
			else {

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

			// System.out.println(container.getChannelLocator() + " " +
		}
	}

	public class DataSimulationThread implements Runnable {

		List<SampledValueContainer> channels;
		ChannelUpdateListener listener;

		public DataSimulationThread(List<SampledValueContainer> channels, ChannelUpdateListener listener) {
			this.channels = channels;
			this.listener = listener;
		}

		public void run() {

			while (true) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Value v = new IntegerValue(3);
				SampledValue value = new SampledValue(v, System.currentTimeMillis(), Quality.GOOD);

				for (SampledValueContainer c : channels) {
					c.setSampledValue(value);
				}

				listener.channelsUpdated(channels);
			}
		}
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		listenChannelsCalled++;

		System.out.println("In listenChannels");

		Runnable r = new DataSimulationThread(channels, listener);

		new Thread(r).start();
	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException {

		System.out.println("DRIVER-WRITE_CHANNELS");

		for (ValueContainer channel : channels) {
			System.out.println("WRITE VALUE " + channel.getChannelLocator());
			channelValues.put(channel.getChannelLocator(), channel.getValue());
		}

		writeChannelsCalled++;
	}

	@Override
	public void shutdown() {
		resetCalled++;
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
		channelAddedCalled++;

	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
		channelRemovedCalled++;
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

	@Override
	public void writeChannel(ChannelLocator channelLocator, Value value) throws UnsupportedOperationException,
			IOException, NoSuchDeviceException, NoSuchChannelException {
		System.out.println("DRIVER-WRITE_CHANNELS");

		System.out.println("WRITE VALUE " + channelLocator);
		channelValues.put(channelLocator, value);

		writeChannelsCalled++;
	}

}
