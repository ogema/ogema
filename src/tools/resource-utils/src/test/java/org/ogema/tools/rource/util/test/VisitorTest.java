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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Room;
import org.ogema.tools.resource.visitor.ResourceProxy;
import org.ogema.tools.resource.visitor.ResourceVisitor;

/**
 * Tests for resource utils tool.
 * 
 */
public class VisitorTest extends OsgiAppTestBase {

	private ResourceManagement rm;
	private ResourceAccess ra;

	public VisitorTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
		ra = getApplicationManager().getResourceAccess();
	}

	@Test
	public void visitorTest() {
		Thermostat thermo = rm.createResource("thermo", Thermostat.class);
		Room room = rm.createResource("room", Room.class);
		room.location().room().setAsReference(room); // create a loop
		List<Resource> subresources = new ArrayList<Resource>();
		thermo.location().room().setAsReference(room);
		subresources.add(thermo.battery().chargeSensor().reading().create());
		subresources.add(thermo.valve().connection().conductivity().create());
		subresources.add(thermo.valve().connection().energySensor().reading().create());
		subresources.add(thermo.valve().connection().energySensor().settings().controlLimits().create());
		subresources.add(thermo.valve().connection().energySensor().settings().alarmLimits().create());
		TestVisitor visitor = new TestVisitor();
		ResourceProxy proxy = new ResourceProxy(thermo);
		proxy.depthFirstSearch(visitor, false);
		for (Resource sr : subresources) {
			assertTrue("Resource not visited", visitor.wasThere(sr));
		}
		assertTrue("Resource not visited", visitor.wasThere(thermo));
		assertFalse("Referenced resource unexpectedly visited", visitor.wasThere(room));
		// now test again, but this time follow references
		visitor = new TestVisitor();
		proxy.depthFirstSearch(visitor, true);
		for (Resource sr : subresources) {
			assertTrue("Resource not visited", visitor.wasThere(sr));
		}
		assertTrue("Resource not visited", visitor.wasThere(thermo));
		assertTrue("Resource not visited", visitor.wasThere(room));
		room.delete();
		thermo.delete();
	}

	class TestVisitor implements ResourceVisitor {

		private final Set<String> visitedPaths = new LinkedHashSet<String>();

		@Override
		public void visit(Resource resource) {
			visitedPaths.add(resource.getLocation());
		}

		public boolean wasThere(Resource res) {
			return visitedPaths.contains(res.getLocation());
		}

	}

}
