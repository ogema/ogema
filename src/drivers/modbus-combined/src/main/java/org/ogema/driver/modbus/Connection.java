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
package org.ogema.driver.modbus;

import java.io.IOException;
import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.util.SerialParameters;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;

/**
 * This class manages the driver data for an open interface. The interface can either be a serial port or a socket.
 * 
 * @author pau
 * 
 */
public class Connection {
	private final DeviceLocator locator;
	private final ModbusDriver driver;
	private final List<Channel> channels = new ArrayList<Channel>();

	// the different modbus connections don't share a base class
	private Object connection;

	private ModbusTransaction transaction;

	Connection(ModbusDriver driver, DeviceLocator locator) throws IOException {
		this.driver = driver;
		this.locator = locator;

		createTransaction(locator);
	}

	private void createTransaction(DeviceLocator locator) throws IOException {
		// find out what kind of connection it should be. RTU or TCP?

		// has interface name, is RTU
		if (locator.getInterfaceName() != null && !"".equals(locator.getInterfaceName())) {
			SerialParameters params;
			SerialConnection con;

			if (locator.getParameters() == null)
				throw new IOException("malformed DeviceLocator. getParameters() == 0 for RTU connection");

			params = parseParameterString(locator.getParameters());
			params.setPortName(locator.getInterfaceName());
			params.setEncoding(Modbus.SERIAL_ENCODING_RTU);

			con = new SerialConnection(params);

			try {
				con.open();
			} catch (Exception e) {
				throw new IOException("could not open serial connection. Original Message: " + e.getMessage(), e);
			}
			connection = con;

			// create transport
			transaction = new ModbusSerialTransaction(con);

		}
		// does not have an interface, must be TCP
		else if (locator.getDeviceAddress() != null) {

			String addr = locator.getDeviceAddress();
			final String[] parts = addr.split(":");

			// throws UnknownHostException which is a kind of IOException
			Object o = AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					TCPMasterConnection con;

					try {
						InetAddress ipAddr;
						ipAddr = InetAddress.getByName(parts[0]);

						con = new TCPMasterConnection(ipAddr);

						if (parts.length > 1) {
							int port;
							try {
								port = Integer.parseInt(parts[1]);
								con.setPort(port);
							} catch (NumberFormatException e) {
								throw new IOException(
										"malformed DeviceLocator. getDeviceAdress() returned contained illegal port number",
										e);
							}
						}

						con.connect();
					} catch (IOException e) {
						// forward IOException, no need to wrap it again
						return e;
					} catch (Exception e) {
						return new IOException("could not open tcp connection", e);
					}
					return con;
				}
			});
			if (o instanceof IOException)
				throw (IOException) o;
			connection = o;

			transaction = new ModbusTCPTransaction((TCPMasterConnection) o);

		}
		else {
			throw new IOException("malformed DeviceLocator. It has neither device address nor interface");
		}
	}

	private String getPortName(String iface) {
		String portName = null;
		HardwareManager hw = driver.getHardwareManager();
		HardwareDescriptor desc = hw.getDescriptor(iface);

		if (desc.getHardwareType() == HardwareType.USB) {
			portName = ((UsbHardwareDescriptor) desc).getPortName();
		}
		else if (desc.getHardwareType() == HardwareType.SERIAL) {
			portName = ((SerialHardwareDescriptor) desc).getPortName();
		}
		return portName;
	}

	DeviceLocator getDeviceLocator() {
		return locator;
	}

	/**
	 * Parse the parameter string for the modbus connection
	 * 
	 * @param parameter
	 * @return
	 * @throws IOException
	 */
	private SerialParameters parseParameterString(String parameter) throws IOException {

		SerialParameters newParams = new SerialParameters();

		// baudRate:databits:parity:stopbits:flowcontrol:echo:timeoutMs

		try {
			String[] splitted = parameter.split(":");

			if (splitted.length < 6)
				throw new IOException("malformed DeviceLocator. getParameters() has only " + splitted.length
						+ " elements (expected 6)");

			// NumberFormatException is a kind of IllegalArgumentException
			newParams.setBaudRate(Integer.parseInt(splitted[0], 10));
			newParams.setDatabits(Integer.parseInt(splitted[1], 10));
			newParams.setParity(splitted[2]);
			newParams.setStopbits(splitted[3]);
			newParams.setFlowControlIn(splitted[4]);
			newParams.setFlowControlOut(splitted[5]);
			newParams.setEcho(Integer.parseInt(splitted[6], 10) != 0);
			// newParams.setReceiveTimeout(Integer.parseInt(splitted[7], 10));

		} catch (NumberFormatException e) {
			throw new IOException("malformed DeviceLocator. getParameters() has malformed integer values", e);
		}

		return newParams;
	}

	static public String createParameterString(SerialParameters params) {
		String result;

		result = params.getBaudRateString();
		result += ":" + params.getDatabitsString();
		result += ":" + params.getParityString();
		result += ":" + params.getStopbitsString();
		result += ":" + params.getFlowControlInString();
		result += ":" + params.getFlowControlOutString();
		result += ":" + (params.isEcho() ? '1' : '0');
		// result += ":" + params.getReceiveTimeout();

		return result;
	}

	List<Channel> getChannels() {
		return channels;
	}

	List<ChannelLocator> getChannelLocators() {
		List<ChannelLocator> result = new ArrayList<>(channels.size());

		for (Channel chan : channels) {
			result.add(chan.getChannelLocator());
		}

		return result;
	}

	private Channel findChannel(ChannelLocator channel) {
		for (Channel chan : channels) {
			if (chan.getChannelLocator().equals(channel))
				return chan;
		}

		return null;
	}

	private void addChannel(Channel chan) {
		channels.add(chan);
	}

	synchronized void removeChannel(ChannelLocator locator) {

		Channel channel = findChannel(locator);

		if (channel != null)
			channels.remove(channel);
	}

	boolean hasChannels() {
		return !channels.isEmpty();
	}

	synchronized ModbusResponse executeTransaction(ModbusRequest request) throws Exception {

		// if each device should have its own parameter settings,
		// these are modified here before each transaction
		// - params.equals(device.params) <-- TODO: add function parameter
		// - modify params
		// - con.setConnectionParameters() re-reads the params (has internal
		// reference to it)

		transaction.setRequest(request);
		transaction.execute();
		return transaction.getResponse();
	}

	synchronized void close() {

		if (connection instanceof SerialConnection) {
			((SerialConnection) connection).close();
		}
		else if (connection instanceof TCPMasterConnection) {
			((TCPMasterConnection) connection).close();
		}

		connection = null;
		transaction = null;
	}

	synchronized Channel getChannel(ChannelLocator channelLocator) throws IOException {

		Channel channel = findChannel(channelLocator);

		if (channel == null) {
			channel = Channel.createChannel(channelLocator);
			addChannel(channel);
		}

		return channel;
	}
}
