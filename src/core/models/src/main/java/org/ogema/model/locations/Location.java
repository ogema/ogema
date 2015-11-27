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
package org.ogema.model.locations;

import org.ogema.model.prototypes.Data;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Defines information about the physical location of a device or a sensor reading.
 */
public interface Location extends Data {

	/**
	 * Reference to the device that this object is attached to. Note that although rooms are also PhysicalElements, 
	 * the element "room" should be used if this information is given. If a hierarchy of objects exists that all
	 * would be suitable (room-workplace-device on workplace) this should reference the smallest unit in the
	 * hierarchy (in the example: the device on the workplace).
	 */
	PhysicalElement device();

	/** Room the device is located in. Also the location "outside a building" should be modeled as a room.
	 * Note that the room information might only be given by a higher level in the device hierarchy (see "device")
	 */
	Room room();

	/** Geographical position */
	GeographicLocation geographicLocation();

}
