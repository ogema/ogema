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

import java.util.ArrayList;
import java.util.List;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.util.SerialParameters;

import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.slf4j.LoggerFactory;

/**
 * This class manages the driver data for an open interface
 * 
 * @author pau
 * 
 */
public class Connection {
	private final String interfaceId;
	private SerialConnection con;
	private ModbusSerialTransaction transaction;
	private final List<Device> devices;
	private final SerialParameters params;
	private final String parameterString;
	private final ModbusDriver driver;

	public Connection(ModbusDriver driver, String iface, String parameter) {
        this.driver = driver;
		devices = new ArrayList<>();

		interfaceId = iface;

		parameterString = parameter;
		params = parseParameterString(parameter);
		//params.setPortName(getPortName(iface));
		params.setPortName(iface);
		params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
		con = new SerialConnection(params);
		try {
			con.open();
		} catch (Exception e) {
			LoggerFactory.getLogger(ModbusDriver.class).error("opening connection failed", e);
		}
		transaction = new ModbusSerialTransaction(con);
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

	public String getParameterString() {
		return parameterString;
	}

	/**
	 * Parse the parameter string for the modbus connection
	 * 
	 * @param parameter
	 * @return
	 */
	public final SerialParameters parseParameterString(String parameter) {
		// baudRate:databits:parity:stopbits:flowcontrol:echo:timeoutMs
		SerialParameters newParams = new SerialParameters();
		String[] splitted = parameter.split(":");
		newParams.setBaudRate(Integer.parseInt(splitted[0], 10));
		newParams.setDatabits(Integer.parseInt(splitted[1], 10));
		newParams.setParity(splitted[2]);
		newParams.setStopbits(splitted[3]);
		newParams.setFlowControlIn(splitted[4]);
		newParams.setFlowControlOut(splitted[5]);
		newParams.setEcho(Integer.parseInt(splitted[6], 10) != 0);
		newParams.setReceiveTimeout(Integer.parseInt(splitted[7], 10));

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
		result += ":" + params.getReceiveTimeout();

		return result;
	}

	// private int parseFlowcontrol(String input) throws Exception {
	// switch (input) {
	// case "NO":
	// return SerialPort.FLOWCONTROL_NONE;
	// case "RTSCTS":
	// return SerialPort.FLOWCONTROL_RTSCTS_IN |
	// SerialPort.FLOWCONTROL_RTSCTS_OUT;
	// case "XONXOFF":
	// return SerialPort.FLOWCONTROL_XONXOFF_IN |
	// SerialPort.FLOWCONTROL_XONXOFF_OUT;
	// default:
	// throw new Exception("Unknown Flowcontrol: " + input);
	// }
	// }
	//
	// private int parseParity(String input) throws Exception {
	// switch (input) {
	// case "N":
	// return SerialPort.PARITY_NONE;
	// case "E":
	// return SerialPort.PARITY_EVEN;
	// case "O":
	// return SerialPort.PARITY_ODD;
	// case "M":
	// return SerialPort.PARITY_MARK;
	// case "S":
	// return SerialPort.PARITY_SPACE;
	// default:
	// throw new Exception("Unknown Parity: " + input);
	// }
	// }

	public String getInterfaceId() {
		return interfaceId;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public List<DeviceLocator> getDeviceLocators() {
		List<DeviceLocator> result = new ArrayList<>(devices.size());

		for (Device dev : devices) {
			result.add(dev.getDeviceLocator());
		}

		return result;
	}

	public Device findDevice(DeviceLocator device) {
		for (Device dev : devices) {
			if (dev.getDeviceLocator().equals(device))
				return dev;
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

	synchronized public ModbusResponse executeTransaction(ModbusRequest request) throws Exception {

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

	synchronized public void close() {
		// transaction.setSerialConnection(null);
		con.close();
		con = null;
		transaction = null;
	}
}
