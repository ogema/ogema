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
