/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.modbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.ExceptionListener;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareListener;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.osgi.framework.BundleContext;

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
public class ModbusDriver implements ChannelDriver, HardwareListener {

	private static final String DRIVER_ID = "modbus-rtu";
	private static final String DESCRIPTION = "MODBUS RTU over Serial Line";

	private final List<Connection> connectionList;

	@Reference
	private HardwareManager hardwareManager;

	public ModbusDriver() {
		connectionList = new ArrayList<Connection>();
	}

	@Activate
	protected void activate(BundleContext ctx) {
		hardwareManager.addListener(this);
	}

	@Deactivate
	protected void deactivate(BundleContext ctx) {
		hardwareManager.removeListener(this);
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
				Connection con = findConnection(channelLocator.getDeviceLocator().getInterfaceName());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				Channel channel = dev.findChannel(channelLocator);

				// read data
				container.setSampledValue(channel.readValue(con));

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
				Connection con = findConnection(channelLocator.getDeviceLocator().getInterfaceName());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				Channel channel = dev.findChannel(channelLocator);

				// write data
				channel.writeValue(con, container.getValue());

			} catch (NullPointerException e) {
				throw new IOException("Unknown channel: " + channelLocator, e);
			}
		}
	}

	/**
	 * Frees all channels, devices and interfaces
	 */
	@Override
	public void reset() {
		for (Connection con : connectionList) {
			removeConnection(con);
			con.close();
		}
	}

	@Override
	public void channelAdded(ChannelLocator channel) {

		DeviceLocator device = channel.getDeviceLocator();
		String iface = device.getInterfaceName();

		Connection con;
		Device dev;
		Channel chan;

		// Connection handling
		con = findConnection(iface);

		if (con == null) {
			con = new Connection(this, device.getInterfaceName(), device.getParameters());
			addConnection(con);
		}
		// else {
		// // check if the same port parameters have been used
		// if (!device.getParameters().equals(con.getParameterString()))
		// throw new IOException("Inteface " + device.getInterfaceName()
		// + " is already open with different serial parameters");
		// }

		// Device handling
		dev = con.findDevice(device);

		if (dev == null) {
			dev = new Device(device);
			con.addDevice(dev);
		}

		// Channel handling
		chan = dev.findChannel(channel);

		if (chan == null) {
			chan = Channel.createChannel(channel);
			dev.addChannel(chan);
		}
	}

	@Override
	public void channelRemoved(ChannelLocator channel) {

		DeviceLocator device = channel.getDeviceLocator();
		String iface = device.getInterfaceName();

		Connection con;
		Device dev;
		Channel chan;

		con = findConnection(iface);

		if (con != null) {
			dev = con.findDevice(device);

			if (dev != null) {
				chan = dev.findChannel(channel);

				if (chan != null) {
					dev.removeChannel(chan);
				}

				if (!dev.hasChannels()) {
					con.removeDevice(dev);
				}
			}
			if (!con.hasDevices()) {
				removeConnection(con);

			}
		}
	}

	private void removeConnection(Connection con) {
		connectionList.remove(con);
	}

	private void addConnection(Connection con) {
		connectionList.add(con);
	}

	private Connection findConnection(String interfaceId) {
		for (Connection con : connectionList) {
			if (con.getInterfaceId().equals(interfaceId))
				return con;
		}

		return null;
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

	/**
	 * Asynchronous read channel method - not mandatory
	 */
	@Override
	public void readChannels(List<SampledValueContainer> channels, ChannelUpdateListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Asynchronous write channel method - not mandatory
	 */
	@Override
	public void writeChannels(List<ValueContainer> channels, ExceptionListener listener)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void hardwareAdded(HardwareDescriptor descriptor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hardwareRemoved(HardwareDescriptor descriptor) {
		for (Connection con : connectionList) {
			if (con.getInterfaceId().equals(descriptor.getIdentifier())) {
				connectionList.remove(con);
				con.close();
				break;
			}
		}
	}

	public HardwareManager getHardwareManager() {
		return hardwareManager;
	}

}
