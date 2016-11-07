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
package org.ogema.tests.dumydriver.lld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
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
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

@Component
@Service(ChannelDriver.class)
public class DummyDriver implements ChannelDriver {

	public static final String DRIVER_ID = "dummy-lld";
	private static final String DESCRIPTION = "Dummy Test Driver";
	SampledValue value;
	ChannelUpdateListener listener;
	private BusEngine bus;

	public DummyDriver() {
		value = new SampledValue(new LongValue(System.currentTimeMillis()), System.currentTimeMillis(), Quality.BAD);
		this.bus = new BusEngine(this);

		Thread t = new Thread(this.bus);
		t.setName("Dummy-Driver-Bus-Thread");
		t.start();
	}

	@Override
	public String getDriverId() {
		return DRIVER_ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	/**
	 * Scanning method that returns all possible channels of a device.
	 */
	@Override
	public List<ChannelLocator> getChannelList(DeviceLocator device) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {

		bus.putRequest(channels);
		synchronized (channels) {
			try {
				channels.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		this.listener = listener;
	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException {
		bus.putRequest(channels);
		synchronized (channels) {
			try {
				channels.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Frees all channels, devices and interfaces
	 */
	@Override
	public void shutdown() {
		bus.stop();
	}

	@Override
	public void channelAdded(ChannelLocator channel) {
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {
	}

	/**
	 * Scanning method that returns all connected devices - not applicable for Modbus
	 */
	@Override
	public void startDeviceScan(String interfaceId, String filter, DeviceScanListener listener)
			throws UnsupportedOperationException, NoSuchInterfaceException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortDeviceScan(String interfaceId, String filter) {
		// ignore since device scan is not supported
	}

	/**
	 * Scanning method that returns all channels of a device - not applicable for Modbus
	 */
	@Override
	public void startChannelScan(DeviceLocator device, ChannelScanListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
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
	public void writeChannel(ChannelLocator channelLocator, Value value)
			throws UnsupportedOperationException, IOException, NoSuchDeviceException, NoSuchChannelException {
		ArrayList<SampledValueContainer> writeBack = new ArrayList<>();
		SampledValueContainer contBack = new SampledValueContainer(channelLocator);
		long millis = System.currentTimeMillis();
		contBack.setSampledValue(new SampledValue(new LongValue(millis), millis, Quality.GOOD));
		writeBack.add(contBack);
		listener.channelsUpdated(writeBack);
	}

}
