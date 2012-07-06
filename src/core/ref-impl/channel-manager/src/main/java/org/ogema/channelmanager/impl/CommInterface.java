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
package org.ogema.channelmanager.impl;

import java.util.LinkedList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class CommInterface {

	private class CommDevice {
		public CommDevice(DeviceLocator device) {
			this.channels = new LinkedList<ChannelLocator>();
			this.device = device;
		}

		public DeviceLocator device;
		public List<ChannelLocator> channels;
	}

	private final ChannelDriver driver;
	private final String interfaceName;
	private final List<CommDevice> devices;

	public CommInterface(ChannelDriver driver, String interfaceName) {
		this.driver = driver;
		this.interfaceName = interfaceName;

		this.devices = new LinkedList<CommDevice>();
	}

	public void startReading() {
		// connect on "startReading" - reconnect on error/timeout
	}

	public void stopReading() {

	}

	public boolean addChannel(ChannelLocator channel) {
		if (doesDriverAndInterfaceMatch(channel)) {

			CommDevice device = getKnownDevice(channel);

			if (device != null) {
				if (hasChannel(channel)) {
					return false;
				}
			}
			else {
				device = new CommDevice(channel.getDeviceLocator());

				devices.add(device);
			}

			device.channels.add(channel);

			return true;
		}

		return false;
	}

	public boolean hasChannel(ChannelLocator channel) {
		CommDevice device = getKnownDevice(channel);

		if (device != null) {
			for (ChannelLocator channelLocator : device.channels) {
				if (channelLocator.equals(channel)) {
					return true;
				}
			}

			return false;
		}

		return false;
	}

	private CommDevice getKnownDevice(ChannelLocator channel) {
		for (CommDevice device : devices) {
			if (device.device.equals(channel.getDeviceLocator())) {
				return device;
			}
		}

		return null;
	}

	private boolean doesDriverAndInterfaceMatch(ChannelLocator channel) {
		DeviceLocator deviceLocator = channel.getDeviceLocator();

		if (deviceLocator.getDriverName().equals(driver.getDriverId())) {
			if (deviceLocator.getInterfaceName().equals(interfaceName)) {
				return true;
			}
		}

		return false;
	}

}
