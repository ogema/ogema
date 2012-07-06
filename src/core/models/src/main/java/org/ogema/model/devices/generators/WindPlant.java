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
package org.ogema.model.devices.generators;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.AreaResource;
import org.ogema.model.sensors.GeographicDirectionSensor;
import org.ogema.model.devices.sensoractordevices.WindSensor;

/**
 * Data model for a wind plant. This can also encompass a wind park consisting 
 * of multiple wind turbines.
 */
public interface WindPlant extends ElectricityGenerator {

	/**
	 * Total area of the rotors.
	 */
	AreaResource rotorArea();

	/**
	 * Indicates if the turbines in the plant can be oriented in the wind
	 * direction or are static in one direction.
	 */
	BooleanResource orientable();

	/**
	 * Orientation of the turbines. 
	 * */
	GeographicDirectionSensor orientation();

	/**
	 * List of wind sensors in the wind plant.
	 */
	ResourceList<WindSensor> windSensors();

}
