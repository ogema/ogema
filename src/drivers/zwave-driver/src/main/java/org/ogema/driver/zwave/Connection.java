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
package org.ogema.driver.zwave;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.driver.zwave.manager.LocalDevice;
import org.slf4j.Logger;

/**
 * 
 * @author baerthbn
 * 
 */
public class Connection {
	private String interfaceId;
	private final Map<Short, Device> devices;
	private LocalDevice localDevice;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");
	private boolean hasConnection = false;
	private final Object connectionLock;

	public Connection(String portName, Object lock, HardwareManager hwMngr, ZWaveDriver driver) {
		this.connectionLock = lock;
		devices = new HashMap<>();
		// String portName = getPortName(hwMngr);
		localDevice = new LocalDevice(portName, driver);
		if (localDevice.isConnected()) {
			synchronized (connectionLock) {
				hasConnection = true;
			}
			interfaceId = portName;
		}
	}

	public Device findDevice(DeviceLocator device) {
		String params = device.getParameters();
		String id = params.substring(params.lastIndexOf(':') + 1, params.length());
		short deviceId = Short.valueOf(id);
		return devices.get(deviceId);
	}

	public void addDevice(Device dev) {
		devices.put(dev.getDeviceId(), dev);
		logger.info("Device added: " + dev.getDeviceId());
	}

	public LocalDevice getLocalDevice() {
		return localDevice;
	}

	public Map<Short, Device> getDevices() {
		return devices;
	}

	public void close() {
		localDevice.close();
	}

	public boolean hasConnection() {
		return hasConnection;
	}

	public String getInterfaceId() {
		return interfaceId;
	}
}
