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

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.devices.generators.HeatPump;
import org.ogema.model.sensors.TemperatureSensor;

/**
 * Air conditioning operating on a thermal thermalConnection. In principle, the device
 can operate in both directions on the thermalConnection. Usually, air conditioners
 have a target room/location whose temperature they are intended to control.
 This should be the {@link ThermalConnection#output() } of the thermalConnection, 
 whenever possible.
 */
public interface AirConditioner extends HeatPump {

	/**
	 * Determines whether air conditioner also reduces absolute humidity
	 */
	BooleanResource isDehumidifying();

	/**
	 * Temperature sensor of the target location this air conditioner is
	 * supposed to cool/heat. Will often be a reference to the
	 * {@link #thermalConnection()}s 
	 * {@link ThermalConnection#outputTemperature()}.
	 */
	TemperatureSensor temperatureSensor();

}
