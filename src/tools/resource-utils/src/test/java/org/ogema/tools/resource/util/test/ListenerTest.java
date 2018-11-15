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
package org.ogema.tools.resource.util.test;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.exam.ExceptionHandler;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ValueTestListener;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.listener.util.TransitiveStructureListener;
import org.ogema.tools.listener.util.TransitiveValueListener;
import org.ogema.tools.resource.util.ListenerUtils;
import org.ogema.tools.resource.util.test.tools.OrderedStructureTestListener;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests for the {@link ListenerUtils} methods. Note that the tests are prone to runtime problems 
 * (which is true for the transitive listeners in general).
 * 
 * @author cnoelle
 */
@ExamReactorStrategy(PerClass.class)
public class ListenerTest extends OsgiAppTestBase {
	
	private final ExceptionHandler handler = new ExceptionHandler();
	
	@Override
	public void doBefore() {
		handler.reset();
		getApplicationManager().addExceptionListener(handler); 
	}
	
	@Override
	public void doAfter() {
		handler.checkForExceptionsInOtherThreads(); // rethrow AssertionErrors from other threads in the test thread, so that the tests can fail
	}
	
	
	@Test
	public void transitiveValueListenerWorksOnTopNode() throws InterruptedException {
		StringResource topString = getApplicationManager().getResourceManagement().createResource("a", StringResource.class);
		topString.activate(false);
		ValueTestListener<ValueResource> listener = new ValueTestListener<ValueResource>(getApplicationManager());
		TransitiveValueListener<?> tvl = ListenerUtils.registerTransitiveValueListener(topString, listener);
		
		String newValue = "testtesttest";
		listener.setExpectedSource(topString);
		listener.setExpectedValue(newValue);
		topString.setValue(newValue);
		boolean event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		listener.reset();
		
		tvl.destroy();
		
		newValue = "test2test2test2";
		listener.setExpectedSource(topString);
		listener.setExpectedValue(newValue);
		topString.setValue(newValue);
		event = listener.await(1,TimeUnit.SECONDS);
		Assert.assertFalse("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		topString.delete();
	}
	
	@Test
	public void transitiveValueListenerWorksForExistingSubresources() throws InterruptedException {
		// setup
		CoolingDevice cd = getApplicationManager().getResourceManagement().createResource("b", CoolingDevice.class);
		IntegerResource ir1 = cd.temperatureSensor().settings().controlLimits().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		IntegerResource ir2 = cd.temperatureSensor().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		cd.name().create();
		cd.activate(true);
		ValueTestListener<ValueResource> listener = new ValueTestListener<ValueResource>(getApplicationManager());
		TransitiveValueListener<?> tvl = ListenerUtils.registerTransitiveValueListener(cd, listener);
		// tests
		int kelvin = 312;
		listener.setExpectedSource(ir1);
		listener.setExpectedValue(String.valueOf(kelvin));
		ir1.setValue(kelvin);
		boolean event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		listener.reset();
		
		kelvin = 291;
		listener.setExpectedSource(ir2);
		listener.setExpectedValue(String.valueOf(kelvin));
		ir2.setValue(kelvin);
		event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		listener.reset();
		
		String newName = "testtesttest";
		listener.setExpectedSource(cd.name());
		listener.setExpectedValue(newName);
		cd.name().setValue(newName);
		event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		listener.reset();
		
		tvl.destroy();
		cd.delete();
	}
	
	@Test
	public void transitiveValueListenerWorksForNewSubresources() throws InterruptedException {
		CoolingDevice cd = getApplicationManager().getResourceManagement().createResource("c", CoolingDevice.class);
		ValueTestListener<ValueResource> listener = new ValueTestListener<ValueResource>(getApplicationManager());
		TransitiveValueListener<?> tvl = ListenerUtils.registerTransitiveValueListener(cd, listener);
		IntegerResource ir1 = cd.temperatureSensor().settings().controlLimits().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		IntegerResource ir2 = cd.temperatureSensor().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		cd.name().create();
		cd.activate(true);
		wait(100); // ensure structure callbacks for the above subresources have been executed; this is essential for the test to work
		// tests
		int kelvin = 312;
		listener.setExpectedSource(ir1);
		listener.setExpectedValue(String.valueOf(kelvin));
		ir1.setValue(kelvin);
		boolean event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		listener.reset();

		kelvin = 291;
		listener.setExpectedSource(ir2);
		listener.setExpectedValue(String.valueOf(kelvin));
		ir2.setValue(kelvin);
		event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		
		listener.reset();
		String newName = "testtesttest";
		listener.setExpectedSource(cd.name());
		listener.setExpectedValue(newName);
		cd.name().setValue(newName);
		event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);	
		listener.checkForExceptionsInListenerThread();
		
		tvl.destroy();
		listener.setExpectedSource(null);
		listener.setExpectedValue(null);
		cd.delete();
	}
	
