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
public class SimpleAccessTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	class CountingListener implements PatternListener<HeatPumpRad> {

		public CountDownLatch foundLatch;
		public CountDownLatch lostLatch;

		public HeatPumpRad lastAvailable = null;

		public CountingListener(int foundCounts, int lossCounts) {
			foundLatch = new CountDownLatch(foundCounts);
			lostLatch = new CountDownLatch(lossCounts);
		}

		@Override
		public void patternAvailable(HeatPumpRad pump) {
			lastAvailable = pump;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(HeatPumpRad pump) {
			lostLatch.countDown();
		}
	}

	@Test
	public void findLoseRad() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);

		advAcc.addPatternDemand(HeatPumpRad.class, listener, AccessPriority.PRIO_LOWEST);
		final HeatPumpRad rad = advAcc.createResource("HPName1", HeatPumpRad.class);
		rad.model.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		assertNotNull(listener.lastAvailable);

		listener.lastAvailable.model.delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());
	}

	@Test
	public void findReferencedRAD() throws InterruptedException {

		final CountingListener listener = new CountingListener(1, 1);

		advAcc.addPatternDemand(HeatPumpRad.class, listener, AccessPriority.PRIO_LOWEST);
		final HeatPumpRad rad = advAcc.createResource(newResourceName(), HeatPumpRad.class);
		// delete the sensor part and activate the rest
		rad.sensor.delete();
		rad.model.activate(true);
		// create an active sensor an reference it into the rad
		ThermalPowerSensor refSensor = resMan.createResource("refSensor", ThermalPowerSensor.class);
		refSensor.reading().create();
		refSensor.activate(true);
		assertEquals(1, listener.foundLatch.getCount());

		//Thread.sleep(500);

		AdminApplication app = getApplicationManager().getAdministrationManager().getAppById(
				getApplicationManager().getAppID().getIDString());
		System.out.println(app.getStructureListeners());
		System.out.println(app.getResourceDemands());
		System.out.println(app.getResourceListeners());

		rad.sensor.setAsReference(refSensor);

		assertTrue(rad.sensor.reading().exists());
		// this should now issue the callback for the RAD being completed.
		assertTrue("no callback", listener.foundLatch.await(10, TimeUnit.SECONDS));
		assertNotNull(listener.lastAvailable);

		advAcc.removePatternDemand(HeatPumpRad.class, listener);
		refSensor.delete();
		rad.model.delete();
	}

}
