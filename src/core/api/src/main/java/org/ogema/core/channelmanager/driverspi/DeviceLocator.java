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
 * object provided by framework
 * 
 */
public interface DeviceLocator {

	/**
	 * Returns the driver name. The driver name is a unique ID of a driver (e.g. "modbus-tcp").
	 * 
	 * @return
	 */
	String getDriverName();

	/**
	 * Specifies the address of the local interface the device is connected. Is usually used for serial communication.
	 * Examples are /dev/ttyS0 (linux) and COM1 (windows).
	 * 
	 * @return the address of the local interface as a low-level driver specfic string.
	 */
	String getInterfaceName();

	/**
	 * The device address contains all required informations to address a certain device. The format of the device
	 * address is a low-level driver specific string. (e.g. for Modbus/TCP the required information is usually the IP
	 * address and a port number. This information can be given by the string "10.0.0.3:502" indicating that there is a
	 * device with IP address 10.0.0.3 and a Modbus/TCP server listening on TCP port 502).
	 * 
	 * @return the device address as a low-level driver specific string
	 */
	String getDeviceAddress();

	String getParameters(); /* e.g. com parameters, authentication values ... */

	void setParameters(String params);
}
