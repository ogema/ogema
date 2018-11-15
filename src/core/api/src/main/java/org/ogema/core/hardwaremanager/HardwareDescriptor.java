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
