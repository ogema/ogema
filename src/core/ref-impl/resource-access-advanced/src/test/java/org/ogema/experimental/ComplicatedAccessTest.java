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
package org.ogema.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ComplicatedAccessTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	BuildingRad m_building = null;

	@Before
	public void init() {
		final String RADNAME = "testRad";
		final Resource existingModel = resAcc.getResource(RADNAME);
		if (existingModel != null) {
			m_building = new BuildingRad(existingModel);
			assertTrue(m_building.requiredFieldsExist());
			if (!m_building.requiredFieldsActive())
				((Resource) m_building.model).activate(true);
			return;
		}
		// Test the creation of a RAD with the advanced access.
		m_building = advAcc.createResource(RADNAME, BuildingRad.class);
		assertNotNull(m_building);
		assertTrue(m_building.requiredFieldsNotNull());
		assertTrue(m_building.requiredFieldsExist());
		assertTrue(m_building.requiredFieldsInactive());
		final IntegerResource optionalElement = m_building.connType;
		assertFalse(optionalElement.exists());
		m_building.model.activate(true);
		assertTrue(m_building.requiredFieldsActive());
	}

	class CountingListener implements PatternListener<BuildingRad> {

		public CountDownLatch foundLatch;
		public CountDownLatch lostLatch = new CountDownLatch(1);

		public BuildingRad lastAvailable = null;

		public CountingListener(int foundCounts, int lossCounts) {
			foundLatch = new CountDownLatch(foundCounts);
			lostLatch = new CountDownLatch(lossCounts);
		}

		@Override
		public void patternAvailable(BuildingRad fulfilledDemand) {
			lastAvailable = fulfilledDemand;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(BuildingRad object2beLost) {
			lostLatch.countDown();
		}
	}

	@Test
	public void findLoseAndRefindRad() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);

		advAcc.addPatternDemand(BuildingRad.class, listener, null);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		assertNotNull(listener.lastAvailable);
		assertTrue(m_building.model.equalsLocation(listener.lastAvailable.model));

		final BuildingRad building = listener.lastAvailable;
		building.elConn.deactivate(false);
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(listener.lostLatch.getCount(), 0);

		listener.foundLatch = new CountDownLatch(1);
		listener.lastAvailable.model.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(listener.foundLatch.getCount(), 0);
	}

	@Test
	@Ignore("incomplete")
	public void findLoseAndRefindRadWithDeletionAndReferences() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);

		advAcc.addPatternDemand(BuildingRad.class, listener, null);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		assertNotNull(listener.lastAvailable);
		assertTrue(m_building.model.equalsLocation(listener.lastAvailable.model));

		final BuildingRad building = listener.lastAvailable;
		building.powerReading.forecast().delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());

		listener.foundLatch = new CountDownLatch(1);
		PowerResource alternatePowerRes = resMan.createResource("alternatePower", PowerResource.class);
		alternatePowerRes.forecast().create();
		alternatePowerRes.activate(true);
		building.elConn.powerSensor().reading().forecast().setAsReference(alternatePowerRes.forecast());
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());

		alternatePowerRes.delete();
		building.powerForecast.create();
		building.model.activate(true);
	}

	@Test
	public void losingWriteAccessCausesLossOfRad() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);

		advAcc.addPatternDemand(BuildingRad.class, listener, AccessPriority.PRIO_LOWEST);
		assertTrue(listener.foundLatch.await(5, TimeUnit.SECONDS));
		assertNotNull(listener.lastAvailable);
		assertTrue(m_building.model.equalsLocation(listener.lastAvailable.model));

		final BuildingRad building = listener.lastAvailable;
		final FloatResource powerReading = building.powerReading;
		powerReading.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_LOWEST);
		assertEquals(AccessMode.SHARED, powerReading.getAccessMode());
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(listener.lostLatch.getCount(), 0);

		listener.foundLatch = new CountDownLatch(1);
		powerReading.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_LOWEST);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(listener.foundLatch.getCount(), 0);
	}

	@Test
	public void creatingRadWithResourceListWorks() {

		final String RADNAME = "creationChargingStation";
		final ChargingStationRad rad = advAcc.createResource(RADNAME, ChargingStationRad.class);

		assertNotNull(rad);
		assertNotNull(rad.cps);
		assertTrue(rad.cps.exists());
		assertFalse(rad.cps.isActive());
		assertTrue(rad.cps.getAllElements().isEmpty());
	}
}
