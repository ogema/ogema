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
package org.ogema.model.devices.sensoractordevices;

import org.ogema.core.model.ResourceList;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.sensors.Sensor;

/**
 * A single device containing one or multiple sensors. All sensors provided by sensor device shall be contained in the
 * sensor list as an instance of their actual sensor type. Models from package org.ogema.core.model.commonsensors shall be used.
 */
public interface SensorDevice extends PhysicalElement {

	/**
	 * On/off switch of the device, in case it can be turned off.
	 */
	OnOffSwitch onOffSwitch();

	/**
	 * Electricity connection in case the device is connected to an electricity circuit.
	 */
	ElectricityConnection electricityConnection();

	/**
	 * Battery for a sensor device operating on batteries.
	 */
	ElectricityStorage electricityStorage();

	/**
	 * Rated electrical power for the device.
	 */
	PowerRange ratedPower();

	/**
	 * The sensors contained in the device. The sensors contained in this list
	 * do not need to be all of the same sensor type, so a SensorDevice can 
	 * for example contain a light sensor and a temperature sensor.
	 */
	ResourceList<Sensor> sensors();
}
