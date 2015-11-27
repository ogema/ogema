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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.sensors.ThermalPowerSensor;
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

}
