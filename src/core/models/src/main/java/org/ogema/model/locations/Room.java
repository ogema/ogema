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
import org.ogema.core.model.units.AreaResource;
import org.ogema.model.sensors.CO2Sensor;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Representation of a room in a {@link BuildingPropertyUnit}. The BuildingPropertyUnit
 * this room belongs to can be inferred from the {@link #location() } of this room.
 */
public interface Room extends PhysicalElement {

	/**
	 * Type of room:<br>
	 * 0: outside of building <br>
	 * 1: Living room (private)<br>
	 * 2: Combined living room with kitchen (private)<br>
	 * 3: Kitchen (private)<br>
	 * 4: Private bath room (with bath / shower, may also contain toilet)<br>
	 * 5: Toilet (without bath / shower)
	 * 10: Bed room (private)<br>
	 * 20: Garage (private)<br>
	 * 100: Office room<br>
	 * 101: Meeting room<br>
	 * 200: Commercial kitchen<br>
	 * 210: Commercial public dining room<br>
	 * ...<br>
	 * greater/equal 10.000: custom values
	 */
	IntegerResource type();

	/**
	 * Area of the room.
	 */
	AreaResource area();

	/** Parts of room assigned to work places */
	public ResourceList<WorkPlace> workPlaces();

	/** Room temperature */
	TemperatureSensor temperatureSensor();

	/** CO2 level in room atmosphere */
	CO2Sensor co2Sensor();

	/** Relative humidity in room atmosphere */
	HumiditySensor humiditySensor();

	/** Average light available in the room */
	LightSensor lightSensor();

	/** Motion detector for entire room */
	MotionSensor motionSensor();

	/** Room occupancy */
	OccupancySensor occupancySensor();

	/**
	 * Reference to meter that measures devices within in the room. Note that it is to be determined from electric
	 * circuit connected to the meter whether all devices in the room are included and whether also devices from other
	 * rooms are included. If the virtual meter shall be deleted in case the room information is deleted, it may also be
	 * created as direct child resource.
	 */
	ElectricityMeter meter();
}
