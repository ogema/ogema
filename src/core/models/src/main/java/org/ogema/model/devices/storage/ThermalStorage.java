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
package org.ogema.model.devices.storage;

import org.ogema.core.model.ResourceList;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.VolumeAccumulatedSensor;

/**
 * Device storing thermal energy.
 */
public interface ThermalStorage extends EnergyStorage {

	/**
	 * Mean temperature of the thermal storage. Individual temperatures at the exchange points can be encoded in the
	 * {@link #heatConnections()}.
	 */
	TemperatureSensor storageTemperature();

	/**
	 * Heat connections to the storage. 
	 */
	ResourceList<ThermalConnection> heatConnections();

	/**
	 * Sensor for the volume of the storage medium, if applicable.
	 */
	VolumeAccumulatedSensor volume();
}