	@Test
	public void transitiveListenerWorksForRestrictedResourceType() throws InterruptedException {
		CoolingDevice cd = getApplicationManager().getResourceManagement().createResource("d", CoolingDevice.class);
		ValueTestListener<IntegerResource> listener = new ValueTestListener<IntegerResource>(getApplicationManager());
		TransitiveValueListener<IntegerResource> tvl = ListenerUtils.registerTransitiveValueListener(cd, listener, IntegerResource.class, true);
		IntegerResource ir1 = cd.temperatureSensor().settings().controlLimits().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		cd.activate(true);
		wait(100); // ensure structure callbacks for the above subresources have been executed; this is essential for the test to work
		// tests
		int kelvin = 312;
		listener.setExpectedSource(ir1);
		listener.setExpectedValue(String.valueOf(kelvin));
		ir1.setValue(kelvin);
		boolean event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();

		listener.reset();
		cd.name().create().activate(false);
		String newName = "testtesttest";
		cd.name().setValue(newName);
		event = listener.await(1,TimeUnit.SECONDS);
		Assert.assertFalse("Unexpected valueChanged callback",event);	
		
		tvl.destroy();
		cd.delete();
	}
	
	@Test
	public void deleteWorksForTransitiveValueListener() throws InterruptedException {
		CoolingDevice cd = getApplicationManager().getResourceManagement().createResource("e", CoolingDevice.class);
		IntegerResource ir1 = cd.temperatureSensor().settings().controlLimits().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		ValueTestListener<ValueResource> listener = new ValueTestListener<ValueResource>(getApplicationManager());
		TransitiveValueListener<?> tvl = ListenerUtils.registerTransitiveValueListener(cd, listener);
		IntegerResource ir2 = cd.temperatureSensor().getSubResource("testRes", IntegerResource.class).<IntegerResource> create();
		cd.name().create();
		cd.activate(true);
		wait(100); // ensure structure callbacks for the above subresources have been executed; this is essential for the test to work
		// tests
		int kelvin = 312;
		listener.setExpectedSource(ir1);
		listener.setExpectedValue(String.valueOf(kelvin));
		ir1.setValue(kelvin);
		boolean event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		listener.reset();
		
		tvl.destroy(); // now we should not receive any further callbacks
		
		kelvin = 291;
		ir2.setValue(kelvin);
		event = listener.await(1,TimeUnit.SECONDS);
		Assert.assertFalse("Unexpected valueChanged callback",event);	
		
		String newName = "testtesttest";
		cd.name().setValue(newName);
		event = listener.await(1,TimeUnit.SECONDS);
		Assert.assertFalse("Unexpected valueChanged callback",event);	
		
		cd.delete();
	}
	
