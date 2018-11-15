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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.LightSensor;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 * Tests for resource utils tool.
 * 
 */
public class ActivationTest extends OsgiAppTestBase {

	private ResourceManagement rm;
	private ResourceAccess ra;

	public ActivationTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
		ra = getApplicationManager().getResourceAccess();
	}

	@Test
	public void testComplexResourceActivation() {
		Room room = rm.createResource("room", Room.class);
		room.temperatureSensor().reading().create();
		room.heatCapacity().create();
		room.humiditySensor().reading().create();
		LightSensor ls = rm.createResource("lightSensor", LightSensor.class);
		room.lightSensor().setAsReference(ls);
		ResourceUtils.activateComplexResources(room, true, ra);
		String msg = "Resource unexpctedly found inactive";
		assertTrue(msg, room.isActive());
		assertTrue(msg, room.temperatureSensor().isActive());
		assertTrue(msg, room.humiditySensor().isActive());
		msg = "Resource unexpectedly found active";
		assertFalse(msg, room.temperatureSensor().reading().isActive());
		assertFalse(msg, room.humiditySensor().reading().isActive());
		assertFalse(msg, room.heatCapacity().isActive());
		assertFalse(msg, ls.isActive());
		room.delete();
		ls.delete();
	}

	@Test
	public void testComplexResourcePatternActivation() {
		TestPattern pattern = getApplicationManager().getResourcePatternAccess().createResource("testPattern", TestPattern.class);
		pattern.room.create();
		pattern.type.create();
		ResourceUtils.activateComplexResources(pattern, true, ra);
		String msg = "Resource unexpectedly found inactive";
		assertTrue(msg, pattern.model.isActive());
		assertTrue(msg, pattern.battery.isActive());
		assertTrue(msg, pattern.conn.isActive());
		assertTrue(msg, pattern.room.isActive());
		msg = "Resource unexpectedly found active";
		assertFalse(msg, pattern.chargeState.isActive());
		assertFalse(msg, pattern.power.isActive());
		assertFalse(msg, pattern.type.isActive());
		pattern.model.delete();
	}
}
