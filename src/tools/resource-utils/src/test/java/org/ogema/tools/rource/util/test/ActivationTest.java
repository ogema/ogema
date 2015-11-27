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
package org.ogema.tools.rource.util.test;

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
		TestPattern pattern = getApplicationManager().getResourcePatternAccess().createResource("testPattern",
				TestPattern.class);
		pattern.room.create();
		pattern.type.create();
		ResourceUtils.activateComplexResources(pattern, true, ra);
		String msg = "Resource unexpctedly found inactive";
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
