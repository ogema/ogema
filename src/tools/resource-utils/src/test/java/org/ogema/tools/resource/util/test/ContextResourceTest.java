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

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.locations.Location;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ResourceUtils;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;


/**
 * Tests for ResourceUtils context resource utilities.
 */
@ExamReactorStrategy(PerClass.class)
public class ContextResourceTest extends OsgiAppTestBase {

	public ContextResourceTest() {
		super(true);
	}

	@Test
	public void contextResourceWorksForSubresource() {
		final Thermostat thermo = getApplicationManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		thermo.temperatureSensor().reading().create();
		Assert.assertEquals(thermo.temperatureSensor(), ResourceUtils.getFirstContextResource(thermo, TemperatureSensor.class));
		Assert.assertEquals(thermo.temperatureSensor().reading(), ResourceUtils.getFirstContextResource(thermo, TemperatureResource.class));
	}
	
	@Test
	public void contextResourceWorksForParentSubresource() {
		final Thermostat thermo = getApplicationManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		final Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		thermo.location().room().setAsReference(room);
		thermo.temperatureSensor().reading().create();
		ResourceAssertions.assertLocationsEqual(room, ResourceUtils.getFirstContextResource(thermo.temperatureSensor(), Room.class));
	}
	
	@Test
	public void contextResourceWorksForReference() {
		final Thermostat thermo = getApplicationManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		final Thermostat thermo2 = getApplicationManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		final Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		thermo2.location().room().setAsReference(room);
		thermo.temperatureSensor().reading().create();
		thermo2.temperatureSensor().setAsReference(thermo.temperatureSensor());
		ResourceAssertions.assertLocationsEqual(room, ResourceUtils.getFirstContextResource(thermo.temperatureSensor(), Room.class));
	}
	
	@Test
	public void contextResourceConflictWorks() {
		final Thermostat thermo = getApplicationManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		final Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		thermo.location().room().setAsReference(room);
		thermo.name().create();
		thermo.name().getSubResource("location", Location.class).device().create();
		ResourceAssertions.assertLocationsEqual(thermo.name().getSubResource("location", Location.class).device(), 
				ResourceUtils.getFirstContextResource(thermo.name(), PhysicalElement.class));
	}
	
	@Test
	public void contextResourceTypeMatcherWorks() {
		final Thermostat thermo = getApplicationManager().getResourceManagement().createResource(newResourceName(), Thermostat.class);
		final Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		thermo.location().room().setAsReference(room);
		thermo.name().create();
		thermo.name().getSubResource("location", Location.class).device().create();
		final Pattern pattern = Pattern.compile("[^" + PhysicalElement.class.getName().replace(".", "\\.") + "]"); // matches everything except the PhysicalElement class
		ResourceAssertions.assertLocationsEqual(room, ResourceUtils.getFirstContextResource(thermo.name(), PhysicalElement.class, pattern, null));
	}
	
}
