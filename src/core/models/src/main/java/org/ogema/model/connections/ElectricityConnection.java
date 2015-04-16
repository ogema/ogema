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
