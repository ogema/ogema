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
package org.ogema.model.locations;

import org.ogema.model.prototypes.Data;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Defines information about the physical location of a device or a sensor reading.
 */
public interface Location extends Data {

	/**
	 * Reference to the device that this object is attached to. Note that rooms are also PhysicalElements, so
	 * "device" can also be the room that this device is located in. If a hierarchy of objects exists that all
	 * would be suitable (room->workplace->device on workplace) this should reference the smallest unit in the
	 * hierarchy (in the example: the device on the workplace).
	 */
	PhysicalElement device();

	/** Geographical position */
	GeographicLocation geographicLocation();
}
