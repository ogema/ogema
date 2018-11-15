/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.driver.homematic;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.homematic.manager.LocalDevice;
import org.ogema.driver.homematic.usbconnection.UsbConnection;
import org.slf4j.Logger;

public class Connection {
	private final String interfaceId;
	private final String parameterString;
	private final Map<String, Device> devices;
	protected LocalDevice localDevice;
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
		connectUsb.setName("homematic-ll-connectUSB");
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

	public static String getPortName() {
		return "USB";
	}
}
