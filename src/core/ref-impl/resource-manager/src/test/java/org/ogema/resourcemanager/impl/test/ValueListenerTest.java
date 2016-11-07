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
package org.ogema.resourcemanager.impl.test;

import java.util.ArrayList;
import java.util.List;
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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.ValueTestListener;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
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
        
        ValueTestListenerLocal<PowerResource> l = new ValueTestListenerLocal<>();
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
        
        ValueTestListenerLocal<PowerResource> l = new ValueTestListenerLocal<>();
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
        
        ValueTestListenerLocal<PowerResource> l = new ValueTestListenerLocal<>();
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
        
        ValueTestListenerLocal<PowerResource> l = new ValueTestListenerLocal<>();
        pwr.addValueListener(l, true);
        pwr.setValue(47.11f);
        l.assertNoCallback();
        
        pwr.create();
        cooler.activate(true);
        
        pwr.setValue(1f);
        l.assertCallback();
    }

	@Test
    public void listenersSurviveReferenceChanges() throws InterruptedException {
        CoolingDevice cooler = resMan.createResource(newResourceName(), CoolingDevice.class);
        CoolingDevice cooler2 = resMan.createResource(newResourceName(), CoolingDevice.class);
        PowerResource pwr = cooler.electricityConnection().powerSensor().reading().create();
        PowerResource pwr2 = cooler2.electricityConnection().powerSensor().reading().create();
        
        cooler.activate(true);
        cooler2.activate(true);
        
        ValueTestListenerLocal<PowerResource> l = new ValueTestListenerLocal<>();
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

	static class ValueTestListenerLocal<T extends Resource> implements ResourceValueListener<T> {

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
	
	@Test
	public void callbacksForDirectReferencesWork() throws InterruptedException {
		String suffix = newResourceName();
		TemperatureResource sensor1 = resMan.createResource("temp1_" + suffix, TemperatureSensor.class).reading().create();
		TemperatureResource sensor2 = resMan.createResource("temp2_" + suffix, TemperatureSensor.class).reading().create();
		sensor1.activate(false);
		sensor2.activate(false);
		ValueTestListener<FloatResource> listener = new ValueTestListener<FloatResource>(getApplicationManager());
		sensor1.addValueListener(listener);
		sensor1.setAsReference(sensor2);
		sensor2.setValue(34F);
		listener.assertCallback();
		sensor1.removeValueListener(listener);
		resAcc.getResource("temp1_" + suffix).delete();
		resAcc.getResource("temp2_" + suffix).delete();
	}
	
	@Test
	public void callbacksForTransitiveDirectReferencesWork() throws InterruptedException {
		String suffix = newResourceName();
		TemperatureResource sensor1 = resMan.createResource("temp1_" + suffix, TemperatureSensor.class).reading().create();
		TemperatureResource sensor2 = resMan.createResource("temp2_" + suffix, TemperatureSensor.class).reading().create();
		TemperatureResource sensor3 = resMan.createResource("temp3_" + suffix, TemperatureSensor.class).reading().create();
		TemperatureResource sensor4 = resMan.createResource("temp4_" + suffix, TemperatureSensor.class).reading().create();
		sensor4.activate(false);
		
		ValueTestListener<FloatResource> listener = new ValueTestListener<FloatResource>(getApplicationManager());
		sensor1.setAsReference(sensor2);
		sensor1.addValueListener(listener);
		
		sensor3.setAsReference(sensor4);
		sensor2.setAsReference(sensor3); // now sensor1 references sensor4

		sensor4.setValue(34F);
		listener.assertCallback();
		sensor1.removeValueListener(listener);
		resAcc.getResource("temp1_" + suffix).delete();
		resAcc.getResource("temp2_" + suffix).delete();
		resAcc.getResource("temp3_" + suffix).delete();
		resAcc.getResource("temp4_" + suffix).delete();
	}
	
	@Test
	public void noExcessiveCallbacksFromReferences() throws InterruptedException {
		String suffix = newResourceName();
		TemperatureResource sensor1 = resMan.createResource("temp1_" + suffix, TemperatureSensor.class).reading().create();
		TemperatureResource sensor2 = resMan.createResource("temp2_" + suffix, TemperatureSensor.class).reading().create();
		sensor1.activate(false);
        sensor2.activate(false);
		
		ValueTestListener<FloatResource> listener = new ValueTestListener<FloatResource>(getApplicationManager());
		sensor1.addValueListener(listener);
		sensor1.setAsReference(sensor2);
		
		listener.reset(2); // we expect only one callback, but wait for up to a second if another one occurs
		sensor2.setValue(34F);
		listener.await(3, TimeUnit.SECONDS);
		Assert.assertEquals("Unexpected number of callbacks;", 1, listener.getNrCallbacks()) ;
	}
	
	@Test 
	public void valueListenerCallbacksWithOriginallyRegisteredResourceAsSource() throws InterruptedException {
		String suffix = newResourceName();
		CoolingDevice fridge1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class);
		CoolingDevice fridge2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class);
		fridge1.temperatureSensor().reading().create();
		fridge2.temperatureSensor().reading().create();
		fridge1.activate(true);
		fridge2.activate(true);
		
		ValueTestListener<FloatResource> listener = new ValueTestListener<FloatResource>(getApplicationManager());

		fridge1.temperatureSensor().reading().addValueListener(listener);
		fridge1.temperatureSensor().setAsReference(fridge2.temperatureSensor());
		
		// potential exceptions will occur in the listener thread, hence do not cause the test to fail
		// -> need to call listener.checkForExceptionsInListenerThread() at the end
		listener.setExpectedSource(fridge1.temperatureSensor().reading());
		fridge2.temperatureSensor().reading().setValue(89F);
		listener.assertCallback();
		listener.checkForExceptionsInListenerThread();
		
	}
	
	@Test
	public void valueListenerCallbacksWithOriginallyRegisteredResourceAsSource2() throws InterruptedException {
		String suffix = newResourceName();
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource("fridge_" + suffix, CoolingDevice.class);
		TemperatureSensor ts1 = getApplicationManager().getResourceManagement().createResource("tempSens_" + suffix, TemperatureSensor.class);
		fridge.location().room().temperatureSensor().setAsReference(ts1);
		fridge.temperatureSensor().setAsReference(ts1);
		ts1.reading().<TemperatureResource> create();
		fridge.activate(true);
		ts1.activate(true);
		ValueTestListener<TemperatureResource> listener = new ValueTestListener<TemperatureResource>(getApplicationManager());
		fridge.temperatureSensor().reading().addValueListener(listener);
		
		listener.setExpectedSource(fridge.temperatureSensor().reading()); // source should be the one by which the resource 
		ts1.reading().setCelsius(23.1F);
		boolean event = listener.await();
		Assert.assertTrue("Missing value changed event",event);
		
		listener.reset(2); // now expecting two callbacks
		
		List<TemperatureResource> expected = new ArrayList<TemperatureResource>();
		expected.add(fridge.location().room().temperatureSensor().reading()); 
		expected.add(fridge.temperatureSensor().reading()); 
		listener.setExpectedSources(expected); // we do not know which callback is issued first
		fridge.location().room().temperatureSensor().reading().addValueListener(listener);
		fridge.location().room().temperatureSensor().setAsReference(ts1);
		ts1.reading().setCelsius(53.1F);
		event = listener.await();
		Assert.assertTrue("Missing value changed event",event);
		listener.checkForExceptionsInListenerThread();
		
		fridge.temperatureSensor().reading().removeValueListener(listener);
		fridge.location().room().temperatureSensor().reading().removeValueListener(listener);
		fridge.delete();
		ts1.delete();

	}
	
	@Test 
	public void valueCallbackWorksForHigherLevelReferences() throws InterruptedException {
		Thermostat thermostat = resMan.createResource(newResourceName(), Thermostat.class);		
		TemperatureSensor sensor1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor sensor2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		
		thermostat.temperatureSensor().setAsReference(sensor1);
		sensor1.settings().setpoint().create();
		sensor2.settings().setpoint().create();
		sensor1.activate(true);
		sensor2.activate(true);
		thermostat.activate(true);
		ValueTestListener<FloatResource> listener = new ValueTestListener<>(getApplicationManager());
		thermostat.temperatureSensor().settings().setpoint().addValueListener(listener);
		sensor1.settings().setAsReference(sensor2.settings());
		sensor1.settings().setpoint().setValue(234);
		listener.assertCallback();
		listener.reset();
		sensor1.delete();
		sensor2.delete();
		thermostat.delete();
	}
	
	@Ignore("Callback missing")
	@Test
	public void doubleReferencesWork() throws InterruptedException {
		ElectricHeater a = resMan.createResource("a", ElectricHeater.class);
		Room b = resMan.createResource("b", Room.class);
		TemperatureSensor c = resMan.createResource("c", TemperatureSensor.class);
		c.reading().create();
		
		ValueTestListener<TemperatureResource> listener = new ValueTestListener<>(getApplicationManager());

		a.location().room().temperatureSensor().reading().addValueListener(listener);
		assertFalse(a.location().room().temperatureSensor().reading().exists());

		a.location().room().setAsReference(b);
		assertTrue(b.equalsLocation(a.location().room()));
		
		b.temperatureSensor().setAsReference(c); // <-- broken!
		assertTrue(a.location().room().temperatureSensor().reading().exists());

		System.out.println("registered value listeners:");
		for (RegisteredValueListener rsl : getApplicationManager().getAdministrationManager().getAppById(
				getApplicationManager().getAppID().toString()).getValueListeners()) {
			System.out.printf("%s: %s%n", rsl.getResource(), rsl.getValueListener());
		}
		
		c.reading().activate(false);
		c.reading().setValue(4000.1F);
		assertTrue("missing value callback", listener.await(5, TimeUnit.SECONDS));
		a.location().room().temperatureSensor().reading().removeValueListener(listener);
		c.delete();
		b.delete();
		a.delete();
	}
	
	@Test 
	public void highlyNestedReferencesWork() throws InterruptedException {
		TemperatureSensor thermo0 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor thermo1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor thermo2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor thermo3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor thermo4 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor thermo5 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		
		ValueTestListener<TemperatureResource> listener = new ValueTestListener<>(getApplicationManager());
		thermo0
			.location().room().temperatureSensor()
			.location().room().temperatureSensor()
			.location().room().temperatureSensor()
			.location().room().temperatureSensor()
			.location().room().temperatureSensor().reading().addValueListener(listener);
		thermo5.reading().create().activate(false);;
		thermo4.location().room().temperatureSensor().setAsReference(thermo5);
		thermo3.location().room().temperatureSensor().setAsReference(thermo4);
		thermo2.location().room().temperatureSensor().setAsReference(thermo3);
		thermo1.location().room().temperatureSensor().setAsReference(thermo2);
		thermo0.location().room().temperatureSensor().setAsReference(thermo1);
		thermo5.reading().setValue(12345);
		Assert.assertTrue("Missing value callback",listener.await(5, TimeUnit.SECONDS));
		thermo5.reading().activate(false);
		thermo3.delete();
		thermo4.delete();
		thermo5.delete();
		thermo0.delete();
		thermo2.delete();
		thermo1.delete();
	}
	
	@Test
	public void valueCallbackForNewDeepReferenceWorks() throws InterruptedException {
		Thermostat thermo = resMan.createResource(newResourceName(), Thermostat.class);
		TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		ValueTestListener<TemperatureResource> listener = new ValueTestListener<>(getApplicationManager());
		thermo.temperatureSensor().location().room().temperatureSensor().settings().setpoint().addValueListener(listener);
		tempSens.location().room().temperatureSensor().settings().setpoint().create().activate(false);;
		thermo.temperatureSensor().setAsReference(tempSens);
		tempSens.location().room().temperatureSensor().settings().setpoint().setCelsius(12.32F);
		Assert.assertTrue("Missing value callback", listener.await(5, TimeUnit.SECONDS));
		thermo.delete();
		tempSens.delete();
	}

}
