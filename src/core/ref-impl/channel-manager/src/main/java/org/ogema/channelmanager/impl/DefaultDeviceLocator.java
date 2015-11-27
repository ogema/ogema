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
package org.ogema.channelmanager.impl;

import java.util.Objects;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class DefaultDeviceLocator implements DeviceLocator {

	private final String driverName;
	private final String interfaceName;
	private final String deviceAddress;
	private String parameters;

	public DefaultDeviceLocator(String driverName, String interfaceName, String deviceAddress, String parameters) {
		this.driverName = driverName;
		this.interfaceName = interfaceName;
		this.deviceAddress = deviceAddress;
		this.parameters = parameters;
	}

	@Override
	public String getDriverName() {
		return driverName;
	}

	@Override
	public String getInterfaceName() {
		return interfaceName;
	}

	@Override
	public String getDeviceAddress() {
		return deviceAddress;
	}

	@Override
	public String getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof DeviceLocator) {
			DeviceLocator otherLocator = (DeviceLocator) other;

			if (!otherLocator.getDriverName().equals(this.driverName)) {
				return false;
			}
			if (!otherLocator.getInterfaceName().equals(this.interfaceName)) {
				return false;
			}
			if (!otherLocator.getDeviceAddress().equals(this.deviceAddress)) {
				return false;
			}
			if (otherLocator.getParameters() != null && this.parameters != null
					&& !otherLocator.getParameters().equals(this.parameters)) {
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 73 * hash + Objects.hashCode(this.driverName);
		hash = 73 * hash + Objects.hashCode(this.interfaceName);
		hash = 73 * hash + Objects.hashCode(this.deviceAddress);
		return hash;
	}

	@Override
	public String toString() {
		return driverName + ":" + interfaceName + ":" + deviceAddress;
	}

}
