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

import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.devices.buildingtechnology.ElectricLight;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Part of a room, for example part of an office room including a single desk, chair etc.
 * The room this is located in can be inferred from the {@link #location() } of this.
 */
public interface WorkPlace extends PhysicalElement {

	/** Temperature measurement at work place */
	TemperatureSensor temperatureSensor();

	/** Light available at work place */
	LightSensor lightSensor();

	/** Occupancy of work place (usually only status is relevant). */
	OccupancySensor occupancySensor();

	/** Light assigned to work place */
	ElectricLight light();

	/**
	 * Reference to meter that measures devices within in the workplace. Note that it is to be determined from electric
	 * circuit connected to the meter whether all devices in the workplace are included and whether also devices from
	 * other workplaces/rooms are included. If the virtual meter shall be deleted in case the workplace information is
	 * deleted, it may also be created as direct child resource.
	 */
	ElectricityMeter meter();
}
