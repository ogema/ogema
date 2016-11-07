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
package org.ogema.tools.resource.util.test;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.locations.Room;

public class TestPattern extends ResourcePattern<Thermostat> {

	public TestPattern(Resource match) {
		super(match);
	}

	ElectricityStorage battery = model.battery();

	FloatResource chargeState = battery.chargeSensor().reading();

	public ElectricityConnection conn = battery.electricityConnection();

	PowerResource power = conn.powerSensor().reading();

	@Existence(required = CreateMode.OPTIONAL)
	Room room = model.location().room();

	@Existence(required = CreateMode.OPTIONAL)
	IntegerResource type = room.type();

	public ElectricityStorage storage;

	public float testFloat = 0.2F;

}
