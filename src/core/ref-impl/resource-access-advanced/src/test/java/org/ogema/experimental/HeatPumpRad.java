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
package org.ogema.experimental;

import org.ogema.core.model.Resource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.devices.generators.HeatPump;
import org.ogema.model.sensors.ThermalPowerSensor;

/**
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class HeatPumpRad extends ResourcePattern<HeatPump> {

	public ThermalConnection conn = model.thermalConnection();
	public ThermalPowerSensor sensor = conn.powerSensor();
	public PowerResource power = sensor.reading();

	public HeatPumpRad(Resource match) {
		super(match);
	}

	public boolean allActive() {
		return conn.isActive() && sensor.isActive() && power.isActive();
	}

	public boolean allInactive() {
		return (!conn.isActive()) && (!sensor.isActive()) && (!power.isActive());
	}

}
