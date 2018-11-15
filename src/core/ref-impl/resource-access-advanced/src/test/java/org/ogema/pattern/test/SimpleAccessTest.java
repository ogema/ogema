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

import org.ogema.pattern.test.pattern.HeatPumpRad;
import org.ogema.pattern.test.pattern.OptionalPattern;
import org.ogema.pattern.test.pattern.UninitializedRad;
import org.ogema.pattern.test.pattern.SimplisticPattern;
import org.ogema.pattern.test.pattern.ThermostatPattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.sensors.TemperatureSensor;
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

		public final CountDownLatch foundLatch;
		public final CountDownLatch lostLatch;

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

	class UninitListener implements PatternListener<UninitializedRad> {

		public CountDownLatch foundLatch = new CountDownLatch(1);
		public CountDownLatch lostLatch = new CountDownLatch(1);
		public UninitializedRad lastAvailable = null;

		@Override
		public void patternAvailable(UninitializedRad pattern) {
			lastAvailable = pattern;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(UninitializedRad pattern) {
			lostLatch.countDown();
		}
	};

	class SimplisticPatternListener implements PatternListener<SimplisticPattern> {

		public CountDownLatch foundLatch = new CountDownLatch(1);
		public CountDownLatch lostLatch = new CountDownLatch(1);
		public SimplisticPattern lastAvailable = null;

		@Override
		public void patternAvailable(SimplisticPattern pattern) {
			lastAvailable = pattern;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(SimplisticPattern pattern) {
			lostLatch.countDown();
		}
	};

	@Test
	public void findRADwithUninitializedField() throws InterruptedException {
		UninitListener listener = new UninitListener();
		advAcc.addPatternDemand(UninitializedRad.class, listener, AccessPriority.PRIO_LOWEST);
		UninitializedRad rad = advAcc.createResource("HPName1", UninitializedRad.class);
		rad.model.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		assertNotNull(listener.lastAvailable);
		listener.lastAvailable.model.delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());
	}

	@Test
	public void findSimplisticPattern() throws InterruptedException {
		SimplisticPatternListener listener = new SimplisticPatternListener();
		advAcc.addPatternDemand(SimplisticPattern.class, listener, AccessPriority.PRIO_LOWEST);
		SimplisticPattern rad = advAcc.createResource("SimplePatternTest", SimplisticPattern.class);
		rad.model.activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.foundLatch.getCount());
		assertNotNull(listener.lastAvailable);
		listener.lastAvailable.model.delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals(0, listener.lostLatch.getCount());
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
	
	@Test
	public void createAndActivateComplexPatternInstanceWorks() {
		ThermostatPattern thermo = getApplicationManager().getResourcePatternAccess().createResource(newResourceName(), ThermostatPattern.class);
		thermo.setpoint.create();
		thermo.setpointFB.create();
		thermo.valvePosition.create();
		getApplicationManager().getResourcePatternAccess().activatePattern(thermo);
		ResourceAssertions.assertActive(thermo.model);
		ResourceAssertions.assertActive(thermo.tempSetting);
		ResourceAssertions.assertActive(thermo.setpoint);
		ResourceAssertions.assertActive(thermo.valvePosition);
		ResourceAssertions.assertActive(thermo.setpointFB);
		thermo.model.delete();
	}

	
	@Test
	public void purelyOptionalFieldsWork() throws InterruptedException {
		for (TemperatureSensor ts : resAcc.getResources(TemperatureSensor.class)) { // just in case
			ts.delete();
		}
		final CountDownLatch latch = new CountDownLatch(1);
		final PatternListener<OptionalPattern> listener = new PatternListener<OptionalPattern>() {

			@Override
			public void patternAvailable(OptionalPattern pattern) {
				latch.countDown();
			}

			@Override
			public void patternUnavailable(OptionalPattern pattern) {}
		};
		advAcc.addPatternDemand(OptionalPattern.class, listener, AccessPriority.PRIO_LOWEST);
		Assert.assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
		final TemperatureSensor ts = resMan.createResource(newResourceName(), TemperatureSensor.class);
		ts.activate(false);
		Assert.assertTrue("Missing pattern callback", latch.await(5, TimeUnit.SECONDS));
		ts.delete();
	}
	
}
