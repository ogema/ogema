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

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricEnergySensor;
import org.ogema.model.sensors.ElectricFrequencySensor;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;
import org.ogema.model.sensors.ReactivePowerAngleSensor;

/**
 * Electric connection of a device. The connection may be AC or DC. If this is
 * sub-resource of a device the device is assumed to be the "output". That is, a
 * positive power rating relates to energy transfer to the device.
 */
public interface ElectricityConnection extends CircuitConnection {

	@Override
	ElectricityCircuit input();

	/**
	 * Power sensor (active power) for the connection.
	 */
	ElectricPowerSensor powerSensor();

	/**
	 * Sensor for accumulated energy (reading of power sensor integrated
	 * since {@link ElectricEnergySensor#startTime()}).
	 * For other
	 * accumulated readings, use a {@link ElectricityMeter} referring to
	 * this connection. 
	 */
	ElectricEnergySensor energySensor();

	/**
	 * Reactive power sensor. Not relevant for DC connections
	 */
	ElectricPowerSensor reactivePowerSensor();

	/**
	 * GenericFloatSensor to measure reactive power angle<br>
	 */
	ReactivePowerAngleSensor reactiveAngleSensor();

	/**
	 * voltage sensor
	 */
	ElectricVoltageSensor voltageSensor();

	/**
	 * current sensor
	 */
	ElectricCurrentSensor currentSensor();

	/**
	 * frequency sensor
	 */
	ElectricFrequencySensor frequencySensor();

	/**
	 * connection type:<br>
	 * 1: fixed connection<br>
	 * 2: plug, usually connected<br>
	 * 3: plug, frequently disconnected (e.g. device connected via a manual
	 * switch)<br>
	 * 4: plug, connected at various places (e.g. vacuum cleaner)
	 */
	IntegerResource connectionType();

	/**
	 * In case of a multi-phase connection access to connection information of
	 * the single phases may be provided
	 */
	public ResourceList<ElectricityConnection> subPhaseConnections();
}
