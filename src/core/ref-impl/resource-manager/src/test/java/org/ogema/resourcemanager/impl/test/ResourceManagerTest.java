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
package org.ogema.resourcemanager.impl.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.buildingtechnology.AirConditioner;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.model.actors.OnOffSwitch;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ResourceManagerTest extends OsgiTestBase {

	public static final String RESNAME = ResourceManagerTest.class.getSimpleName();

	/**
	 * Access Declaration like representation of an air conditioner used in this
	 * test.
	 */
	class MyAirCond {

		public AirConditioner device;
		public FloatResource coolingPower;
		public OnOffSwitch swtch;
		public BooleanResource stateControl;
		public BooleanResource feedback;
		public ElectricityConnection elConn;
	}

	@ProbeBuilder
	public TestProbeBuilder buildCustomProbe(TestProbeBuilder builder) {
		builder.setHeader("Export-Package", "org.ogema.resourcemanager.impl.test");
		return builder;
	}

	/**
	 * Creates a small resource tree for subsequent testing. To be used by other
	 * tests.
	 */
	MyAirCond createAirConditioner(boolean activate) {
		final MyAirCond result = new MyAirCond();
		result.device = resMan.createResource(RESNAME + counter++, AirConditioner.class);
		assertNotNull(result.device);
		result.coolingPower = (FloatResource) result.device.addOptionalElement("coolingPower");
		assertNotNull(result.coolingPower);
		result.elConn = (ElectricityConnection) result.device.addOptionalElement("elConn");
		assertNotNull(result.elConn);
		result.swtch = (OnOffSwitch) result.device.addOptionalElement("swtch");
		assertNotNull(result.swtch);
		result.stateControl = (BooleanResource) result.swtch.addOptionalElement("state");
		assertNotNull(result.stateControl);
		result.feedback = (BooleanResource) result.swtch.addOptionalElement("stateFeedback");
		assertNotNull(result.feedback);

		if (activate) {
			result.device.activate(true);
		}
		return result;
	}

	@Test
    // this also tests getResourceTypes()
    public void addResourceTypeWorks() {
        List<Class<? extends Resource>> testClasses = new ArrayList<>();
        testClasses.add(OnOffSwitch.class);
        assertTrue(resMan.getResourceTypes().containsAll(testClasses));
        testClasses.add(ElectricityMeter.class);
        assertTrue(resMan.getResourceTypes().containsAll(testClasses));
    }

	@Test
    // this also tests getResourceTypes()
    public void addingSameResourceTypeAgainWorks() {
        List<Class<? extends Resource>> testClasses = new ArrayList<>();
        testClasses.add(OnOffSwitch.class);
        assertTrue(resMan.getResourceTypes().containsAll(testClasses));
        assertTrue(resMan.getResourceTypes().containsAll(testClasses));
    }

	@Test
	public void createNewResourceWorks() throws ResourceException {
		String name = RESNAME + counter++;
		Resource r;
		assertNotNull(r = resMan.createResource(name, ElectricityMeter.class));
		assertEquals(name, r.getName());
	}

	@Test
	public void createResourceReturnsAlreadyExistingResource() throws ResourceException {
		String name = RESNAME + counter++;
		assertNotNull(resMan.createResource(name, ElectricityMeter.class));
		Resource r;
		assertNotNull(r = resMan.createResource(name, ElectricityMeter.class));
		assertEquals(name, r.getName());
	}

	@Test(expected = ResourceAlreadyExistsException.class)
	public void createResourceFailsIfResourceExistsWithDifferentType() throws ResourceException {
		String name = RESNAME + counter++;
		assertNotNull(resMan.createResource(name, ElectricityMeter.class));
		resMan.createResource(name, OnOffSwitch.class);
	}

	@Test
	public void uniqueResourceNameWorks() {
		String requestedName = "blub";
		resMan.createResource(requestedName, OnOffSwitch.class);
		String uniqueName = resMan.getUniqueResourceName(requestedName);
		assertNotEquals(requestedName, uniqueName);
		System.out.printf("%s -> %s%n", requestedName, uniqueName);
		String uniqueName2 = resMan.getUniqueResourceName(requestedName);
		assertEquals(uniqueName, uniqueName2);
	}

}
