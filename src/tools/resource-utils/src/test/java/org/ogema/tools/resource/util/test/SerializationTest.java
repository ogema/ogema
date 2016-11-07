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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.tools.resource.util.SerializationUtils;

public class SerializationTest extends OsgiAppTestBase {

	private ResourceManagement rm;

	public SerializationTest() {
		super(true);
	}
	
	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
	}

	
	@Test
	public void removeSubresourcesWorks_JSON() {
		PhysicalElement dummy = rm.createResource(newResourceName(), PhysicalElement.class);
		Room room = dummy.location().room().create();
		room.name().<StringResource> create().setValue("test");
		room.getSubResource("testSubRes", StringResource.class).<StringResource> create().setValue("test2");
		room.temperatureSensor().reading().<TemperatureResource> create().setCelsius(23.2F);
		String json = getApplicationManager().getSerializationManager().toJson(dummy);
		json = SerializationUtils.removeSubresources(json, StringResource.class, true);
		System.out.println("Applying json " + json);
		room.delete();
		getApplicationManager().getSerializationManager().applyJson(json, dummy, false);
		ResourceAssertions.assertExists(room);
		assertEquals("Unexpected number of StringResource subresources", 0, room.getSubResources(StringResource.class,true).size());
		assertEquals("Unexpected number of subresources", 2, room.getSubResources(true).size());
		dummy.delete();
	}

}
