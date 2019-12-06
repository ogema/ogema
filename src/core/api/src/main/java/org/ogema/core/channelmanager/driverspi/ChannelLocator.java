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
package org.ogema.core.channelmanager.driverspi;

/**
 * The unique description of a channel. This can be seen as the system wide unique address of a channel. It consists of
 * a low-level driver specific channel address and a DeviceLocator instance.
 */
public final class ChannelLocator {

	private final String channelAddress;
	private final DeviceLocator deviceLocator;
	private String locatorString;

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
		}
		else if (!channelAddress.equals(other.channelAddress))
			return false;
		if (deviceLocator == null) {
			if (other.deviceLocator != null)
				return false;
		}
		else if (!deviceLocator.equals(other.deviceLocator))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (locatorString == null)
			locatorString = deviceLocator.toString() + ":" + channelAddress;
		return locatorString;
	}
}
