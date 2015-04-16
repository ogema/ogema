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
	 * @deprecated The thermal connection now is contained in {@link #valve()#connection()}.
	 */
	ThermalConnection connection();
}
