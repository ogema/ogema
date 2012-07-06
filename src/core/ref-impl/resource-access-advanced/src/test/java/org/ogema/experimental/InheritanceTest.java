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
package org.ogema.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class InheritanceTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	OutsideRoomRad m_room = null;

	@Before
	public void initDeactiveRad() {
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

		public CountDownLatch foundLatch;
		public CountDownLatch lostLatch = new CountDownLatch(1);

		public InheritedOutsideRoomRad lastAvailable = null;

		public CountingListener(int foundCounts, int lossCounts) {
			foundLatch = new CountDownLatch(foundCounts);
			lostLatch = new CountDownLatch(lossCounts);
		}

		@Override
		public void patternAvailable(InheritedOutsideRoomRad fulfilledDemand) {
			lastAvailable = fulfilledDemand;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(InheritedOutsideRoomRad object2beLost) {
			lostLatch.countDown();
		}
	}

	@Test
	//        @Ignore
	public void findLoseRefindForChildRad() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);

		advAcc.addPatternDemand(InheritedOutsideRoomRad.class, listener, null);
		m_room.model.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		assertNotNull(listener.lastAvailable);
		assertTrue(((Resource) m_room.model).equalsLocation((Resource) listener.lastAvailable.model));

		final InheritedOutsideRoomRad room = listener.lastAvailable;

		// @Equals annotation was removed
		//		room.type.setValue(46);
		//		listener.lostLatch.await(5, TimeUnit.SECONDS);
		//		assertEquals(0, listener.lostLatch.getCount());
		//
		//		listener.foundLatch = new CountDownLatch(1);
		//		room.type.setValue(0);
		//		listener.foundLatch.await(5, TimeUnit.SECONDS);
		//		assertEquals(0, listener.foundLatch.getCount());
	}
}
