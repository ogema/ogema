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
