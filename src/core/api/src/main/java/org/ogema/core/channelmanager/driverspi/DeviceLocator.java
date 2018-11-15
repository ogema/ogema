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

public final class DeviceLocator {

	private final String driverName;
	private final String interfaceName;
	private final String deviceAddress;
	private final String parameters;

	public DeviceLocator(String driverName, String interfaceName, String deviceAddress, String parameters) {
		
		if (driverName == null)
			throw new NullPointerException();
		
		if (interfaceName == null)
			throw new NullPointerException();
		
		if (deviceAddress == null)
			throw new NullPointerException();

		// parameters can be null
		
		this.driverName = driverName;
		this.interfaceName = interfaceName;
		this.deviceAddress = deviceAddress;
		this.parameters = parameters;
	}

	/**
	 * Returns the driver name. 
	 * The driver name is a unique ID of a driver (e.g. "modbus-tcp").
	 * 
	 */
	public String getDriverName() {
		return driverName;
	}

	/**
	 * Specifies the address of the local interface the device is connected. 
	 * Is usually used for serial communication.
	 * Examples are /dev/ttyS0 (linux) and COM1 (windows).
	 * 
	 * @return the address of the local interface as a low-level driver specific string.
	 */
	public String getInterfaceName() {
		return interfaceName;
	}

	/**
	 * The device address contains all required informations to address a certain device. 
	 * The format of the device address is a low-level driver specific string. 
	 * (e.g. for Modbus/TCP the required information is usually the IP address and a port number. 
	 * This information can be given by the string "10.0.0.3:502" indicating that there is a
	 * device with IP address 10.0.0.3 and a Modbus/TCP server listening on TCP port 502).
	 * 
	 * @return the device address as a low-level driver specific string
	 */
	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * The parameter string contains required auxiliary information. 
	 * (e.g. serial parameters or authentication values.)
	 * The format of parameter is a low-level driver specific string.
	 * 
	 * @return the parameters as a low-level driver specific string
	 */
	public String getParameters() {
		return parameters;
	}

	public String toString() {
		return driverName + ":" + interfaceName + ":" + deviceAddress + ":" + parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deviceAddress == null) ? 0 : deviceAddress.hashCode());
		result = prime * result + ((driverName == null) ? 0 : driverName.hashCode());
		result = prime * result + ((interfaceName == null) ? 0 : interfaceName.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
		DeviceLocator other = (DeviceLocator) obj;
		if (deviceAddress == null) {
			if (other.deviceAddress != null)
				return false;
		} else if (!deviceAddress.equals(other.deviceAddress))
			return false;
		if (driverName == null) {
			if (other.driverName != null)
				return false;
		} else if (!driverName.equals(other.driverName))
			return false;
		if (interfaceName == null) {
			if (other.interfaceName != null)
				return false;
		} else if (!interfaceName.equals(other.interfaceName))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}
}
