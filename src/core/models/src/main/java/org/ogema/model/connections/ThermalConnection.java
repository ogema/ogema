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
package org.ogema.model.connections;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.sensors.FlowSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.ThermalEnergySensor;
import org.ogema.model.sensors.ThermalPowerSensor;

/**
 * A thermal connection between two devices/rooms or the thermal connection of a
 * device to a heating/cooling circuit. Thermal connections to other devices are
 * not modeled in the devices' data models, explicitly. If a device contains a
 * ThermalConnection as a sub-resource it is usually meant as the device
 * operating on the thermal connection, i.e. that the device is the "device"
 * entry of the thermal connection.
 */
public interface ThermalConnection extends CircuitConnection {

	@Override
	ThermalCircuit input();

	/**
	 * The thermal power transferred by diffusion as well as possible transport
	 * of a fluid. Positive values related to thermal energy being transported
	 * from "input" to "output".
	 */
	ThermalPowerSensor powerSensor();

	/**
	 * Sensor for accumulated thermal energy. This value contains the power
	 * integrated since {@link ThermalEnergySensor#startTime()}.
	 */
	ThermalEnergySensor energySensor();

	/**
	 * Flow sensor measuring the flow of the fluid from "input" to "output"
	 * (negative values indicate flow from "out" to "in"). If the thermal
	 * connection is not associated with the flow of a fluid (e.g. thermal
	 * conduction through walls) the flow sensor should not exist. If this
	 * connection is known to be flow-based, the empty FlowSensor can be created
	 * to indicate this, even if no values can be provided.
	 */
	FlowSensor flowSensor();

	/**
	 * Thermal conductivity of the connection in W/K. Models a heat conduction
	 * with a power equal to this times the difference in temperatures between
	 * the both ends (and power flow from hot to cold, of course).
	 */
	FloatResource conductivity();

	/**
	 * Temperature at the "input" side, e.g. the temperature of the in-flowing
	 * fluid or the temperature of the "input" device.
	 */
	TemperatureSensor inputTemperature();

	/**
	 * "Output" temperature.
	 * @see ThermalConnection#inputTemperature()
	 */
	TemperatureSensor outputTemperature();

}
