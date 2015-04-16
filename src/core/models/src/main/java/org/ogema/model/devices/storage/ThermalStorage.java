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
package org.ogema.model.devices.storage;

import org.ogema.core.model.ResourceList;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.prototypes.Connection;
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
	 * Heat connections to the storage. If the storage does not provide the
	 * {@link #powerSetting()} field the connections may have to be controlled
	 * individually via the {@link ThermalValve}s acting on them (i.e. referenced via {@link Connection#device() }.
	 */
	ResourceList<ThermalConnection> heatConnections();

	/**
	 * Sensor for the volume of the storage medium, if applicable.
	 */
	VolumeAccumulatedSensor volume();
}
