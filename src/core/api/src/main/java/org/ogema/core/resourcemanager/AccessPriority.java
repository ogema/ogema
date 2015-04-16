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
package org.ogema.core.resourcemanager;

/** Priority of write access */
public enum AccessPriority {
	/**
	 * highest priority (reserved for communication systems providing data from device and framework)
	 */
	PRIO_HIGHEST(0),

	/**
	 * highest application priority: restricted to safety-related applications preventing damage to life
	 */
	PRIO_LIFERELATED(1),

	/**
	 * restricted to safety-related applications preventing damage to devices
	 */
	PRIO_PHYSICALRELATED(2),

	/**
	 * restricted to applications requesting access to devices to stabilize a larger system, the electric grid etc
	 */
	PRIO_GRIDSTABILISATION(3),

	/**
	 * restricted to device type and manufacturer specific application<br>
	 */
	PRIO_DEVICESPECIFIC(4),

	/**
	 * applications performing specific management for a group of devices<br>
	 */
	PRIO_DEVICEGROUPMAN(6),

	/**
	 * applications performing a generic management that shall be applied when no device-specific management is
	 * available
	 */
	PRIO_GENERICMANAGEMENT(8),
	/**
	 * lowest priority (should be used when write access should only be given if no other applications wants to write)
	 */
	PRIO_LOWEST(10);

	private AccessPriority(int priority) {
		this.priority = priority;
	}

	private final int priority;

	/**
	 * Gets the current priority set.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * True if the priority object from which the method is called has a higher relevance than the argument prio.
	 */
	public boolean hasHigherPriorityThan(AccessPriority prio) {
		if (priority < prio.getPriority()) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * True if the priority object from which the method is called has a lower relevance than the argument prio.
	 */
	public boolean hasLowerPriorityThan(AccessPriority prio) {
		if (priority > prio.getPriority()) {
			return true;
		}
		else {
			return false;
		}
	}
}
