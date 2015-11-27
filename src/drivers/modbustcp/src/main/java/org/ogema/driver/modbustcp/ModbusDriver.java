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
package org.ogema.driver.modbustcp;

import java.io.IOException;
import java.util.ArrayList;
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
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.modbustcp.ModbusChannel.EAccess;
import org.ogema.driver.modbustcp.enums.EDatatype;

/**
 * This is a wrapper for the jamod api (http://jamod.sourceforge.net)
 * 
 * On every channelAdded() a check for an existing connection is progressed. If the channel is related to a non-existing
 * connection, it will be created.
 * 
 * This driver offers Modbus TCP master transfers.
 * 
 * The following four basic channel addressing modes are supported by this driver:
 * 
 * - access 16bit register(s) called Input Registers (read only) "INPUT_REGISTERS:REGISTERNUMBER:REGISTERDATATYP"
 * 
 * - access 16bit register(s) called Holding Registers (read / write) "HOLDING_REGISTERS:REGISTERNUMBER:REGISTERDATATYP"
 * 
 * - access 1bit called Discrete Input (read only) "DISCRETE_INPUTS:REGISTERNUMBER:REGISTERDATATYP"
 * 
 * - access 1bit called Coils (read / write) "COILS:REGISTERNUMBER:REGISTERDATATYP"
 * 
 * @author pau
 * 
 */
@Component(immediate = false)
@Service(ChannelDriver.class)
public class ModbusDriver implements ChannelDriver {

	private static final String DRIVER_ID = "modbus-tcp";
	private static final String DESCRIPTION = "MODBUS TCP via Ethernet";

	private final List<Connection> connectionList;
	private int splitNumber = 0;

	public ModbusDriver() {
		connectionList = new ArrayList<Connection>();
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
				Connection con = findConnection(channelLocator.getDeviceLocator().getDeviceAddress());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				Channel channel = dev.findChannel(channelLocator);

				// set access flag
				channel.update(EAccess.READ);

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
				Connection con = findConnection(channelLocator.getDeviceLocator().getDeviceAddress());
				Device dev = con.findDevice(channelLocator.getDeviceLocator());
				Channel channel = dev.findChannel(channelLocator);

				// set access flag
				channel.update(EAccess.WRITE);

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
	public void shutdown() {
		for (Connection con : connectionList) {
			removeConnection(con);
			con.close();
		}
	}

	@Override
	public void channelAdded(ChannelLocator channel) {

		DeviceLocator device = channel.getDeviceLocator();
		String devAdress = device.getDeviceAddress();

		Connection con;
		Device dev;
		Channel chan;

		// Connection handling
		con = findConnection(devAdress);

		if (con == null) {
			con = new Connection(device.getDeviceAddress());
			addConnection(con);
		}

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

	public void readChannelGroup(Device device) {

		List<Channel> channels = device.getChannels();

		ArrayList<ModbusChannel> mods = new ArrayList<ModbusChannel>();

		for (Channel c : channels) {
			mods.add((ModbusChannel) c);
		}

		// ModbusChannelGroup channelGroup = new ModbusChannelGroup("test", mods);

	}

	@Override
	public void channelRemoved(ChannelLocator channel) {

		DeviceLocator device = channel.getDeviceLocator();
		String iface = device.getInterfaceName();

		Connection con = findConnection(iface);
		Device dev = con.findDevice(device);
		Channel chan = dev.findChannel(channel);

		// Channel handling
		dev.removeChannel(chan);

		// Device handling
		if (!dev.hasChannels()) {
			con.removeDevice(dev);
		}

		// Connection handling
		if (!con.hasDevices()) {
			removeConnection(con);
			con.close();
		}
	}

	private void removeConnection(Connection con) {
		connectionList.remove(con);
	}

	private void addConnection(Connection con) {
		connectionList.add(con);
	}

	private Connection findConnection(String devAdress) {
		for (Connection con : connectionList) {
			if (con.getDeviceAdress().equals(devAdress)) {
				return con;
			}
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
	 * Returns the right value of the register
	 */
	public Object getDataTypeValue(SampledValueContainer container) {

		ChannelLocator locator = container.getChannelLocator();
		String addressParams[] = locator.getChannelAddress().toUpperCase().split(":");

		splitNumber = 0;

		for (char c : locator.getChannelAddress().toCharArray()) {
			if (c == ':') {
				splitNumber++;
			}
		}

		if (addressParams[splitNumber].contains("BYTEARRAY")) {
			addressParams[splitNumber] = addressParams[splitNumber].substring(0, 9);
		}

		if (addressParams[splitNumber].contains("STRING")) {
			addressParams[splitNumber] = addressParams[splitNumber].substring(0, 6);
		}

		switch (addressParams[splitNumber]) {

		case "SHORT":
			return container.getSampledValue().getValue().getIntegerValue();

		case "FLOAT":
			return container.getSampledValue().getValue().getFloatValue();

		case "INT":
			return container.getSampledValue().getValue().getIntegerValue();

		case "DOUBLE":
			return container.getSampledValue().getValue().getDoubleValue();

		case "BOOLEAN":
			return container.getSampledValue().getValue().getBooleanValue();

		case "LONG":
			return container.getSampledValue().getValue().getLongValue();

		case "STRING":
			return container.getSampledValue().getValue().getStringValue();

		case "BYTEARRAY":
			return container.getSampledValue().getValue().getByteArrayValue();

		}
		return null;

	}

	/**
	 * Returns the string out of the byte[]
	 * 
	 * @param byteArray
	 * @return
	 */
	public String getStringFromBYTEARRAY(Object byteArray) {

		String result = new String((byte[]) byteArray);

		return result;

	}

	/**
	 * Prints the value, timestamp and quality of each container
	 * 
	 * @param channel
	 * @param container
	 */
	@SuppressWarnings("unused")
	private void print(Channel channel, SampledValueContainer container) {

		String addressParams[] = container.getChannelLocator().getChannelAddress().split(":");

		splitNumber = 0;

		for (char c : container.getChannelLocator().getChannelAddress().toCharArray()) {
			if (c == ':') {
				splitNumber++;
			}
		}

		String channelAddress = addressParams[splitNumber - 1];

		// if value = byte[] invoke getStringFromByteArray before printing
		if (channel.getDatatype().equals(EDatatype.BYTEARRAY)) {

			System.out.println();
			System.out.println("The value read from the channel " + channelAddress + " is: " + "\t"
					+ getStringFromBYTEARRAY(getDataTypeValue(container)));
			System.out.println("The timestamp read from the channel " + channelAddress + " is: " + "\t"
					+ container.getSampledValue().getTimestamp());
			System.out.println("The quality read from the channel is " + channelAddress + " is: " + "\t"
					+ container.getSampledValue().getQuality());

		}

		else {

			System.out.println();
			System.out.println("The value read from the channel " + channelAddress + " is: " + "\t"
					+ getDataTypeValue(container));
			System.out.println("The timestamp read from the channel " + channelAddress + " is: " + "\t"
					+ container.getSampledValue().getTimestamp());
			System.out.println("The quality read from the channel is " + channelAddress + " is: " + "\t"
					+ container.getSampledValue().getQuality());

		}

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
		try {
			Connection con = findConnection(channelLocator.getDeviceLocator().getDeviceAddress());
			Device dev = con.findDevice(channelLocator.getDeviceLocator());
			Channel channel = dev.findChannel(channelLocator);

			// set access flag
			channel.update(EAccess.WRITE);

			// write data
			channel.writeValue(con, value);

		} catch (NullPointerException e) {
			throw new IOException("Unknown channel: " + channelLocator, e);
		}
	}

}
