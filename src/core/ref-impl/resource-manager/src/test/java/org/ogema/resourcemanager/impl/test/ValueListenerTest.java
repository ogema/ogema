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
package org.ogema.resourcemanager.impl.test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ValueListenerTest extends OsgiTestBase {

	@Test
    public void callbacksWorksOnChange() throws InterruptedException {
        CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
        PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
        
        ValueTestListener<PowerResource> l = new ValueTestListener<>();
        pwr.addValueListener(l);
        
        cooler.activate(true);
        pwr.setValue(47.11f);
        
        l.assertCallback();
        assertTrue(pwr.removeValueListener(l));
    }

	@Test
    public void noCallbackWhenNewValueIsSameAsOld() throws InterruptedException {
        CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
        PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
        
        ValueTestListener<PowerResource> l = new ValueTestListener<>();
        cooler.activate(true);
        
        pwr.addValueListener(l);
        pwr.setValue(47.11f);
        l.assertCallback();
        
        l.reset();
        pwr.setValue(47.11f);
        l.assertNoCallback();
        
        assertTrue(pwr.removeValueListener(l));
    }

	@Test
    public void callbacksWorksOnEveryUpdateIfRequested() throws InterruptedException {
        CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
        PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
        
        ValueTestListener<PowerResource> l = new ValueTestListener<>();
        cooler.activate(true);
        pwr.setValue(47.11f);
        
        pwr.addValueListener(l, true);
        pwr.setValue(47.11f);
        l.assertCallback();
        
        l.reset();
        pwr.setValue(47.11f);
        l.assertCallback();
        
        assertTrue(pwr.removeValueListener(l));
    }

	@Test
    public void listenerCanBeRegisteredOnVirtualResource() throws InterruptedException {
        CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
        PowerResource pwr = cooler.electricityConnection().powerSensor().reading();
        
        ValueTestListener<PowerResource> l = new ValueTestListener<>();
        pwr.addValueListener(l, true);
        pwr.setValue(47.11f);
        l.assertNoCallback();
        
        pwr.create();
        cooler.activate(true);
        
        pwr.setValue(1f);
        l.assertCallback();
    }

	//FIXME
	//@Ignore("FIXME")
	@Test
    public void listenersSurviveReferenceChanges() throws InterruptedException {
        CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
        CoolingDevice cooler2 = resMan.createResource(newResourceName(), CoolingDevice.class);
        PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
        PowerResource pwr2 = cooler2.electricityConnection().powerSensor().reading().create();
        
        cooler.activate(true);
        cooler2.activate(true);
        
        ValueTestListener<PowerResource> l = new ValueTestListener<>();
        pwr.addValueListener(l);
        pwr.setValue(1);
        l.assertCallback();
        
        l.reset();
        
        cooler.electricityConnection().setAsReference(cooler2.electricityConnection());
        assertTrue(pwr.equalsLocation(pwr2));
        
        pwr2.setValue(2);
        l.assertCallback();
    }

	@Test
	public void changingReferencesCauseProperCallbacks() throws Exception {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		sw1.heatCapacity().create();
		sw2.heatCapacity().setAsReference(sw1.heatCapacity());
		sw1.activate(true);
		sw2.activate(true);
		final AtomicInteger lastVal = new AtomicInteger();

		final CyclicBarrier barrier = new CyclicBarrier(2);

		ResourceValueListener<FloatResource> l = new ResourceValueListener<FloatResource>() {

			@Override
			public void resourceChanged(FloatResource resource) {
				try {
					lastVal.set(Float.floatToIntBits(resource.getValue()));
                    barrier.await();
				} catch (InterruptedException | BrokenBarrierException ex) {
					throw new RuntimeException(ex);
				}
			}
		};

		sw2.heatCapacity().addValueListener(l, true);
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

	static class ValueTestListener<T extends Resource> implements ResourceValueListener<T> {

		final int timeout = 3;

		private CountDownLatch callbackLatch = new CountDownLatch(1);

		//public ValueTestListener(T expectedResource)

		@Override
		public void resourceChanged(T resource) {
			System.out.printf("resourceChanged: %s%n", resource.getPath());
			callbackLatch.countDown();
		}

		public void assertCallback() throws InterruptedException {
			assertTrue(callbackLatch.await(timeout, TimeUnit.SECONDS));
		}

		public void assertNoCallback() throws InterruptedException {
			assertFalse(callbackLatch.await(timeout, TimeUnit.SECONDS));
		}

		public void reset() {
			callbackLatch = new CountDownLatch(1);
		}

	}

}
