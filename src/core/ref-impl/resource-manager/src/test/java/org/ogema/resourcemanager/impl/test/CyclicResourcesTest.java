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

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.devices.generators.PVPlant;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests for the case that cycles (via refences) exist in the resource graph.
 */
@ExamReactorStrategy(PerClass.class)
public class CyclicResourcesTest extends OsgiTestBase {

	public static final String RESNAME = CyclicResourcesTest.class.getSimpleName();

	/**
	 * A made-up cyclical structure of - (A), a PV plant with - (B), an azimuth angle, for which - (C), a definition
	 * schedule exists, which - has a decorator referring to the PV plant this corresponds to.
	 */
	class CyclicStructure {

		private final ResourceManagement resMan;
		public PVPlant pvPlant;
		public FloatResource azimuth;
		public DefinitionSchedule aziforecast;
		public PVPlant referredPlant;

		/**
		 * Creates the object and remembers the reference to OGEMA. Does not actually create the resources. Use
		 * this.create() for doing so.
		 * 
		 * @param resMan
		 *            Reference to the OGEMA resouce manager to use.
		 */
		public CyclicStructure(final ResourceManagement resMan) {
			this.resMan = resMan;
		}

		/**
		 * Actually create the resources for the structure.
		 * 
		 * @param activate
		 *            iff true, created resources are being activated.
		 */
		public void create(final boolean activate) {
			pvPlant = resMan.createResource(RESNAME + counter++, PVPlant.class);
			assertNotNull(pvPlant);
			azimuth = pvPlant.azimuth().reading();
			azimuth.create();
			assertNotNull(azimuth);
			azimuth.setValue(0.f);
			aziforecast = azimuth.addDecorator("definition", DefinitionSchedule.class);
			assertNotNull(aziforecast);
			aziforecast.addDecorator("referringObject", pvPlant);
			referredPlant = (PVPlant) aziforecast.getSubResource("referringObject");
			assertTrue(referredPlant.equalsLocation(pvPlant));
			assertFalse(referredPlant.equalsPath(pvPlant));

			if (activate)
				pvPlant.activate(true);
		}
	}

	/**
	 * Tests if getSubResource works. Test fails will probably result in a stack overflow error, owing to the framework
	 * running in an infinite loop.
	 */
	@Test
	public void getAllSubresourcesWorks() {
		CyclicStructure struct = new CyclicStructure(resMan);
		struct.create(true);

		// get all subresrouces recursively
		List<Resource> subres;
		subres = struct.pvPlant.getSubResources(true);
		assertEquals(subres.size(), 4);
		subres = struct.azimuth.getSubResources(true);
		assertEquals(subres.size(), 4);
		subres = struct.aziforecast.getSubResources(true);
		assertEquals(subres.size(), 4);
		subres = struct.referredPlant.getSubResources(true);
		assertEquals(subres.size(), 4);

		// get only subresources of specific type
		List<PVPlant> subpv;
		subpv = struct.pvPlant.getSubResources(PVPlant.class, true);
		assertEquals(subpv.size(), 1);
		subpv = struct.azimuth.getSubResources(PVPlant.class, true);
		assertEquals(subpv.size(), 1);
		subpv = struct.aziforecast.getSubResources(PVPlant.class, true);
		assertEquals(subpv.size(), 1);
		subpv = struct.referredPlant.getSubResources(PVPlant.class, true);
		assertEquals(subpv.size(), 1);
	}
}
