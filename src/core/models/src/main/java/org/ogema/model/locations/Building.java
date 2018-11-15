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
