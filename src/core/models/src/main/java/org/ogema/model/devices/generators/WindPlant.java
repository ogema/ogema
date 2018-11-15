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