	@Test
	public void transitiveValueListenerWorksWithReferences() throws InterruptedException {
		CoolingDevice cd = getApplicationManager().getResourceManagement().createResource(newResourceName(), CoolingDevice.class);
		TemperatureSensor ts = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		ts.name().create();
		ValueTestListener<ValueResource> listener = new ValueTestListener<>(getApplicationManager());
		TransitiveValueListener<?> tvl = ListenerUtils.registerTransitiveValueListener(cd, listener);
		
		ts.activate(true);
		cd.temperatureSensor().setAsReference(ts);
		cd.name().create();
		cd.activate(true);
		
		wait(100);

		listener.reset();  
		// this one is a reference
		listener.setExpectedSource(cd.temperatureSensor().name());
		String value = "test2";
		listener.setExpectedValue(value);
		ts.name().setValue(value);
		boolean event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		
		// this one is not a reference
		listener.reset();	
		listener.setExpectedSource(cd.name());
		value = "test";
		listener.setExpectedValue(value);
		cd.name().setValue(value);
		event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		
		// need to ensure that this does not lead to a listener registration on ts.name() again...
		ts.location().room().temperatureSensor().setAsReference(ts); // now we have created a loop
		ts.location().room().name().create().activate(false);
		wait(100);
		
		// this one is a reference
		listener.setExpectedSource(cd.temperatureSensor().name());
		listener.reset(2); // in fact we expect only one callback, but since there is a loop in the resoure tree, we check that really only one callback is issued
		value = "test3";
		listener.setExpectedValue(value);
		ts.name().setValue(value);
		event = listener.await(1,TimeUnit.SECONDS);
		Assert.assertFalse("Too many valueChanged callbacks",event);
		listener.checkForExceptionsInListenerThread();

//		Assert.assertFalse("Unexpected double callback",listener.await(1,TimeUnit.SECONDS)); //there is in fact a double callback,
							// but this test does not capture it!
		
		
		listener.reset();
		// this one is a reference
		listener.setExpectedSource(cd.temperatureSensor().location().room().name());
		value = "test4";
		listener.setExpectedValue(value);
		ts.location().room().name().setValue(value);
		event = listener.await();
		Assert.assertTrue("Missing valueChanged callback",event);
		listener.checkForExceptionsInListenerThread();
		
		tvl.destroy();
		cd.delete();
		wait(10);
		ts.delete(); // NullPointer!
	}
	
	//TODO test value transitive listener in case of removed references
	
