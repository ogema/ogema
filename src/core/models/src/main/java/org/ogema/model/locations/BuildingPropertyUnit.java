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
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.devices.connectiondevices.HeatConnectionBox;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.OccupancySensor;

/**
 * Property unit, for example a flat within a residential building or shop within a mall. A separate BuildingPropertyUnit
 * resource may be used to represent the spaced commonly used by all property units of the building
 * ("virtual building property unit"). The building this belongs to can be inferred from the
 * {@link #location()} of this resource.
 */
public interface BuildingPropertyUnit extends PhysicalElement {

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
	 * 9999: other; greater/equal 10.000: custom values
	 */
	IntegerResource type();

	/**
	 * List of the rooms in this unit.
	 */
	ResourceList<Room> rooms();

	/**
	 * Sensor for presence of a person in this building property unit. If this
	 * is not set, the information may still be available via the occupancy sensors
	 * of the contained rooms.
	 */
	OccupancySensor occupancySensor();
}
