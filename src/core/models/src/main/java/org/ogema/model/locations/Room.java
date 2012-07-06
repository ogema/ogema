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
	//	/** Reference to building property unit in which room is situated */
	//	BuildingPropertyUnit propertyUnit();

	/**
	 * Type of room:<br>
	 * 0: outside of building <br>
	 * 1: Living room (private)<br>
	 * 2: Combined living room with kitchen (private)<br>
	 * 3: Kitchen (private)<br>
	 * 10: Bed room (private)<br>
	 * 20: Garage (private)<br>
	 * 100: Office room<br>
	 * 101: Meeting room<br>
	 * 200: Commercial kitchen<br>
	 * 210: Commercial public dining room<br>
	 * ...<br>
	 * >=10.000: custom values
	 */
	IntegerResource type();

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

	/** Parts of room assigned to work places */
	public ResourceList<WorkPlace> workPlaces();

	/**
	 * Reference to meter that measures devices within in the room. Note that it is to be determined from electric
	 * circuit connected to the meter whether all devices in the room are included and whether also devices from other
	 * rooms are included. If the virtual meter shall be deleted in case the room information is deleted, it may also be
	 * created as direct child resource.
	 */
	ElectricityMeter meter();
}
