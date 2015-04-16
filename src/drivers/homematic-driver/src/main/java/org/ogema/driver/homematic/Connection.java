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
package org.ogema.driver.homematic;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.homematic.manager.LocalDevice;
import org.ogema.driver.homematic.usbconnection.UsbConnection;
import org.slf4j.Logger;

public class Connection {
	private final String interfaceId;
	private final String parameterString;
	private final Map<String, Device> devices;
	protected LocalDevice localDevice;
	ApplicationManager applicationManager;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");
	private boolean hasConnection = false;
	private final Object connectionLock;

	public Connection(Object lock, String iface, String parameter) {
		this.connectionLock = lock;
		interfaceId = iface;
		parameterString = parameter;
		final UsbConnection usbConnection = new UsbConnection();

		Thread connectUsb = new Thread() {
			@Override
			public void run() {
				while (!hasConnection && Activator.bundleIsRunning) {
					if (usbConnection.connect()) {
						synchronized (connectionLock) {
							hasConnection = true;
							localDevice = new LocalDevice(parameterString, usbConnection);
							connectionLock.notify();
						}
					}
					try {
						Thread.sleep(Constants.CONNECT_WAIT_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		connectUsb.start();
		devices = new HashMap<>();
	}

	public Device findDevice(DeviceLocator device) {
		return devices.get(device.getDeviceAddress());
	}

	public void addDevice(Device dev) {
		devices.put(dev.getDeviceAddress(), dev);
		logger.info("Device added: " + dev.getDeviceAddress());
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

	public boolean hasConnection() {
		return hasConnection;
	}
}
