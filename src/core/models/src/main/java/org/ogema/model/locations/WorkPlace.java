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
