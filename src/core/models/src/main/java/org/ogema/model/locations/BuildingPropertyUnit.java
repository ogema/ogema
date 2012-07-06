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

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.devices.connectiondevices.HeatConnectionBox;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Property unit, for example a flat within a residential building or shop within a mall. A separate BuildingPropertyUnit
 * resource may be used to represent the spaced commonly used by all property units of the building
 * ("virtual building property unit"). The building this belongs to can be inferred from the
 * {@link #location()} of this resource.
 */
public interface BuildingPropertyUnit extends PhysicalElement {
	//	/** Reference to building in which property unit is situated */
	//	Building building();

	/**
	 * Electrical connection cabinet of property unit including (sub-)distribution cabinet providing the different
	 * electrical circuits of the property units
	 */
	ElectricityConnectionBox electricityConnectionBox();

	/** Heating connection point for the building property unit. */
	HeatConnectionBox heatConnectionBox();

	/**
	 * Type of building property unit:<br>
	 * 1: unit intended for a private household<br>
	 * 2: office space<br>
	 * 3: salesroom / shopping store (might include workshop/production space)<br>
	 * 4: pure workshop / production space<br>
	 * 9999: other >=10.000: custom values
	 */
	IntegerResource type();

	/**
	 * List of the rooms in this unit.
	 */
	ResourceList<Room> rooms();
}
