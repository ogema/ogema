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

import org.ogema.core.model.ResourceList;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.devices.connectiondevices.HeatConnectionBox;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.OccupancySensor;

/**
 * Definition of a single building. Buildings contain {@link BuildingPropertyUnit}s
 * which contain {@link Room}s.
 */
public interface Building extends PhysicalElement {

	/**
	 * Electrical connection cabinet for entire building (including distribution cabinet towards building property units
	 * if applicable)
	 */
	ElectricityConnectionBox electricityConnectionBox();

	/** 
	 * Heating connection point for the entire building. 
	 */
	HeatConnectionBox heatConnectionBox();

	/**
	 * List of the building property units associated to this building.
	 */
	ResourceList<BuildingPropertyUnit> buildingPropertyUnits();

	/**
	 * Occupancy sensor for the entire building. If this does not exist
	 * the information may still be available via the occupancy sensors
	 * of the building property units contained or the sensors of the 
	 * rooms contained in them.
	 */
	OccupancySensor occupancySensor();
}
