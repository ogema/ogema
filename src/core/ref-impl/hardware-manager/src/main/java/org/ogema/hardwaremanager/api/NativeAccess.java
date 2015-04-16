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
package org.ogema.hardwaremanager.api;

import java.util.Map;

/**
 * Interface used by {@link org.ogema.core.hardwaremanager.impl.HardwareManagerImpl} to access the system-dependent
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
