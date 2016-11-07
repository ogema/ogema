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
package org.ogema.pattern.test;

import org.ogema.pattern.test.pattern.TempSensPattern;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ValueChangedTest extends OsgiTestBase {

	//@ProbeBuilder
	//public TestProbeBuilder build(TestProbeBuilder builder) {
	//	builder.setHeader("Bundle-SymbolicName", "test-probe");
	//	return builder;
	//}

	public class TempListener implements PatternListener<TempSensPattern> {

		public volatile boolean available;
		public volatile CountDownLatch foundLatch;
		public volatile CountDownLatch lostLatch;

		public TempListener() {
			reset();
			available = false;
		}

		@Override
		public void patternAvailable(TempSensPattern pattern) {
			// System.out.println("Available callback");
			available = true;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(TempSensPattern pattern) {
			// System.out.println("Unavailable callback");
			available = false;
			lostLatch.countDown();
		}

		public void reset() {
			foundLatch = new CountDownLatch(1);
			lostLatch = new CountDownLatch(1);
		}

	}

	@Test
	public void findLoseAndRefindRad() throws InterruptedException {
		TempListener listener = new TempListener();
		advAcc.addPatternDemand(TempSensPattern.class, listener, AccessPriority.PRIO_HIGHEST);
		TemperatureSensor tempSens = resMan.createResource("testTempSens", TemperatureSensor.class);
		tempSens.name().create();
		tempSens.name().setValue("testTempSens");
		tempSens.reading().create();
		tempSens.reading().setCelsius(21);
		tempSens.ratedValues().upperLimit().create();
		tempSens.ratedValues().upperLimit().setCelsius(25);
		tempSens.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		tempSens.reading().setCelsius(27);
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());
		listener.reset();
		tempSens.ratedValues().upperLimit().deactivate(false);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		tempSens.reading().setCelsius(19);
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());
		listener.reset();
		tempSens.reading().setCelsius(27);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		tempSens.reading().deactivate(false);
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());
	}

}
