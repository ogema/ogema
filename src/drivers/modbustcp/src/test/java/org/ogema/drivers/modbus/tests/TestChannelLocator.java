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
package org.ogema.drivers.modbus.tests;

import java.util.Objects;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class TestChannelLocator implements ChannelLocator {

	private final String channelAddress;
	private final DeviceLocator deviceLocator;

	public TestChannelLocator(DeviceLocator deviceLocator, String channelAddress) {
		this.deviceLocator = deviceLocator;
		this.channelAddress = channelAddress;
	}

	@Override
	public String getChannelAddress() {
		return this.channelAddress;
	}

	@Override
	public DeviceLocator getDeviceLocator() {
		return this.deviceLocator;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ChannelLocator) {
			ChannelLocator otherLocator = (ChannelLocator) other;

			if (!otherLocator.getChannelAddress().equals(this.channelAddress)) {
				return false;
			}
			if (!otherLocator.getDeviceLocator().equals(this.deviceLocator)) {
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 41 * hash + Objects.hashCode(this.channelAddress);
		hash = 41 * hash + Objects.hashCode(this.deviceLocator);
		return hash;
	}

	@Override
	public String toString() {
		return deviceLocator.toString() + ":" + channelAddress;
	}

}
