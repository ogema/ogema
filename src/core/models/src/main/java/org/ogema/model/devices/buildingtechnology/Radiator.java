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
