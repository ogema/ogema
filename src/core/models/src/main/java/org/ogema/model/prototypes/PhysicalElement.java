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
package org.ogema.model.prototypes;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.model.locations.PhysicalDimensions;
import org.ogema.model.locations.Location;

/**
 * Prototype to be extended by resources representing a physical element such as sensors, actors, devices, parts of
 * devices and buildings.
 */
public interface PhysicalElement extends Resource {

	/**
	 * Physical position of device (geographic coordinates, Room in which the element is situated, mobile information.
	 * Usually only set for the top-level resource as this is usually the same for all parts of a device.
	 */
	Location location();

	/**
	 * Heat capacity of the device. Technically, this can be defined for every
	 * physical element, which is why it is defined on this level. In practice it 
	 * will only be relevant for thermal 
	 * storage devices, either dedicated storages or effective storages like rooms.
	 */
	ThermalEnergyCapacityResource heatCapacity();

	/** 
	 * Information on the physical dimensions.
	 */
	PhysicalDimensions physDim();

	/**
	 * Human-readable name.
	 */
	StringResource name();
}
