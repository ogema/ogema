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
package org.ogema.tests.dumydriver;

import java.io.IOException;
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

/**
 * This is a wrapper for the jamod api (http://jamod.sourceforge.net)
 * 
 * On every conectDevice() a new Modbus connection is created. The Modbus connection is torn down on disconnectDevice()
 * 
 * This driver offers Modbus RTU master transfers.
 * 
 * The following channel adressing modes are supported by this driver:
 * 
 * - access 16bit register "reg:<hex address> returns an IntegerValue
 * 
 * - access multiple 16bit registers "multireg:<no of 16bit register>:<first register address in hex>" returns an
 * ByteArrayValue
 * 
 * - access modbus single bit coil: "coil:<bit number>"
 * 
 * @author pau
 * 
 */
@Component
@Service(ChannelDriver.class)
public class DummyDriver implements ChannelDriver {

	protected static final String DRIVER_ID = "dummy-lld";
	private static final String DESCRIPTION = "Dummy Test Driver";
	SampledValue value;

	public DummyDriver() {
		value = new SampledValue(new LongValue(System.currentTimeMillis()), System.currentTimeMillis(), Quality.BAD);
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

	/**
	 * SampledValueContainer has the ChannelLocator which in turn has the addressing. The addressing has to be string
	 * parsed in order to get which registers have to be read. The addressing has to be parsed once for every new
	 * ChannelLocator, if ChannelLocators are immutable. The serial port parameters are set according to the setting for
	 * each device.
	 */
	@Override
	public void readChannels(List<SampledValueContainer> channels) throws UnsupportedOperationException, IOException {

		for (SampledValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}

			try {
				// read data

				container.setSampledValue(new SampledValue(new LongValue(System.currentTimeMillis()), System
						.currentTimeMillis(), Quality.GOOD));
			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
	}

	/**
	 * The port parameters are varied according to each accessed device. How do the synchronous read and the async read
	 * work together? (They should be a queue of transactions)
	 */
	@Override
	public void listenChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeChannels(List<ValueContainer> channels) throws UnsupportedOperationException, IOException {
		for (ValueContainer container : channels) {
			ChannelLocator channelLocator = container.getChannelLocator();

			try {

			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
	}

	/**
	 * Frees all channels, devices and interfaces
	 */
	@Override
	public void shutdown() {
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
		// TODO Auto-generated method stub
		
	}
}
