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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * This class manages the driver data for an open interface
 * 
 * @author pau
 * 
 */
public class Connection extends ModbusConnection {

	private final String deviceAddress;
	private String slaveAddress;
	private int port;
	private InetAddress slave;
	private TCPMasterConnection con;
	private ModbusTCPTransaction transaction;
	private ModbusDriverUtil util;

	private final List<Device> devices;

	public Connection(String devAddress) {

		super();
		devices = new ArrayList<Device>();
		deviceAddress = devAddress;
		setAddress(devAddress);

		try {

			slave = InetAddress.getByName(slaveAddress);
			con = new TCPMasterConnection(slave);
			con.setPort(port);

			util = new ModbusDriverUtil();

		} catch (UnknownHostException e) {

			throw new RuntimeException(e.getMessage());

		}
		try {

			connect();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<Device> getDevices() {
		return devices;
	}

	public String getDeviceAdress() {
		return deviceAddress;
	}

	public List<DeviceLocator> getDeviceLocators() {
		List<DeviceLocator> result = new ArrayList<DeviceLocator>(devices.size());

		for (Device dev : devices) {
			result.add(dev.getDeviceLocator());
		}

		return result;
	}

	public Device findDevice(DeviceLocator device) {
		for (Device dev : devices) {
			if (dev.getDeviceLocator().equals(device)) {
				return dev;
			}
		}

		return null;
	}

	public void addDevice(Device dev) {
		devices.add(dev);
	}

	public void removeDevice(Device dev) {
		devices.remove(dev);
	}

	public boolean hasDevices() {
		return !devices.isEmpty();
	}

	synchronized public void close() {
		transaction.setConnection(null);
		con.close();
		con = null;
		transaction = null;
	}

	private void setAddress(String devAddress) {

		String[] address = devAddress.toLowerCase().split(":");

		slaveAddress = null;
		port = Modbus.DEFAULT_PORT;

		if (address.length == 1) {
			slaveAddress = address[0];
		}
		else if (address.length == 2) {
			slaveAddress = address[0];
			port = Integer.parseInt(address[1]);
		}
		else {
			throw new RuntimeException("Invalid device address: '" + deviceAddress
					+ "'! Use following format: [ip:port] like localhost:1502 or 127.0.0.1:1502");
		}
	}

	@Override
	public void connect() throws Exception {

		if (con != null && !con.isConnected()) {

			// finally connect to slave
			con.connect();

			// after connection established create a transaction object to handle request/response
			transaction = new ModbusTCPTransaction(con);

			setTransaction(transaction);

			if (!con.isConnected()) {
				throw new Exception("unable to connect");
			}
		}
	}

	// invokes the concerning function of the upper class based on
	// the function code read from the channel object
	public Value readChannel(ModbusChannel channel) throws ModbusException {

		Value value = null;

		switch (channel.getFunctionCode()) {
		case FC_01_READ_COILS:
			value = util.getBitVectorsValue(this.readCoils(channel));
			break;
		case FC_02_READ_DISCRETE_INPUTS:
			value = util.getBitVectorsValue(this.readDiscreteInputs(channel));
			break;
		case FC_03_READ_HOLDING_REGISTERS:
			value = util.getRegistersValue(this.readHoldingRegisters(channel), channel);
			break;
		case FC_04_READ_INPUT_REGISTERS:
			value = util.getRegistersValue(this.readInputRegisters(channel), channel);
			break;
		default:
			throw new RuntimeException("FunctionCode " + channel.getFunctionCode() + " not supported yet");
		}

		return value;
	}

	public void writeChannel(ModbusChannel channel, Value value) throws ModbusException, RuntimeException {

		switch (channel.getFunctionCode()) {
		case FC_05_WRITE_SINGLE_COIL:
			writeSingleCoil(channel, value.getBooleanValue());
			break;
		case FC_15_WRITE_MULITPLE_COILS:
			writeMultipleCoils(channel, util.getBitVectorFromByteArray(value));
			break;
		case FC_06_WRITE_SINGLE_REGISTER:
			writeSingleRegister(channel, new SimpleRegister(value.getIntegerValue()));
			break;
		case FC_16_WRITE_MULTIPLE_REGISTERS:
			writeMultipleRegisters(channel, util.valueToRegisters(value, channel.getDatatype()));
			break;
		default:
			throw new RuntimeException("FunctionCode " + channel.getFunctionCode().toString() + " not supported yet");
		}
	}

	@Override
	public void disconnect() {

	}

}
