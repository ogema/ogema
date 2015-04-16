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
 * A Exception thrown if Device don't exist
 */
public class NoSuchDeviceException extends Exception {

	private static final long serialVersionUID = 3367332317411774073L;

	private DeviceLocator deviceLocator = null;

	/**
	 * Constuctor
	 * 
	 * @param deviceLocator
	 */
	public NoSuchDeviceException(DeviceLocator deviceLocator) {
		this.deviceLocator = deviceLocator;
	}

	/**
	 * 
	 * @return deviceLocator
	 */
	public DeviceLocator getDeviceLocator() {
		return deviceLocator;
	}
}
