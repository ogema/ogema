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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.model.actors.Actor;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.metering.ElectricityMeter;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ResourceAccessTest extends OsgiTestBase {

	public static final String RESNAME = "foo";

	@AfterClass
	public static void afterClass() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void getToplevelResourcesWithTypeNullWorks() {
		Resource r1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource r2 = resMan.createResource(RESNAME + counter++, ElectricityMeter.class);
		List<Resource> all = resAcc.getToplevelResources(null);
		assertTrue(all.size() >= 2);
		assertTrue(all.contains(r1));
		assertTrue(all.contains(r2));
	}

	@Test
	public void getResourcesWithTypeNullWorks() {
		Resource r1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource r2 = resMan.createResource(RESNAME + counter++, ElectricityMeter.class);
		Resource r3 = r2.addDecorator("decor", OnOffSwitch.class);
		List<? extends Resource> all = resAcc.getResources(null);
		assertTrue(all.size() >= 3);
		assertTrue(all.contains(r1));
		assertTrue(all.contains(r2));
		assertTrue(all.contains(r3));
	}

	@Test
	public void getResourcesWorks() {
		Resource r1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource r2 = resMan.createResource(RESNAME + counter++, ElectricityMeter.class);
		Resource r3 = r2.addDecorator("decor", OnOffSwitch.class);
		List<? extends Resource> all = resAcc.getResources(OnOffSwitch.class);
		assertTrue(all.size() >= 2);
		assertTrue(all.contains(r1));
		assertFalse(all.contains(r2));
		assertTrue(all.contains(r3));
	}
    
    @Test
	public void getResourcesReturnsSubtypes() {
		Resource r1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource r2 = resMan.createResource(RESNAME + counter++, ElectricityMeter.class);
		Resource r3 = r2.addDecorator("decor", OnOffSwitch.class);
		List<? extends Resource> all = resAcc.getResources(Actor.class);
		assertTrue(all.size() >= 2);
		assertTrue(all.contains(r1));
		assertFalse(all.contains(r2));
		assertTrue(all.contains(r3));
	}

	@Test
	public void getToplevelResourcesWorks() {
		Resource r1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		Resource r2 = resMan.createResource(RESNAME + counter++, ElectricityMeter.class);
		List<? extends Resource> all = resAcc.getToplevelResources(OnOffSwitch.class);
		assertTrue(all.size() >= 1);
		assertTrue(all.contains(r1));
		assertFalse(all.contains(r2));
	}

	@Test
	public void getResourceWorks() {
		String name = RESNAME + counter++;
		@SuppressWarnings("unused")
		Resource r1 = resMan.createResource(name, OnOffSwitch.class);
		Resource r = resAcc.getResource("/" + name);
		assertNotNull(r);
	}

	@Test
	public void getResourceReturnsNullForNonExistingResource() {
		String name = RESNAME + counter++;
		@SuppressWarnings("unused")
		Resource r1 = resMan.createResource(name, OnOffSwitch.class);
		Resource r = resAcc.getResource("/fnord");
		assertNull(r);
	}
    
    @Test(expected = NoSuchResourceException.class)
    public void getResourceChecksForValidResourceNames() {
        resAcc.getResource("12345");
        assertTrue(true);
    }
    
    @Test(expected = NoSuchResourceException.class)
    public void getResourceChecksForValidResourceNames2() {
        Resource top = resMan.createResource(newResourceName(), Resource.class);
        resAcc.getResource("/" + top.getName() + "/" + "12345");
        assertTrue(true);
    }
}
