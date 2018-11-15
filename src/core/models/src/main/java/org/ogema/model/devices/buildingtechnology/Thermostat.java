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
package org.ogema.model.devices.buildingtechnology;

import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Data model for a room thermostat.
 */
public interface Thermostat extends PhysicalElement {

	/**
	 * Temperature sensor of the thermostat. This not only contains the
	 * thermostat's temperature reading (if available), but also the temperature
	 * setpoint and the device settings.
	 */
	TemperatureSensor temperatureSensor();

	/**
	 * The thermal valve that is controlled by this thermostat. Also
	 * contains the thermal connection that is ultimately affected by
	 * this.
	 */
	ThermalValve valve();

	/**
	 * Battery of the device.
	 */
	ElectricityStorage battery();

	/**
	 * The thermal connection that is influenced by the thermostat.
	 * @deprecated The thermal connection now is contained in {@link #valve() valve.connection}.
	 */
	@Deprecated
	ThermalConnection connection();
}
