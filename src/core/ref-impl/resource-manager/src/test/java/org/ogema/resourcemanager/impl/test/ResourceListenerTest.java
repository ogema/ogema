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
package org.ogema.resourcemanager.impl.test;

import java.util.concurrent.BrokenBarrierException;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.exam.ValueTestListener;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.sensors.PowerSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
@SuppressWarnings("deprecation")
public class ResourceListenerTest extends OsgiTestBase {

	String RESNAME = getClass().getSimpleName();

	@Test
	public void resourceChangeListenerCallbacksWork() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				callbackCount.countDown();
			}
		};

		sw.controllable().create();
		sw.controllable().addResourceListener(l, true);
		sw.controllable().activate(true);
		sw.controllable().setValue(true);

		assertTrue("no resourceChanged callback", callbackCount.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void recursiveResourceChangeListenerRegistrationsAffectExistingSubresources() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		final OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw.addOptionalElement("controllable");
		sw.controllable().activate(true);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				assertEquals(sw.controllable(), resource);
				callbackCount.countDown();
			}
		};

		sw.addResourceListener(l, true);
		sw.controllable().setValue(true);

		assertTrue("no resourceChanged callback", callbackCount.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void recursiveResourceChangeListenerRegistrationsAffectNewlyAddedOptionals() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		final OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener listener = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				callbackCount.countDown();
			}
		};

		sw.addResourceListener(listener, true);

		sw.addOptionalElement("controllable");
		//                sw.controllable().create(); // this would fail - see next test
		sw.controllable().activate(true);
		sw.controllable().setValue(true);

		assertTrue("no resourceChanged callback", callbackCount.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void recursiveResourceChangeListenerRegistrationsAffectNewlyAddedOptionals2() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		final OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener listener = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				callbackCount.countDown();
			}
		};

		sw.addResourceListener(listener, true);

		//		sw.addOptionalElement("controllable"); // this worked well in the test above.
		sw.controllable().create();
		sw.controllable().activate(true);
		sw.controllable().setValue(true);

		assertTrue("no resourceChanged callback", callbackCount.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void recursiveResourceChangeListenerRegistrationsAffectNewlyAddedReferences() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		final OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				callbackCount.countDown();
			}
		};

		sw.addResourceListener(l, true);

		sw2.controllable().create();
		sw.controllable().setAsReference(sw2.controllable());
		sw.controllable().activate(true);
		sw.controllable().setValue(true);

		assertTrue("no resourceChanged callback", callbackCount.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void recursiveResourceChangeListenerRegistrationsAffectNewlyAddedDecorators() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		final OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				callbackCount.countDown();
			}
		};

		sw.addResourceListener(l, true);

		BooleanResource res = sw.addDecorator("fnord", BooleanResource.class);
		res.activate(true);
		res.setValue(true);

		assertTrue("no resourceChanged callback", callbackCount.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void unregisteredListenersReceiveNoCallbacks() throws BrokenBarrierException, TimeoutException,
			InterruptedException {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException ex) {
					throw new RuntimeException(ex);
				}
			}
		};

		sw.controllable().create();
		sw.addResourceListener(l, true);
		sw.controllable().activate(true);
		sw.controllable().setValue(true);

		barrier.await(5, TimeUnit.SECONDS);
        assertFalse(barrier.isBroken());
		barrier.reset();
		sw.removeResourceListener(l);
		sw.controllable().setValue(false);
		try {
            barrier.await(5, TimeUnit.SECONDS);
			fail("removed listener received callback");
		} catch (TimeoutException te) {
            assertTrue(barrier.isBroken());
			// success!
		}
	}

	@Test
	public void nonRecursiveListenerRegistrationsAreNonRecursive() throws InterruptedException {
		final CountDownLatch cdl = new CountDownLatch(2);
		OnOffSwitch sw = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				cdl.countDown();
			}
		};
		sw.heatCapacity().create();
		IntegerResource r2 = sw.heatCapacity().addDecorator("foo", IntegerResource.class);

		sw.heatCapacity().addResourceListener(l, false);
		sw.activate(true);
		sw.heatCapacity().setValue(4711);
		r2.setValue(4712);
		assertFalse("surplus listener calls", cdl.await(5, TimeUnit.SECONDS));
		assertTrue("no listener callbacks received", cdl.getCount() < 2);
	}

	/**
	 * resources that are only reachable via reference cause no more callbacks once the reference is removed and when
	 * replacing references, the new reference inherits the listeners of the replaced reference
	 * @throws java.lang.Exception
	 */
	@Test
	public void changingReferencesCauseProperCallbacks() throws Exception {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.heatCapacity().create();
		sw2.heatCapacity().setAsReference(sw1.heatCapacity());
		sw1.activate(true);
		sw2.activate(true);
		final AtomicInteger lastVal = new AtomicInteger();

		final CyclicBarrier barrier = new CyclicBarrier(2);

		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				try {
					lastVal.set(Float.floatToIntBits(((FloatResource) resource).getValue()));
                    barrier.await();
				} catch (InterruptedException | BrokenBarrierException ex) {
					throw new RuntimeException(ex);
				}
			}
		};

		sw2.heatCapacity().addResourceListener(l, false);
		sw2.heatCapacity().setValue(42);
		barrier.await(5, TimeUnit.SECONDS);

		sw2.heatCapacity().setAsReference(sw2.addDecorator("whatever", ThermalEnergyCapacityResource.class));

		barrier.reset();
		sw2.heatCapacity().activate(true);
		// change detached reference
		sw1.heatCapacity().setValue(43);

		try {
			barrier.await(5, TimeUnit.SECONDS);
			fail("received listener callback on detached resource");
		} catch (TimeoutException te) {
			// success!
		}

		/* OTOH sw2.ratedSwitchingCurrent().setValue() should still cause a callback! */
		barrier.reset();
		sw2.heatCapacity().setValue(4711);
		barrier.await(5, TimeUnit.SECONDS);
		assertEquals(4711, Float.intBitsToFloat(lastVal.get()), 0.1f);

		barrier.reset();
		((FloatResource) sw2.getSubResource("whatever")).setValue(4712);
		barrier.await(5, TimeUnit.SECONDS);
		assertEquals(4712, Float.intBitsToFloat(lastVal.get()), 0.1f);
	}

	@Test
	public void listenersSurviveReferenceChanges() throws InterruptedException {
		CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
		CoolingDevice cooler2 = resMan.createResource(newResourceName(), CoolingDevice.class);
		PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
		PowerResource pwr2 = cooler2.electricityConnection().powerSensor().reading().create();

		cooler.activate(true);
		cooler2.activate(true);

		ChangeTestListener l = new ChangeTestListener(pwr, 1);
		pwr.addResourceListener(l, false);
		pwr.setValue(1);
		assertTrue(l.await());

		l.reset();

		cooler.electricityConnection().setAsReference(cooler2.electricityConnection());
		assertTrue(pwr.equalsLocation(pwr2));

		pwr2.setValue(2);
		assertTrue(l.await());
        
        l.reset();
        
        pwr.setValue(3);
		assertTrue(l.await());
	}
    
    @Test
	public void listenersSurviveReferenceChanges2() throws InterruptedException {
		CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
		CoolingDevice cooler2 = resMan.createResource(newResourceName(), CoolingDevice.class);
		PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
		PowerResource pwr2 = cooler2.electricityConnection().powerSensor().reading().create();

		cooler.activate(true);
		cooler2.activate(true);
        
		ValueTestListener<PowerResource> l = new ValueTestListener<>(getApplicationManager());
		pwr.addValueListener(l, false);
		pwr.setValue(1);
		assertTrue(l.await());

		l.reset();

		cooler.electricityConnection().setAsReference(cooler2.electricityConnection());
		assertTrue(pwr.equalsLocation(pwr2));

		pwr2.setValue(2);
		assertTrue(l.await());
        
        l.reset();
        
        pwr.setValue(3);
		assertTrue(l.await());
	}
    
    @Test
	public void listenersSurviveReferenceChangesOnADecoratorPath() throws InterruptedException {
        Resource top = resMan.createResource(newResourceName(), Resource.class);
        PowerSensor ps1 = top.addDecorator("ps1", PowerSensor.class);
        PowerSensor ps2 = top.addDecorator("ps2", PowerSensor.class);

        ps1.create();
        ps2.create();
		PowerResource pwr = ps1.reading().create();
		PowerResource pwr2 = ps2.reading().create();
        
		top.activate(true);
        
		ValueTestListener<PowerResource> l = new ValueTestListener<>(getApplicationManager());
		pwr.addValueListener(l, false);
		pwr.setValue(1);
		assertTrue(l.await());
        l.reset();

		ps1.setAsReference(ps2);
		assertTrue(pwr.equalsLocation(pwr2));

		pwr2.setValue(2);
		assertTrue(l.await());
        l.reset();
        
        pwr.setValue(3);
		assertTrue(l.await());
	}    
    
    @Test
	public void listenersSurviveReferenceChangesOnAListPath() throws InterruptedException {
        SensorDevice sd = resMan.createResource(newResourceName(), SensorDevice.class);
        PowerSensor ps1 = sd.sensors().add(PowerSensor.class);
        PowerSensor ps2 = sd.sensors().add(PowerSensor.class);
        ps1.create();
        ps2.create();
		PowerResource pwr = ps1.reading().create();
		PowerResource pwr2 = ps2.reading().create();
        
        AdminApplication aa = getApplicationManager().getAdministrationManager().getAppById(getApplicationManager().getAppID().getIDString());

		sd.activate(true);
        
		ValueTestListener<PowerResource> l = new ValueTestListener<>(getApplicationManager());
		pwr.addValueListener(l, false);
		pwr.setValue(1);
		assertTrue(l.await());
        l.reset();

		ps1.setAsReference(ps2);
		assertTrue(pwr.equalsLocation(pwr2));

		pwr2.setValue(2);
		assertTrue(l.await());
        l.reset();
        
        pwr.setValue(3);
		assertTrue(l.await());
	}    

	/** when a recursive listener registration affects resources which are only
	 * reachable via reference, the callbacks from those resources must not use
	 * the location resource but the resource path containing the reference.
	 * In other words, if A is the resource on which the listener was originally
	 * registered, then A must be an ancestor of every resource used as parameter
	 * by the resourceChanged callback (unless A itself is the parameter).
	 * @throws java.lang.InterruptedException
	 */
	@Test
	public void resourceChangedCallbacksOnDecoratorsUseCorrectResource() throws InterruptedException {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.location().geographicLocation().longitudeArcMinutes().create();
		sw2.location().setAsReference(sw1.location());

		final CountDownLatch cdl = new CountDownLatch(1);
		final Resource[] res = new Resource[1];
		final Object lock = new Object();
		org.ogema.core.resourcemanager.ResourceListener l = new org.ogema.core.resourcemanager.ResourceListener() {

			@Override
			public void resourceChanged(Resource resource) {
				synchronized (lock) {
					res[0] = resource;
				}
				cdl.countDown();
			}
		};

		sw1.activate(true);
		sw2.activate(true);

		sw2.addResourceListener(l, true);
		sw1.location().geographicLocation().longitudeArcMinutes().setValue(47.11f);

		assertTrue(cdl.await(10, TimeUnit.SECONDS));
		synchronized (lock) {
			assertTrue(res[0].getPath("/").contains(sw2.getName()));
		}
	}

	@Test
	public void resourceListenerCanBeRegisteredOnVirtualResource() throws InterruptedException {
		//... and works after the resource is realized as reference
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		ChangeTestListener lDirect = new ChangeTestListener(sw.settings().alarmLimits().upperLimit(), 1);
		ChangeTestListener lStoredReference = new ChangeTestListener(sw.settings().alarmLimits().upperLimit(), 1);

		final BooleanResource upperLimit = sw.settings().alarmLimits().upperLimit();
		final BooleanResource upperLimit2 = sw2.settings().alarmLimits().upperLimit();

		upperLimit.addResourceListener(lStoredReference, false);
		sw.settings().alarmLimits().upperLimit().addResourceListener(lDirect, false);

		upperLimit2.create();
		sw2.activate(true);
		assertTrue(upperLimit2.isActive());
		assertFalse(upperLimit.exists());

		sw.settings().create();
		sw.settings().alarmLimits().setAsReference(sw2.settings().alarmLimits());
		assertTrue(upperLimit.isActive());
		assertTrue(sw.settings().alarmLimits().upperLimit().isActive());

		upperLimit2.setValue(!upperLimit2.getValue());

		assertTrue("no callback received", lDirect.await());
		assertTrue("no callback received", lStoredReference.await());
	}

	static class ChangeTestListener implements org.ogema.core.resourcemanager.ResourceListener {

		Resource changedResource;
		int callbackCount;

		private CountDownLatch cbLatch;

		public ChangeTestListener(Resource changedResource, int callbackCount) {
			this.changedResource = changedResource;
			this.callbackCount = callbackCount;
			reset();
		}

		final public void reset() {
			cbLatch = new CountDownLatch(callbackCount);
		}

		public void setChangedResource(Resource changedResource) {
			this.changedResource = changedResource;
		}

		@Override
		public void resourceChanged(Resource resource) {
			if (changedResource != null) {
				assertEquals(changedResource, resource);
			}
			cbLatch.countDown();
		}

		public boolean await() throws InterruptedException {
			return cbLatch.await(5, TimeUnit.SECONDS);
		}

	}

}
