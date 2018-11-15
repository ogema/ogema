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
package org.ogema.pattern.test;

import org.ogema.model.locations.Building;
import org.ogema.pattern.test.pattern.BuildingRad;
import org.ogema.pattern.test.pattern.ChargingStationRad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
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
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
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
	
	/**
	 * Create a toplevel pattern and a pattern below some other resource, and verify that
	 * {@link ResourcePatternAccess#getSubresources(Resource, Class, boolean, AccessPriority)}
	 * only returns the subpattern
	 */
	@Test	
	public void testSubPatterns() {
		Building test = resMan.createResource("testRes", Building.class);
		test.location().create();
		advAcc.addDecorator(test.location(), "recursiveTestSubRes", BuildingRad.class);
		advAcc.addDecorator(test, "testSubRes",BuildingRad.class);
		BuildingRad buildingPattern = advAcc.createResource("topResTest", BuildingRad.class);
		test.activate(true);
		advAcc.activatePattern(buildingPattern);
		List<BuildingRad> subpatterns = advAcc.getSubresources(test, BuildingRad.class, false, AccessPriority.PRIO_HIGHEST);
		assertEquals("Advanced access returned an unexpected number of subpatterns.",1, subpatterns.size());
		test.delete();
		buildingPattern.model.delete();
	}
	
}
