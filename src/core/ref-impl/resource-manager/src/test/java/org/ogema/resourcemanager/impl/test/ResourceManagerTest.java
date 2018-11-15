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
package org.ogema.resourcemanager.impl.test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.buildingtechnology.AirConditioner;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.exam.ResourceAssertions;
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
    @SuppressWarnings("deprecation")
	public void uniqueResourceNameWorks() {
		String requestedName = "blub";
		resMan.createResource(requestedName, OnOffSwitch.class);
		String uniqueName = resMan.getUniqueResourceName(requestedName);
		assertNotEquals(requestedName, uniqueName);
		System.out.printf("%s -> %s%n", requestedName, uniqueName);
		String uniqueName2 = resMan.getUniqueResourceName(requestedName);
		assertEquals(uniqueName, uniqueName2);
	}
	
	@Test
	public void resourcesArentLost() {
		String name = newResourceName();
		int nrResources = 250;
		for (int k=0; k<nrResources; k++) {
			resMan.createResource(name + "__" + k, TemperatureSensor.class).location().room().temperatureSensor().reading().create();
		}
		System.gc();
		for (int k=0; k<nrResources; k++) {
			TemperatureSensor resource = resAcc.getResource(name + "__" + k);
			Assert.assertNotNull("Resource unexepectedly found null",resource);
			ResourceAssertions.assertExists(resource);
			ResourceAssertions.assertExists(resource.location().room().temperatureSensor().reading());
			resource.delete();
		}
		
	}
	
	@Test
	public void referencesArentLost() {
		String name = newResourceName();
		int nrResources = 250;
		for (int k=0; k<nrResources; k++) {
			resMan.createResource(name + "__" + k, TemperatureSensor.class).location().room().temperatureSensor().reading().create();
			if (k>0)
				resAcc.<TemperatureSensor> getResource(name + "__" + k).location().room().setAsReference(resAcc.<TemperatureSensor> getResource(name + "__" + (k-1)).location().room());
		}
		System.gc();
		for (int k=0; k<nrResources; k++) {
			TemperatureSensor resource = resAcc.getResource(name + "__" + (nrResources - k - 1));
			Assert.assertNotNull("Resource unexepectedly found null",resource);
			ResourceAssertions.assertExists(resource);
			ResourceAssertions.assertExists(resource.location().room().temperatureSensor().reading());
			resource.delete();
		}
		
	}

}
