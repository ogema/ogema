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
package org.ogema.core.hardwaremanager;

import java.util.Collection;

/**
 * This class provides information about hardware connected to the system. The hardware management is informative,
 * actual claiming and opening of hardware is done in the respective APIs (i.e. rxtx).
 * 
 * Devices are connected to the to the Ogema gateway mainly via USB. But there are other devices which could be
 * connected via digital IO or native UARTs.
 * 
 * A connected device is unambiguously identified in the system with an identifier string. The format of the identifier
 * string varies according to the connection type (USB, Serial, digitalIO). The first part of an identifier string is
 * the type of the connection (e.g. usb, serial, io).
 * 
 * --- USB --- An USB identifier string consists of the vendor and product id of the device, the (optional) serial
 * number of the device, the configuration and interface and the physical path to the device (root hub port on hub port
 * on hub device). Example: a FTDI serial adapter connected to port 1 of an intermediate hub, connected to root hub 1
 * "usb:1.1:1.0:0x0403:0x06001:FTU74ZQE" "connection type: physical bus location: vendor id: product id: serial number"
 * 
 * --- Serial --- A serial identifier string simply list the number/name of the port: "serial:1" "connection type: port
 * number"
 * 
 * --- Digital IO --- A digitalIO identifier string has the format: "digitalIO:in:portID:pinID"
 * 
 * The identifier string is used when identifying hardware and requesting hardware access.
 * 
 * Additional Information is available in HardwareDescriptors that contain additional information according to the
 * connection type of the device. The most important information would be the port name (Windows) or device file (Linux)
 * of the device.
 * 
 * When the system boots, all devices are presented to the Framework. If a OGEMA bus system driver is installed, all
 * available device descriptions are presented so that a descriptor can be chosen. The administrator can associate the
 * OGEMA driver with a device. These associations are stored persistently and compared after each reboot with the
 * existing hardware configuration and updated if necessary.
 * 
 * If a device is removed or added new one, the table of devices is updated so that it constantly reflects the current
 * state of the hardware configuration.
 */
public interface HardwareManager {

	/**
	 * Gets a list of identifier strings for all currently active hardware.
	 */
	Collection<String> getHardwareIdentifiers();

	/**
	 * Gets a List of {@link HardwareDescriptor}s for all currently active hardware.
	 */
	Collection<HardwareDescriptor> getHardwareDescriptors();

	/**
	 * Gets the {@link HardwareDescriptor} for the hardware with the identifier string.
	 */
	HardwareDescriptor getDescriptor(String identifier);

	/**
	 * Add a {@link HardwareListener} that is called every time the hardware configuration changes. The listener is
	 * called for every added or removed hardware.
	 */
	void addListener(HardwareListener listener);

	/**
	 * Remove a listener from the global list.
	 */
	void removeListener(HardwareListener listener);

	/**
	 * Get a collection of all descriptors that match the given regex pattern. This can be used to look for descriptors
	 * ending with a specific combination of product and vendor ids. For example the pattern ".+:0403:6001:" gets all
	 * descriptors that end with ":0403:6001:".
	 * 
	 * @param pattern
	 *            The regex pattern.
	 * @return Collection of the descriptors.
	 */
	public Collection<HardwareDescriptor> getHardwareDescriptors(String pattern);

	/**
	 * Looks for a port after the specified parameter. The method firstly looks for a port it'S name is specified as the
	 * value of the system property portNameProp. In case of no success a port is searched its descriptor maps to
	 * descriptorRegEx. If the second try is unsuccessful too, the specified {@link HardwareListener} list is
	 * registered.
	 * 
	 * @param portNameProp
	 *            Name String of the system property its value contains the port name.
	 * @param descriptorRegEx
	 *            Regular expression that should map the ports descriptor string.
	 * @param list
	 *            {@link HardwareListener} instance that is registered only if no port is found.
	 * @return
	 */
	public String getPortName(String portNameProp, String descriptorRegEx, HardwareListener list);
}
