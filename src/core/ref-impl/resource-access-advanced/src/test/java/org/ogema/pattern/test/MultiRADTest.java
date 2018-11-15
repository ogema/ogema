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

import org.ogema.model.devices.generators.HeatPump;
import org.ogema.pattern.test.pattern.HeatPumpRad;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class MultiRADTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	class RADListener implements PatternListener<HeatPumpRad> {

		public CountDownLatch foundLatch1 = new CountDownLatch(1);
		public CountDownLatch lostLatch1 = new CountDownLatch(1);
		public CountDownLatch foundLatch2 = new CountDownLatch(1);
		public CountDownLatch lostLatch2 = new CountDownLatch(1);
		public boolean available1 = false;
		public boolean available2 = false;
		public boolean nonavailable1 = false;
		public boolean nonavailable2 = false;

		public RADListener() {
		}

		@Override
		public void patternAvailable(HeatPumpRad pump) {
			switch (pump.model.getLocation()) {
			case "HPTestName1":
				available1 = true;
				foundLatch1.countDown();
				break;
			case "HPTestName2":
				available2 = true;
				foundLatch2.countDown();
				break;
			}
		}

		@Override
		public void patternUnavailable(HeatPumpRad pump) {
			switch (pump.model.getLocation()) {
			case "HPTestName1":
				nonavailable1 = true;
				lostLatch1.countDown();
				break;
			case "HPTestName2":
				nonavailable2 = true;
				lostLatch2.countDown();
				break;
			}
		}
	}

	@Test
	public void findLoseRad() throws InterruptedException {

		RADListener listener = new RADListener();

		advAcc.addPatternDemand(HeatPumpRad.class, listener, AccessPriority.PRIO_LOWEST);
		final HeatPumpRad rad1 = advAcc.createResource("HPTestName1", HeatPumpRad.class);
		final HeatPumpRad rad2 = advAcc.createResource("HPTestName2", HeatPumpRad.class);
		rad1.model.activate(true);
		listener.foundLatch1.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch1.getCount());
		rad2.model.activate(true);
		listener.foundLatch2.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch2.getCount());
		rad1.model.delete();
		listener.lostLatch1.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch1.getCount());
		rad2.conn.deactivate(false);
		listener.lostLatch2.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch2.getCount());
		listener.foundLatch2 = new CountDownLatch(1);
		rad2.conn.activate(false);
		listener.foundLatch2.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch2.getCount());
	}

	@Test
	public void getAllPatternsWorks() {
		final HeatPumpRad rad1 = advAcc.createResource("HPTestName1", HeatPumpRad.class);
		final HeatPumpRad rad2 = advAcc.createResource("HPTestName2", HeatPumpRad.class);
		advAcc.activatePattern(rad1);
		advAcc.activatePattern(rad2);
		HeatPump hp = resMan.createResource("HPTestName3", HeatPump.class);
		hp.thermalConnection().create();
		hp.activate(true);
		List<HeatPumpRad> patterns = advAcc.getPatterns(HeatPumpRad.class, AccessPriority.PRIO_HIGHEST);
		assertEquals("Unexpected number of patterns returned by ResourcePatternAccess.getPatterns()", 2, patterns
				.size());
		rad1.model.deactivate(true);
		rad2.model.delete();
		patterns = advAcc.getPatterns(HeatPumpRad.class, AccessPriority.PRIO_HIGHEST);
		assertEquals("Pattern deletion or deactivation failed", 0, patterns.size());
		hp.delete();
		rad1.model.delete();
	}

}
