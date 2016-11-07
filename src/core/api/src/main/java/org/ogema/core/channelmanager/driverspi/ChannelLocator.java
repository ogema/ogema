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
package org.ogema.core.channelmanager.driverspi;

/**
 * The unique description of a channel. 
 * This can be seen as the system wide unique address of a channel. 
 * It consists of a low-level driver specific channel address and a DeviceLocator instance.
 */
public final class ChannelLocator {

	private final String channelAddress;
	private final DeviceLocator deviceLocator;

	public ChannelLocator(String channelAddress, DeviceLocator deviceLocator) {
		
		if (deviceLocator == null)
			throw new NullPointerException();

		if (channelAddress == null)
			throw new NullPointerException();

		this.deviceLocator = deviceLocator;
		this.channelAddress = channelAddress;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public DeviceLocator getDeviceLocator() {
		return deviceLocator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channelAddress == null) ? 0 : channelAddress.hashCode());
		result = prime * result + ((deviceLocator == null) ? 0 : deviceLocator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChannelLocator other = (ChannelLocator) obj;
		if (channelAddress == null) {
			if (other.channelAddress != null)
				return false;
		} else if (!channelAddress.equals(other.channelAddress))
			return false;
		if (deviceLocator == null) {
			if (other.deviceLocator != null)
				return false;
		} else if (!deviceLocator.equals(other.deviceLocator))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return deviceLocator.toString() + ":" + channelAddress;
	}
}
