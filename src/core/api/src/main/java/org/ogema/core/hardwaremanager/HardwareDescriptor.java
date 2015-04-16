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
package org.ogema.core.hardwaremanager;

/**
 * A HardwareDescriptor offers information about a device connected to the system.
 * 
 */
public interface HardwareDescriptor {

	/**
	 * Encodes the sub-classes of HardwareDescriptor, so that a calling class need not parse the hardware identifier
	 * string just to get the type of device.
	 */
	enum HardwareType {
		USB, SERIAL, GPIO
	};

	/**
	 * Gets the hardware identifier string of the descriptor.
	 */
	String getIdentifier();

	/**
	 * Gets the type of the descriptor. Can be used to cast the descriptor to one of its sub-classes.
	 * 
	 * USB : {@link UsbHardwareDescriptor}, SERIAL : {@link SerialHardwareDescriptor}, GPIO :
	 * {@link GpioHardwareDescriptor}
	 */
	HardwareType getHardwareType();

	/**
	 * Add a {@link HardwareListener} that is only called for changes regarding to this descriptor, mostly removal. This
	 * listener is independent of {@link HardwareManager#addListener}
	 */
	void addListener(HardwareListener listener);

	void removeListener(HardwareListener listener);

	/**
	 * Check if the device related to this descriptor is still connected to the host system.
	 * 
	 * @return true if the device still connected, false otherwise.
	 */
	boolean isAlive();
}
