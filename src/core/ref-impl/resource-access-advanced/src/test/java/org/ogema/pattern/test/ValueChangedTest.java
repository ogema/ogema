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
