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

import org.ogema.model.connections.ThermalCircuit;
import org.ogema.model.devices.connectiondevices.ThermalValve;

/**
 * A radiator/heater. <br> 
 * Use type {@link org.ogema.model.devices.generators.ElectricHeater ElectricHeater} 
 * for electric heaters.
 */
public interface Radiator extends ThermalCircuit {

	/** 
	 * A thermostat. Also contains a temperature sensor for the ambient temperature. <br>
	 * The {@link Thermostat#valve()} subresource should reference the {@link #valve()}
	 * of the Radiator, if both fields {@link Radiator#thermostat()} and {@link #valve()}
	 * are created. 
	 */
	Thermostat thermostat();

	/** 
	 * The valve governing the fluid connection to the radiator. Also contains the 
	 * thermal connection to the radiator.
	 */
	ThermalValve valve();
}
