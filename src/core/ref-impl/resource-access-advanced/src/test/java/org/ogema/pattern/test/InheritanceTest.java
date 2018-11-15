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

import org.ogema.pattern.test.pattern.InheritedOutsideRoomRad;
import org.ogema.pattern.test.pattern.OutsideRoomRad;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.locations.Room;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import junit.framework.Assert;

@ExamReactorStrategy(PerClass.class)
public class InheritanceTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	OutsideRoomRad m_room = null;

	@Before
	public void initInactiveRad() {
		final String RADNAME = "testInheritedRadRoom";
		final Resource existingModel = resAcc.getResource(RADNAME);
		if (existingModel != null) {
			m_room = new InheritedOutsideRoomRad(existingModel);
			m_room.type.setValue(0);
			m_room.model.deactivate(true);
			return;
		}

		// Test the creation of a RAD with the advanced access.
		m_room = advAcc.createResource(RADNAME, InheritedOutsideRoomRad.class);
		assertNotNull(m_room);
		m_room.model.type().setValue(0);
	}

	class CountingListener implements PatternListener<InheritedOutsideRoomRad> {

		public volatile CountDownLatch foundLatch;
		public volatile CountDownLatch lostLatch;

		public volatile InheritedOutsideRoomRad lastAvailable = null;

		public CountingListener(int foundCounts, int lossCounts) {
			foundLatch = new CountDownLatch(foundCounts);
			lostLatch = new CountDownLatch(lossCounts);
		}

		@Override
		public void patternAvailable(InheritedOutsideRoomRad fulfilledDemand) {
			lastAvailable = fulfilledDemand;
			foundLatch.countDown();
            appMan.getLogger().info("pattern available: {}", fulfilledDemand.model.getPath());
		}

		@Override
		public void patternUnavailable(InheritedOutsideRoomRad object2beLost) {
			lostLatch.countDown();
            appMan.getLogger().info("pattern unavailable: {}", object2beLost.model.getPath());
		}
	}

	@Test
	public void findLoseRefindForChildRad() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);
		advAcc.addPatternDemand(InheritedOutsideRoomRad.class, listener, null);
		m_room.model.activate(true);
		assertTrue("pattern should become available", listener.foundLatch.await(10, TimeUnit.SECONDS));
		assertNotNull(listener.lastAvailable);
		assertTrue(((Resource) m_room.model).equalsLocation((Resource) listener.lastAvailable.model));

		final InheritedOutsideRoomRad room = listener.lastAvailable;

		// @Equals annotation was removed
		room.type.setValue(46);
        appMan.getLogger().info("room type changed to incompatible value");
        assertTrue("pattern should become invalid", listener.lostLatch.await(10, TimeUnit.SECONDS));

		listener.foundLatch = new CountDownLatch(1);
		room.type.setValue(0);
        assertTrue("pattern should become available", listener.foundLatch.await(10, TimeUnit.SECONDS));
	}
	
	@Test
	public void doublyParametrizedPatternWorks() {
		final SubPattern pattern = advAcc.createResource(newResourceName(), SubPattern.class);
		Assert.assertNotNull(pattern.model);
		ResourceAssertions.assertExists(pattern.model);
		Assert.assertEquals(TemperatureSensor.class, pattern.model.getResourceType());
		pattern.model.delete();
	}
	
	private static class DoublyParametrizedPattern<S, T extends PhysicalElement> extends ResourcePattern<T> {

		public DoublyParametrizedPattern(Resource match) {
			super(match);
		}
		
	}
	
	private static class SubPattern extends DoublyParametrizedPattern<Float, TemperatureSensor> {
		
		public SubPattern(Resource match) {
			super(match);
		}
		
	}
	
}
