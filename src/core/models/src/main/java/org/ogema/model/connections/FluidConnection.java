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
package org.ogema.model.connections;

import org.ogema.model.prototypes.Connection;
import org.ogema.model.sensors.FlowSensor;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.sensors.VolumeAccumulatedSensor;

/**
 * Connection characterized by a fluid (gas, water, fuel) flowing from a 
 * device to another.
 */
public interface FluidConnection extends Connection {
	/** 
	 * Flow sensor measuring the flow of the fluid from "input" to "output" (negative values 
	 * indicate flow from "out" to "in"). 
	 */
	FlowSensor flowSensor();

	/**
	 * A generic power sensor. For fuel connections this measures the energy content of the fuel.
	 */
	PowerSensor powerSensor();

	/**
	 * Sensor for accumulated flow (=Volume)
	 */
	VolumeAccumulatedSensor volumeSensor();
}
