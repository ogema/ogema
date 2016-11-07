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
package org.ogema.driver.xbee;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jssc.SerialPortException;

import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.xbee.manager.LocalDevice;
import org.slf4j.Logger;

public class Connection {
	private final String interfaceId;
	private Map<String, Device> devices;
	protected final LocalDevice localDevice; // The endpoint connected to the serialPort
	private LocalDevice temp;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");
	private final XBeeDriver xbeeDriver;

	public Connection(String iface, XBeeDriver xbeeDriver) throws SerialPortException {
		this.xbeeDriver = xbeeDriver;
		interfaceId = iface;
		devices = new HashMap<String, Device>();

		try {
			temp = new LocalDevice(interfaceId, this);
		} catch (SerialPortException e) {
			logger.info("Serial connection couldn't be established: " + e.getExceptionType());
			temp = null;
			throw e;
		}
		localDevice = temp;
	}

	public Device findDevice(DeviceLocator device) {
		return devices.get(device.getDeviceAddress());
	}

	public void addDevice(Device dev) {
		devices.put(dev.getDeviceAddress(), dev);
		logger.info("Device added: " + dev.getDeviceAddress());
	}

	public void removeDevices(long address64Bit) {
		Iterator<Map.Entry<String, Device>> iter = devices.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Device> entry = iter.next();
			if (entry.getValue().getAddress64Bit() == address64Bit) {
				entry.getValue().removeChannels();
				iter.remove();
			}
		}
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public LocalDevice getLocalDevice() {
		return localDevice;
	}

	public Map<String, Device> getDevices() {
		return devices;
	}

	public void close() {
		localDevice.close();
	}

	public XBeeDriver getXBeeDriver() {
		return xbeeDriver;
	}
}
