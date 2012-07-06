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
package org.ogema.core.hardwaremanager;

import java.util.Map;

/**
 * The {@link HardwareDescriptor} subclass for USB Devices.
 */
public interface UsbHardwareDescriptor extends HardwareDescriptor {

	/**
	 * Returns the port name (COMx) or device node (/dev/ttyUSBx) of the device.
	 */
	String getPortName();

	/**
	 * Returns additional information about the usb device. This contains for example the manufacturer string
	 * ("iManufacturer"="FTDI") or the product string ("iProduct"="USB to RS232 Adapter") as contained in the USB
	 * Device Descriptor.
	 * 
	 */
	Map<String, String> getInfo();
}