	@Test 
	public void transitiveStructureListenerWorksOnTopNode() throws InterruptedException {
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource("ra", CoolingDevice.class);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(fridge, listener, getApplicationManager());
		
		listener.setExpectedSource(fridge);
		
		listener.setExpectedChangedResource(fridge);
		fridge.activate(false);
		boolean event = listener.awaitActivate(5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing activate event",event);
		
		listener.setExpectedChangedResource(fridge.temperatureSensor());
		fridge.temperatureSensor().create();
		event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		
		listener.setExpectedChangedResource(fridge);
		fridge.delete();
		event = listener.awaitEvent(EventType.RESOURCE_DELETED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing DELETED event",event);

		tsl.destroy();
	}
	
	@Test 
	public void transitiveStructureListenerWorksOnExistingSubresources() throws InterruptedException {
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource("rb", CoolingDevice.class);
		fridge.temperatureSensor().settings().controlLimits().upperLimit().create();
		fridge.activate(true);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(fridge, listener, getApplicationManager());
		
		listener.setExpectedSource(fridge);
		listener.setExpectedChangedResource(fridge.temperatureSensor().settings().alarmLimits());
		fridge.temperatureSensor().settings().alarmLimits().create();
		boolean event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset();

		TemperatureRange range = getApplicationManager().getResourceManagement().createResource("rb_range", TemperatureRange.class);
		listener.setExpectedChangedResource(fridge.temperatureSensor().settings().targetRange());
		fridge.temperatureSensor().settings().targetRange().setAsReference(range);
		event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event); 

		//		
		listener.setExpectedChangedResource(null); // we do not want to insist on the order in which the subresource deleted callbacks occur
		fridge.temperatureSensor().settings().delete(); // triggers multiple callbacks, for all subresources
		event = listener.awaitEvent(EventType.SUBRESOURCE_REMOVED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_REMOVED event",event);
		listener.reset();
//		
		tsl.destroy();
		listener.setExpectedSource(null);
		fridge.delete();
		event = listener.awaitEvent(EventType.RESOURCE_DELETED, 1, TimeUnit.SECONDS);
		Assert.assertFalse("Unexpected DELETED event",event);
	}
	
	@Test
	public void noSpuriousCallbacksFromTransitiveStructureListener() throws InterruptedException {
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource("rb", CoolingDevice.class);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(fridge, listener, getApplicationManager());
		
		listener.reset(2); // we expect only one event, just to be sure no second one will occur
		fridge.temperatureSensor().create();
		Assert.assertFalse("Spurious SUBRESOURCE_ADDED event", listener.awaitEvent(EventType.SUBRESOURCE_ADDED,2,TimeUnit.SECONDS));
		Assert.assertEquals("Spurious RESOURCE_CREATED event", 0, listener.getEventCount(EventType.RESOURCE_CREATED));
		tsl.destroy();
		fridge.delete();
	}
	
	@Test 
	public void transitiveStructureListenerWorksOnNewSubresources() throws InterruptedException {
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource("rb", CoolingDevice.class);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(fridge, listener, getApplicationManager());
		
		fridge.temperatureSensor().settings().controlLimits().upperLimit().create();
		fridge.activate(true);
		wait(500); // this causes many callbacks, we need to wait until they have been executed for sure
		listener.reset();
		
		listener.setExpectedSource(fridge);
		
		listener.setExpectedChangedResource(fridge.temperatureSensor().settings().alarmLimits());
		fridge.temperatureSensor().settings().alarmLimits().create();
		boolean event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset();
		TemperatureRange range = getApplicationManager().getResourceManagement().createResource("rb_range", TemperatureRange.class);
		listener.setExpectedChangedResource(fridge.temperatureSensor().settings().targetRange());
		fridge.temperatureSensor().settings().targetRange().setAsReference(range);
		event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		
		listener.setExpectedChangedResource(null);
		fridge.temperatureSensor().settings().delete();
		event = listener.awaitEvent(EventType.SUBRESOURCE_REMOVED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_REMOVED event",event);
		
		tsl.destroy();
		fridge.delete();
		event = listener.awaitEvent(EventType.RESOURCE_DELETED, 1, TimeUnit.SECONDS);
		Assert.assertFalse("Unexpected DELETED event",event);
	}
	
	@Test
	public void transitiveStructureListenerWorksWithReferences() throws InterruptedException {
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource(newResourceName(), CoolingDevice.class);
		TemperatureSensor ts1 = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor ts2 = getApplicationManager().getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		fridge.temperatureSensor().setAsReference(ts1);
		ts1.settings().controlLimits().upperLimit().create();
		fridge.activate(true);
		ts1.activate(true);
		ts2.activate(true);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(fridge, listener, getApplicationManager());

		listener.setExpectedSource(fridge);
		
		listener.setExpectedChangedResource(ts1.settings().alarmLimits());
		fridge.temperatureSensor().settings().alarmLimits().create();
		boolean event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset();
		
		listener.setExpectedChangedResource(null);
		ts1.location().room().temperatureSensor().setAsReference(ts2); // causes a couple of callbacks
		ts2.settings().create();
		wait(500); // wait long enough for all callbacks to be executed -> timing sensitive
		listener.reset();
		
		listener.setExpectedChangedResource(ts1.location().room().temperatureSensor().settings().alarmLimits());
		ts2.settings().alarmLimits().create();
		event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset(2);
		
		listener.setExpectedChangedResource(ts2.settings());
		ts2.settings().delete();
		event = listener.awaitEvent(EventType.SUBRESOURCE_REMOVED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_REMOVED event",event);
		listener.reset();
	
		listener.setExpectedChangedResource(null); // many different callbacks should occur
		ts1.delete();
		// TODO not implemented yet -> at this point the location-based storing of registered listeners fails
/*
		event = listener.awaitEvent(EventType.SUBRESOURCE_REMOVED, 5, TimeUnit.SECONDS); // actually a couple of callbacks are expected
		Assert.assertTrue("Missing SUBRESOURCE_REMOVED event",event);
		listener.reset();
*/
		
		tsl.destroy();
		fridge.delete();
		ts2.delete();
	}
	
    @Ignore("not implemented yet")
	@Test
	public void transitiveStructureListenerWorksWithChangedReferences() throws InterruptedException {
		CoolingDevice cd1 = getApplicationManager().getResourceManagement().createResource(newResourceName(), CoolingDevice.class);
		CoolingDevice cd2 = getApplicationManager().getResourceManagement().createResource(newResourceName(), CoolingDevice.class);
		cd2.temperatureSensor().location().room().temperatureSensor().settings().create();
		cd1.location().room().temperatureSensor().setAsReference(cd2.temperatureSensor());
		cd1.activate(true);
		cd2.activate(true);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(cd1, listener, getApplicationManager());
		wait(100);
		listener.setExpectedSource(cd1);
		
		// this should not be problematic
		listener.setExpectedChangedResource(cd1.location().room().temperatureSensor().settings().targetRange());
		cd2.temperatureSensor().settings().targetRange().create();
		boolean event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset();
		// now we add another references
		listener.setExpectedChangedResource(cd1.temperatureSensor());
		cd1.temperatureSensor().setAsReference(cd2.temperatureSensor());
		event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset();
		listener.setExpectedChangedResource(null);
		// now we remove the old reference
		cd1.location().room().delete();
		// FIXME
		System.out.println("          --- deleted ---");
		System.out.flush();;
		wait(200); // takes some time to execute all the listener callbacks
		listener.reset();
		// for this to work, the previous structure listener on the deleted path 
		// must have been transferred to the newly created reference
		listener.setExpectedChangedResource(cd1.temperatureSensor().location().room().temperatureSensor().settings().alarmLimits());
		cd2.temperatureSensor().location().room().temperatureSensor().settings().alarmLimits().create();
		event = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_ADDED event",event);
		listener.reset();
		
		tsl.destroy();
		cd1.delete();
		cd2.delete();
	}
	
	@Test
	public void transitiveStructureListenerDeleteCallbacksInExpectedOrder() throws InterruptedException {
		CoolingDevice fridge = getApplicationManager().getResourceManagement().createResource("rd", CoolingDevice.class);
		fridge.temperatureSensor().settings().controlLimits().upperLimit().create();
		fridge.activate(true);
		OrderedStructureTestListener listener = new OrderedStructureTestListener(getApplicationManager());
		TransitiveStructureListener tsl = ListenerUtils.registerTransitiveStructureListener(fridge, listener, getApplicationManager());
		wait(100); // wait for 1s -> this causes many callbacks
		
		listener.reset();
		
		listener.addExpectedChangedResource(fridge.temperatureSensor().settings().controlLimits().upperLimit());
		listener.addExpectedChangedResource(fridge.temperatureSensor().settings().controlLimits());
		listener.addExpectedChangedResource(fridge.temperatureSensor().settings());
		listener.addExpectedChangedResource(fridge.temperatureSensor());
		
		fridge.temperatureSensor().delete();
		boolean event = listener.awaitQueue(5, TimeUnit.SECONDS);
		Assert.assertTrue("Missing SUBRESOURCE_REMOVED event", event);
		
		tsl.destroy();
		fridge.delete();
	}
	
	// wait for length * 10ms
	private static void wait(int length) throws InterruptedException {
		for (int i=0;i<length;i++) {
			Thread.sleep(10); 
		}
	}
	
}
