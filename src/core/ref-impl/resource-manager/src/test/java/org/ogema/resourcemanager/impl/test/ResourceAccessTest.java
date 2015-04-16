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
package org.ogema.resourcemanager.impl.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.ogema.core.model.Resource;
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
		Resource r1 = resMan.createResource(name, OnOffSwitch.class);
		Resource r = resAcc.getResource("/" + name);
		assertNotNull(r);
	}

	@Test
	public void getResourceReturnsNullForNonExistingResource() {
		String name = RESNAME + counter++;
		Resource r1 = resMan.createResource(name, OnOffSwitch.class);
		Resource r = resAcc.getResource("/fnord");
		assertNull(r);
	}
}
