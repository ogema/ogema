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
package org.ogema.hardwaremanager.api;

import java.util.Map;

/**
 * Interface used by {@link org.ogema.hardwaremanager.impl.HardwareManagerImpl} to access the system-dependent
 * functions.
 */
public interface NativeAccess {

	/**
	 * Returns the native handles and types of all current hardware.
	 */
	Container[] getHandles();

	/**
	 * Returns the next native handle and event type. This is called from the event thread. Gets passed and returns a
	 * container for native handle and event type. Blocks until an event occurs.
	 */
	Container getEvent(Container container);

	/**
	 * Returns the ID String of the device.
	 * 
	 * @param handle
	 *            opaque handle
	 */
	String getIdString(Object handle);

	/**
	 * Returns the Port String of the device, if any
	 * 
	 * @param handle
	 *            opaque handle
	 */
	String getPortString(Object handle);

	/**
	 * Returns a Map of usb properties
	 * 
	 * @param handle
	 *            opaque handle
	 */
	Map<String, String> getUsbInfo(Object handle);

	/**
	 * If the hardware manager is stopped this method is called to free the event thread from the native side.
	 */
	void unblock();

}
