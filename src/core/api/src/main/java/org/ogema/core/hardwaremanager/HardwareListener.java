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
 * This listener is called when hardware is added or removed.
 * 
 * This listener can be added to a global list that is notified for every device change, or to a specific list for each
 * Descriptor.
 */
public interface HardwareListener {

	/**
	 * Callback method that signals that a new hardware is added to the system.
	 * 
	 * @param descriptor
	 *            The descriptor object of the of the added hardware.
	 */
	void hardwareAdded(HardwareDescriptor descriptor);

	/**
	 * Callback method that signals that a new hardware is removed from the system.
	 * 
	 * @param descriptor
	 *            The descriptor object of the of the removed hardware.
	 */
	void hardwareRemoved(HardwareDescriptor descriptor);
}
