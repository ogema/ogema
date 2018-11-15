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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
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
//	private ResourceAccess ra;

	public VisitorTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
//		ra = getApplicationManager().getResourceAccess();
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
