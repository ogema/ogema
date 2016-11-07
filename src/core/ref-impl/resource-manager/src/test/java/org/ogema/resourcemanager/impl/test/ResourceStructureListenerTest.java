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

import java.util.concurrent.CountDownLatch;

import org.ogema.exam.StructureTestListener;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;

import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.REFERENCE_ADDED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.REFERENCE_REMOVED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.RESOURCE_ACTIVATED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.RESOURCE_CREATED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.RESOURCE_DEACTIVATED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.RESOURCE_DELETED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.SUBRESOURCE_ADDED;
import static org.ogema.core.resourcemanager.ResourceStructureEvent.EventType.SUBRESOURCE_REMOVED;

import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ResourceStructureListenerTest extends OsgiTestBase {

	@Test
	public void activatingResourceCausesCallback() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		sw.stateControl().create();

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateControl());
		l.setExpectedChangedResource(sw.stateControl());

		sw.stateControl().addStructureListener(l);
		assertFalse(sw.stateControl().isActive());
		assertFalse(l.eventReceived(RESOURCE_ACTIVATED));
		sw.activate(true);
		assertTrue(sw.stateControl().isActive());

		assertTrue("no callback received", l.awaitActivate(5, TimeUnit.SECONDS));
	}

	@Test
	public void deactivatingResourceCausesCallback() throws InterruptedException {
		OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		sw.stateControl().create();

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateControl());
		l.setExpectedChangedResource(sw.stateControl());

		sw.stateControl().addStructureListener(l);
		assertFalse(sw.stateControl().isActive());
		sw.activate(true);
		assertTrue(sw.stateControl().isActive());
		assertFalse(l.eventReceived(RESOURCE_DEACTIVATED));

		sw.deactivate(true);
		assertFalse(sw.stateControl().isActive());

		assertTrue("no callback received", l.awaitEvent(RESOURCE_DEACTIVATED));
	}

	@Test
	public void addingSubResourceCausesCallback() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw);
		l.setExpectedChangedResource(sw.stateControl());

		sw.addStructureListener(l);
		assertFalse(sw.stateControl().exists());
		assertFalse(l.eventReceived(SUBRESOURCE_ADDED));
		sw.stateControl().create();

		assertTrue("no callback received", l.awaitEvent(SUBRESOURCE_ADDED));
	}

	@Test
	public void addingSubResourceCausesCallbackForReferences() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw2.stateFeedback());
		l.setExpectedChangedResource(sw2.stateFeedback().forecast());

		assertFalse(sw.stateFeedback().exists());
		sw.stateFeedback().create();
		sw2.stateFeedback().setAsReference(sw.stateFeedback());

		sw2.stateFeedback().addStructureListener(l);
		assertFalse(l.eventReceived(SUBRESOURCE_ADDED));
		sw.stateFeedback().forecast().create();
		assertTrue("no callback received", l.awaitEvent(SUBRESOURCE_ADDED));
	}

	@Test
	public void createdCallbackWorksWhenUsingCreate() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateFeedback());
		l.setExpectedChangedResource(sw.stateFeedback());

		sw.stateFeedback().addStructureListener(l);
		assertFalse(sw.stateFeedback().exists());
		assertFalse(l.eventReceived(RESOURCE_CREATED));

		sw.stateFeedback().create();

		assertTrue("no callback received", l.awaitEvent(RESOURCE_CREATED));
	}

	@Test
	public void createdCallbackWorksWhenUsingAddOptional() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateFeedback());
		l.setExpectedChangedResource(sw.stateFeedback());

		sw.stateFeedback().addStructureListener(l);
		assertFalse(sw.stateFeedback().exists());
		assertFalse(l.eventReceived(RESOURCE_CREATED));

		sw.addOptionalElement("stateFeedback");

		assertTrue("no callback received", l.awaitEvent(RESOURCE_CREATED));
	}

	@Test
	public void createdCallbackWorksWhenSettingReference() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateFeedback());
		l.setExpectedChangedResource(sw.stateFeedback());

		sw.stateFeedback().addStructureListener(l);
		sw2.stateFeedback().create();
		assertFalse(sw.stateFeedback().exists());
		assertFalse(l.eventReceived(RESOURCE_CREATED));

		sw.stateFeedback().setAsReference(sw2.stateFeedback());

		assertTrue("no callback received", l.awaitEvent(RESOURCE_CREATED));
	}

	@Test
	public void createdCallbackWorksForASubresourceOfAReference() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.settings().alarmLimits().upperLimit());
		l.setExpectedChangedResource(sw.settings().alarmLimits().upperLimit());

		final BooleanResource upperLimit = sw.settings().alarmLimits().upperLimit();

		sw2.settings().alarmLimits().upperLimit().create();

		upperLimit.addStructureListener(l);
		assertFalse(upperLimit.exists());

		sw.settings().create();
		assertFalse(l.eventReceived(RESOURCE_CREATED));
		sw.settings().alarmLimits().setAsReference(sw2.settings().alarmLimits());

		assertTrue("no callback received", l.awaitEvent(RESOURCE_CREATED));
		assertTrue("remove listener failed", upperLimit.removeStructureListener(l));
	}

	@Test
	public void referenceChangeEventsWork() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateControl());
		l.setExpectedChangedResource(sw2);

		sw.stateControl().create();
		sw.stateControl().addStructureListener(l);

		sw2.stateControl().setAsReference(sw.stateControl());
		assertTrue(sw.stateControl().equalsLocation(sw2.stateControl()));
		assertFalse(sw.stateControl().getReferencingResources(OnOffSwitch.class).isEmpty());
		assertTrue(l.awaitEvent(REFERENCE_ADDED));

		//sw2.stateControl().create(); resource already exists! (as reference)
		sw2.addOptionalElement("stateControl");
		assertFalse(sw.stateControl().equalsLocation(sw2.stateControl()));
		assertTrue(sw.stateControl().getReferencingResources(OnOffSwitch.class).isEmpty());
		assertTrue(l.awaitEvent(REFERENCE_REMOVED));
	}

	@Test
	public void deletedCallbackWorksWhenUsingDelete() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.stateFeedback());
		l.setExpectedChangedResource(sw.stateFeedback());

		sw.stateFeedback().addStructureListener(l);
		sw.stateFeedback().create();
		assertTrue(sw.stateFeedback().exists());
		assertFalse(l.eventReceived(RESOURCE_DELETED));
		sw.stateFeedback().delete();

		assertTrue("no callback received", l.awaitEvent(RESOURCE_DELETED));
	}

	@Test
	public void subresourceRemovedCallbackWorksWhenDeletingSubresource() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw);
		l.setExpectedChangedResource(sw.stateFeedback());

		sw.addStructureListener(l);
		sw.stateFeedback().create();
		assertTrue(sw.stateFeedback().exists());
		assertFalse(l.eventReceived(SUBRESOURCE_REMOVED));
		sw.stateFeedback().delete();

		assertTrue("no callback received", l.awaitEvent(SUBRESOURCE_REMOVED));
	}

	@Test
	public void subresourceRemovedCallbackWorksForReferencesToDeletedSubresources() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw);
		l.setExpectedChangedResource(sw.stateFeedback());

		sw.addStructureListener(l);
		sw2.stateFeedback().create();
		assertFalse(sw.stateFeedback().exists());
		sw.stateFeedback().setAsReference(sw2.stateFeedback());
		//setAsReference WILL cause a SUBRESOURCE_REMOVED cb, followed by SUBRESOURCE_ADDED!
		assertTrue(sw.stateFeedback().exists());
		sw2.stateFeedback().delete();
		assertFalse(sw.stateFeedback().exists());
		assertFalse(sw2.stateFeedback().exists());

		assertTrue("no callback received", l.awaitEvent(SUBRESOURCE_REMOVED));
	}

	/* like {@link #referenceChangeEventsWork() } but with reference replace instead addOptional() */
	@Test
	public void referenceRemovedEventWorksWhenReplacingReference() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw3 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw2.stateFeedback());
		l.setExpectedChangedResource(sw);

		sw2.stateFeedback().addStructureListener(l);
		sw2.stateFeedback().create();
		sw3.stateFeedback().create();

		sw.stateFeedback().setAsReference(sw2.stateFeedback());
		assertTrue(sw.stateFeedback().exists());
		assertTrue(sw.stateFeedback().isReference(false));
		assertFalse(l.eventReceived(REFERENCE_REMOVED));
		sw.stateFeedback().setAsReference(sw3.stateFeedback());
		assertTrue(sw.stateFeedback().isReference(false));
		assertTrue(sw.stateFeedback().equalsLocation(sw3.stateFeedback()));

		assertTrue("no callback received", l.awaitEvent(REFERENCE_REMOVED));
	}

	@Test
	/* adding a reference must not cause a REFERENCE_ADDED callbacks on the
	virtual resources which are replaced by the reference */
	public void addingAReferenceCausesNoSpuriousReferenceAddedCall() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		sw.ratedValues().upperLimit().forecast().create();

		StructureTestListener l = new StructureTestListener();

		sw2.ratedValues().create();
		sw2.ratedValues().upperLimit().addStructureListener(l);

		sw2.ratedValues().upperLimit().setAsReference(sw.ratedValues().upperLimit());

		assertFalse("wrong callback received", l.awaitReferenceAdded(5, TimeUnit.SECONDS));
	}

	@Test
	public void listenersRemainAfterReferencesAreReplaced() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw3 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		sw2.settings().setpoint().create();
		sw3.settings().setpoint().create();
		sw.settings().setAsReference(sw2.settings());

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.settings().setpoint());
		l.setExpectedChangedResource(sw.settings().setpoint());
		Resource listenerTarget = sw.settings().setpoint();
		listenerTarget.addStructureListener(l);

		sw.settings().setAsReference(sw3.settings());
		assertTrue(sw.settings().equalsLocation(sw3.settings()));
		assertTrue(listenerTarget.equalsLocation(sw3.settings().setpoint()));

		assertFalse(sw.settings().setpoint().isActive());
		sw3.settings().activate(true);
		assertTrue(sw.settings().setpoint().isActive());

		assertTrue("no callback", l.awaitActivate(5, TimeUnit.SECONDS));
	}

	@Test
	public void listenersRemainAfterReferencesAreReplacedByDirectSubresource() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		sw2.settings().setpoint().create();
		sw.settings().setAsReference(sw2.settings());

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.settings().setpoint());
		l.setExpectedChangedResource(sw.settings().setpoint());
		sw.settings().setpoint().addStructureListener(l);

		sw.addOptionalElement("settings");
		sw.settings().setpoint().create();

		assertFalse(sw.settings().setpoint().isActive());
		sw.activate(true);
		assertTrue(sw.settings().setpoint().isActive());

		assertTrue("no callback", l.awaitActivate(5, TimeUnit.SECONDS));
	}

	@Test
	public void listenersRemainAfterResourcesAreDeleted() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		l.setExpectedSource(sw.settings().setpoint());
		l.setExpectedChangedResource(sw.settings().setpoint());
		sw.settings().setpoint().addStructureListener(l);

		sw.settings().setpoint().create();
		assertTrue("no callback", l.awaitCreate(5, TimeUnit.SECONDS));

		sw.settings().setpoint().delete(); //direct deletion
		l.reset();

		sw.settings().setpoint().create();
		assertTrue("no callback", l.awaitCreate(5, TimeUnit.SECONDS));

		sw.settings().delete(); //recursive deletion
		l.reset();

		sw.settings().setpoint().create();
		assertTrue("no callback", l.awaitCreate(5, TimeUnit.SECONDS));
	}

	@Test
	public void listenersAreNotNotifiedOnRemovedReferences() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		sw2.ratedValues().upperLimit().create();
		sw.ratedValues().upperLimit().addStructureListener(l);

		sw.ratedValues().setAsReference(sw2.ratedValues());
		assertTrue(sw.ratedValues().upperLimit().equalsLocation(sw2.ratedValues().upperLimit()));

		//remove the reference and check that no callbacks are received for sw2//*
		//sw.addOptionalElement(sw.ratedValues().getName());
        sw.ratedValues().delete();
        
        assertTrue(sw2.ratedValues().exists());
		sw.ratedValues().upperLimit().create();        
		assertFalse(sw.ratedValues().equalsLocation(sw2.ratedValues()));
		assertFalse(sw.ratedValues().upperLimit().equalsLocation(sw2.ratedValues().upperLimit()));

		assertFalse(l.eventReceived(RESOURCE_ACTIVATED));
		sw2.ratedValues().upperLimit().activate(true);

		assertFalse("received event from removed reference", l.awaitEvent(RESOURCE_ACTIVATED, 1, TimeUnit.SECONDS));

        l.reset();
        assertFalse(sw.ratedValues().upperLimit().isActive());
        sw.activate(true);
        assertTrue(sw.ratedValues().upperLimit().isActive());
        assertTrue("listener still working", l.awaitEvent(RESOURCE_ACTIVATED));
	}

	@Test
	public void settingAReferenceCausesNoSpuriousResourceCreatedCallbacks() throws InterruptedException {
		final PowerResource f = resMan.createResource(newResourceName(), PowerResource.class);
		final CountDownLatch createdCallbackReceived = new CountDownLatch(1);

		ResourceStructureListener l = new ResourceStructureListener() {

			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				if (event.getType() == RESOURCE_CREATED) {
					System.err.printf("bad RESOURCE_CREATED callback: %s%n", event.getSource());
					createdCallbackReceived.countDown();
				}
			}
		};
		f.addStructureListener(l);

		PowerSensor sens = resMan.createResource(newResourceName(), PowerSensor.class);

		sens.reading().setAsReference(f);

		assertFalse("received faulty RESOURCE_CREATED callback!", createdCallbackReceived.await(3, TimeUnit.SECONDS));
	}

	@Test
	public void deactivatingReferenceCausesCallbackOnReferencedResource() throws InterruptedException {
		final PowerResource f = resMan.createResource(newResourceName(), PowerResource.class);
		PowerSensor sens = resMan.createResource(newResourceName(), PowerSensor.class);
		sens.reading().setAsReference(f);

		f.activate(true);
		assertTrue(sens.reading().isActive());

		StructureTestListener l = new StructureTestListener();
		f.addStructureListener(l);

		sens.reading().deactivate(true);

		assertFalse(f.isActive());
		assertTrue(l.awaitEvent(RESOURCE_DEACTIVATED));
	}

	class EventTestListener implements ResourceStructureListener {

		public EventType lastType = null;
		public CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void resourceStructureChanged(ResourceStructureEvent event) {
			lastType = event.getType();
			latch.countDown();
		}

		public void reset() {
			latch = new CountDownLatch(1);
		}
	};

	@Test
	public void callbackEventTypeWorks() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		EventTestListener listener = new EventTestListener();
		sw.stateControl().addStructureListener(listener);

		// create 
		sw.stateControl().create();
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.RESOURCE_CREATED, listener.lastType);
		// activate
		listener.reset();
		sw.stateControl().activate(false);
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.RESOURCE_ACTIVATED, listener.lastType);
		// add reference
		listener.reset();
		sw2.stateControl().setAsReference(sw.stateControl());
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.REFERENCE_ADDED, listener.lastType);
		// add subresource
		listener.reset();
		Resource subres = sw.stateControl().getSubResource("testDecorator", OnOffSwitch.class).create();
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.SUBRESOURCE_ADDED, listener.lastType);
		// remove reference
		listener.reset();
		sw2.stateControl().delete();
		assertTrue("missing callback, expected " + EventType.REFERENCE_REMOVED, listener.latch.await(5, TimeUnit.SECONDS));
		assertEquals(EventType.REFERENCE_REMOVED, listener.lastType); 
		// deactivate
		listener.reset();
		sw.stateControl().deactivate(false);
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.RESOURCE_DEACTIVATED, listener.lastType);
		// remove subresource
		listener.reset();
		subres.delete();
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.SUBRESOURCE_REMOVED, listener.lastType);
		// delete
		listener.reset();
		sw.stateControl().delete();
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.RESOURCE_DELETED, listener.lastType);
		// recreate
		listener.reset();
		sw.stateControl().create();
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.RESOURCE_CREATED, listener.lastType);
		// clean up
		sw.stateControl().removeStructureListener(listener);
		sw2.delete();
		sw.delete(); 
	}

	@Ignore("Callback missing")
	@Test
	public void doubleReferencesWork() throws InterruptedException {
		ElectricHeater a = resMan.createResource("a", ElectricHeater.class);
		Room b = resMan.createResource("b", Room.class);
		TemperatureSensor c = resMan.createResource("c", TemperatureSensor.class);
		c.reading().create();
//		final CountDownLatch latch = new CountDownLatch(1);

		StructureTestListener listener = new StructureTestListener();

		a.location().room().temperatureSensor().reading().addStructureListener(listener);
		assertFalse(a.location().room().temperatureSensor().reading().exists());

		a.location().room().setAsReference(b);

		//a.location().room().temperatureSensor().reading().create();
		//a.location().room().temperatureSensor().reading().activate(false);

		assertTrue(b.equalsLocation(a.location().room()));
		b.temperatureSensor().setAsReference(c); // <-- broken!
		//b.temperatureSensor().reading().create(); // <-- broken!
		//a.location().room().temperatureSensor().setAsReference(c); // works

		assertTrue(a.location().room().temperatureSensor().reading().exists());
		//a.location().room().temperatureSensor().reading().activate(false);

		System.out.println("registered structure listeners:");
		for (RegisteredStructureListener rsl : getApplicationManager().getAdministrationManager().getAppById(
				getApplicationManager().getAppID().toString()).getStructureListeners()) {
			System.out.printf("%s: %s%n", rsl.getResource(), rsl.getListener());
		}

		//latch.await(5, TimeUnit.SECONDS);
		assertTrue("missing create callback", listener.awaitCreate(5, TimeUnit.SECONDS));
		//assertEquals("Missing structure changed callback; ", 0, latch.getCount());
		a.location().room().temperatureSensor().reading().removeStructureListener(listener);
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
		
		StructureTestListener listener = new StructureTestListener();
		thermo0
			.location().room().temperatureSensor()
			.location().room().temperatureSensor()
			.location().room().temperatureSensor()
			.location().room().temperatureSensor()
			.location().room().temperatureSensor().reading().addStructureListener(listener);
		thermo5.reading().create();
		thermo4.location().room().temperatureSensor().setAsReference(thermo5);
		thermo3.location().room().temperatureSensor().setAsReference(thermo4);
		thermo2.location().room().temperatureSensor().setAsReference(thermo3);
		thermo1.location().room().temperatureSensor().setAsReference(thermo2);
		thermo0.location().room().temperatureSensor().setAsReference(thermo1);
		Assert.assertTrue("Missing create callback",listener.awaitCreate(5, TimeUnit.SECONDS));
		thermo5.reading().activate(false);
		Assert.assertTrue("Missing activate callback", listener.awaitActivate(5, TimeUnit.SECONDS));
		thermo3.delete();
		Assert.assertTrue("Missing delete callback", listener.awaitEvent(RESOURCE_DELETED, 5, TimeUnit.SECONDS));
		thermo4.delete();
		thermo5.delete();
		thermo0.delete();
		thermo2.delete();
		thermo1.delete();
	}
	
	@Test
	public void structureCallbackForNewDeepReferenceWorks() throws InterruptedException {
		Thermostat thermo = resMan.createResource(newResourceName(), Thermostat.class);
		TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		StructureTestListener listener = new StructureTestListener();
		thermo.temperatureSensor().location().room().temperatureSensor().settings().setpoint().addStructureListener(listener);
		tempSens.location().room().temperatureSensor().settings().setpoint().create();
		thermo.temperatureSensor().setAsReference(tempSens);
		Assert.assertTrue("Missing create callback", listener.awaitCreate(5, TimeUnit.SECONDS));
		thermo.delete();
		tempSens.delete();
	}
	
	@Test
	public void addingSubresourceOnReferencedResourceCausesStructureCallback() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		StructureTestListener l = new StructureTestListener();
		sw.settings().setAsReference(sw2.settings().create());
		sw.settings().addStructureListener(l);  
		sw2.settings().alarmLimits().create();
		assertTrue("Referencing resource unexpectedly non-existent",sw.settings().alarmLimits().exists());
		assertTrue("Missing structure callback for newly created subresource",l.awaitEvent(SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS)); 
		sw2.delete();
		sw.delete();
	}
	
	/*
	 * like {@link #addingSubresourceOnReferencedResourceCausesStructureCallback()}, except that there are two references between the
	 * resource on which the listener is registered and the newly created subresource. 
	 */
	@Test
	public void addingSubresourceOnTransitivelyReferencedResourceCausesStructureCallback() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName() + "_sw", OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName() + "_sw2", OnOffSwitch.class);
		final OnOffSwitch sw3 = resMan.createResource(newResourceName() + "_sw3", OnOffSwitch.class);
		StructureTestListener l = new StructureTestListener();
		sw2.settings().setAsReference(sw3.settings().create());
		sw.settings().setAsReference(sw2.settings());
		sw.settings().addStructureListener(l); 
		sw3.settings().alarmLimits().create();
		assertTrue("Referencing resource unexpectedly non-existent",sw.settings().alarmLimits().exists());
		assertTrue("Missing structure callback for newly created subresource",l.awaitEvent(SUBRESOURCE_ADDED, 5, TimeUnit.SECONDS));
        ResourceAssertions.assertExists(sw3.settings().alarmLimits());
		sw2.delete();
        ResourceAssertions.assertDeleted(sw2);
//        ResourceAssertions.assertDeleted(sw2.settings());
//        ResourceAssertions.assertDeleted(sw2.settings().alarmLimits());

        ResourceAssertions.assertExists(sw3.settings().alarmLimits());
        ResourceAssertions.assertExists(sw3.settings());
        ResourceAssertions.assertExists(sw3);
		sw3.delete();
		sw.delete();
	}
	
	@Test
	public void doubleListenerRegistrationOnSameLocationWorks() throws InterruptedException {
		String suffix = newResourceName();
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor0.activate(false);
		sensor1.activate(false);
		sensor2.activate(false);
		
		StructureTestListener listener = new StructureTestListener();
		sensor0.addStructureListener(listener);
		sensor1.addStructureListener(listener);		
		
		sensor0.setAsReference(sensor1);
		waitForSingleReferenceCallbacks(listener);  // from sensor0, expect a pair of deleted and created callbacks
		waitForSingleEventCallback(listener, EventType.REFERENCE_ADDED); // from sensor1
		
		listener.reset(2);
		sensor1.setAsReference(sensor2); // now we should get two reference callbacks, one from sensor0, one from sensor1
		waitForReferenceCallbacks(listener, 2);
		
		listener.reset(2);
		sensor2.delete();
		waitForEventCallbacks(listener, EventType.RESOURCE_DELETED, 2);
		
		sensor1.removeStructureListener(listener);
		sensor0.removeStructureListener(listener);
		sensor1.delete();
		sensor0.delete();
	}

	@Test
	public void noSpuriousReferenceCallbacks() throws InterruptedException {
		String suffix = newResourceName();
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor0.activate(false);
		sensor1.activate(false);
		sensor2.activate(false);
		sensor0.setAsReference(sensor1);
		StructureTestListener listener = new StructureTestListener();
		sensor0.addStructureListener(listener);
		
		sensor2.setAsReference(sensor1);
		assertFalse("Unexpected callback",listener.awaitEvent(EventType.REFERENCE_ADDED, 1, TimeUnit.SECONDS)); // since the reference goes to sensor1, there should probably be no callback for sensor0
		
		sensor2.delete();
		assertFalse("Unexpected callback",listener.awaitEvent(EventType.REFERENCE_REMOVED, 1, TimeUnit.SECONDS)); // since the reference goes to sensor1, there should probably be no callback for sensor0
		
		sensor0.removeStructureListener(listener);
		sensor0.delete();
		sensor1.delete();
	}
	
	@Test
	public void noExcessiveCallbacksForReferencingResources() throws InterruptedException {
		// setup
		String suffix = newResourceName();
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor0.activate(false);
		sensor1.activate(false);
		sensor2.activate(false);
		StructureTestListener listener = new StructureTestListener();
		sensor0.addStructureListener(listener);
		sensor0.setAsReference(sensor1);
		waitForSingleReferenceCallbacks(listener); // check that only one set of reference callbacks occurs (delete, create)
		listener.reset();
		sensor1.setAsReference(sensor2);
		waitForSingleReferenceCallbacks(listener);
		
		listener.reset();
		sensor2.reading().create().activate(false);
		waitForSingleEventCallback(listener, EventType.SUBRESOURCE_ADDED); // check that only one Subresource_Added callback occurs
		
		listener.reset();
		sensor0.location().create().activate(false);
		waitForSingleEventCallback(listener, EventType.SUBRESOURCE_ADDED);
		
		listener.reset();
		sensor2.delete();
		waitForSingleEventCallback(listener, EventType.RESOURCE_DELETED);
		sensor1.delete();
		sensor0.delete();
	}
	
	@Test
	public void noExcessiveCallbacksForReferencingResources2() throws InterruptedException {
		// setup
		String suffix = newResourceName();
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor5 = resMan.createResource("fridge5_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor6 = resMan.createResource("fridge6_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor7 = resMan.createResource("fridge7_" + suffix, CoolingDevice.class).temperatureSensor().create();
		for (int i=0; i<8; i++) {
			CoolingDevice cd = resAcc.getResource("fridge" + i + "_" + suffix);
			cd.temperatureSensor().reading().create();
			cd.activate(true);
		}
		StructureTestListener listener = new StructureTestListener();
		sensor0.setAsReference(sensor1);
		sensor1.setAsReference(sensor2);
		sensor3.setAsReference(sensor4);
		sensor4.setAsReference(sensor5);
		sensor2.setAsReference(sensor3);
		sensor5.setAsReference(sensor6);
		// register listener
		sensor0.addStructureListener(listener);
		sensor6.setAsReference(sensor7);
		waitForSingleReferenceCallbacks(listener); // check that only one set of reference callbacks occurs (delete, create)
		
        //FIXME: missing subresource_added callback!
        /*
		listener.reset();
		sensor5.reading().create().activate(false);
		waitForSingleEventCallback(listener, EventType.SUBRESOURCE_ADDED);
        */
	
		listener.reset();
		sensor7.delete();
		waitForSingleEventCallback(listener, EventType.RESOURCE_DELETED);
		
		for (int i=0; i<7; i++) {
			resAcc.getResource("fridge" + i + "_" + suffix).delete();
		}
	}
	
	@Test
	public void referenceDeletionCausesDeleteCallbackOnSubresource() throws InterruptedException {
		String suffix = newResourceName();
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor1.reading().create();
		
		sensor0.setAsReference(sensor1);
		StructureTestListener listener = new StructureTestListener();
		sensor0.reading().addStructureListener(listener);
		
		sensor0.delete(); // here we also delete the subresource reading(), hence expect a listener callback
		waitForSingleEventCallback(listener, RESOURCE_DELETED);
		
		sensor0.reading().create(); // double check that the listener is still there
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
		sensor0.reading().removeStructureListener(listener);
		resAcc.getResource("fridge0_" + suffix).delete();
		resAcc.getResource("fridge1_" + suffix).delete();
	}
	
	/*
	 * Similar to referenceDeletionCausesDeleteCallbackOnSubresource above, except that there are transitive references
	 */
	@Test
	public void transitiveReferenceDeletionCausesDeleteCallbackOnSubresource() throws InterruptedException {
		String suffix = newResourceName();
		CoolingDevice fridge0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class);
		CoolingDevice fridge1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class);
		CoolingDevice fridge2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class);
		CoolingDevice fridge3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class);
		fridge3.location().room().temperatureSensor().reading().create();
		fridge2.location().room().temperatureSensor().setAsReference(fridge3.location().room().temperatureSensor());
		fridge1.location().setAsReference(fridge2.location());
		
		StructureTestListener listener = new StructureTestListener();
		fridge0.location().room().temperatureSensor().reading().addStructureListener(listener); // add listener to virtual resource
		
		fridge0.location().setAsReference(fridge1.location());
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
        Thread.sleep(2000);
        listener.reset();
		fridge3.location().room().temperatureSensor().delete(); // this also deletes fridge0.location().room().temperatureSensor().reading()
		waitForSingleEventCallback(listener, RESOURCE_DELETED); //error
        //waitForEventCallbacks(listener, RESOURCE_DELETED, 2); //works
		
		// check once more that the listener is still there
		listener.reset();
		fridge0.location().room().temperatureSensor().reading().create();
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
		fridge0.location().room().temperatureSensor().reading().removeStructureListener(listener);
		for (int i=0; i<4; i++) {
			resAcc.getResource("fridge" + i + "_" + suffix).delete();
		}
		
	}
	
	@Test 
	public void structureListenerOnVirtualResourceSurvivesParentReferenceCreation() throws InterruptedException {
		String suffix = newResourceName();
		// sensor0 is virtual
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor1.reading().create();
		sensor1.activate(true);
		
		StructureTestListener listener = new StructureTestListener();
		sensor0.reading().addStructureListener(listener);
		sensor0.setAsReference(sensor1); // this also creates the reading subresource
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
		// now we delete the reference and set it again on another resource -> check that the listener is still there
		sensor0.delete();
		waitForSingleEventCallback(listener, RESOURCE_DELETED);
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor2.reading().create();
		sensor2.activate(true);
		
		listener.reset();
		sensor0.setAsReference(sensor2);
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
		sensor0.reading().removeStructureListener(listener);
		for (int i=0; i<3; i++) {
			resAcc.getResource("fridge" + i + "_" + suffix).delete();
		}
	}
	
	@Test 
	public void structureListenerOnVirtualResourceSurvivesHigherLevelParentReferenceCreation() throws InterruptedException {
		String suffix = newResourceName();
		// sensor0 is virtual
		TemperatureSensor sensor0 = resMan.createResource("fridge0_" + suffix, CoolingDevice.class).temperatureSensor();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor1.location().room().humiditySensor().create();
		sensor1.activate(true);
		
		StructureTestListener listener = new StructureTestListener();
		sensor0.location().room().humiditySensor().addStructureListener(listener);
		sensor0.setAsReference(sensor1); // this also creates the reading subresource
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
		// now we delete the reference and set it again on another resource -> check that the listener is still there
		sensor0.delete();
		waitForSingleEventCallback(listener, RESOURCE_DELETED);
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		sensor2.location().room().humiditySensor().create();
		sensor2.activate(true);
		
		listener.reset();
		sensor0.setAsReference(sensor2);
		waitForSingleEventCallback(listener, RESOURCE_CREATED);
		
		sensor0.location().room().humiditySensor().removeStructureListener(listener);		
		for (int i=0; i<3; i++) {
			resAcc.getResource("fridge" + i + "_" + suffix).delete();
		}
	}
	
	@Test
	public void activationTriggersCallbackOnReferenceTarget() throws InterruptedException {
		StructureTestListener listener = new StructureTestListener();
		TemperatureSensor sensor0 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor sensor1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		sensor0.location().room().temperatureSensor().reading().create();
		sensor0.location().room().temperatureSensor().reading().addStructureListener(listener);
		sensor1.location().setAsReference(sensor0.location());
		sensor1.location().room().temperatureSensor().reading().activate(false);
		Assert.assertTrue("Missing activation callback for reference target",listener.awaitActivate(5, TimeUnit.SECONDS));
		sensor0.location().room().temperatureSensor().reading().removeStructureListener(listener);
		sensor0.delete();
		sensor1.delete();
	}
	
	// same as above, but with a more complex reference chain
	@Test
	public void activationTriggersCallbackOnTransitiveReferenceTarget() throws InterruptedException {
		StructureTestListener listener = new StructureTestListener();
		TemperatureSensor sensor0 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor sensor1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor sensor2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor sensor3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		sensor3.location().room().temperatureSensor().location().room().temperatureSensor().reading().create();
		sensor3.location().room().temperatureSensor().location().room().temperatureSensor().reading().addStructureListener(listener);
		sensor2.location().room().temperatureSensor().location().setAsReference(sensor3.location().room().temperatureSensor().location());
		sensor1.location().room().create();
		sensor0.location().room().setAsReference(sensor1.location().room());
		sensor1.location().room().setAsReference(sensor2.location().room());
		sensor0.location().room().temperatureSensor().location().room().temperatureSensor().reading().activate(false);
		
		Assert.assertTrue("Missing activation callback for reference target",listener.awaitActivate(5, TimeUnit.SECONDS));
		sensor3.location().room().temperatureSensor().location().room().temperatureSensor().reading().removeStructureListener(listener);
		sensor0.delete();
		sensor1.delete();
		sensor2.delete();
		sensor3.delete();
	}
	
	private static void waitForSingleReferenceCallbacks(StructureTestListener listener) throws InterruptedException {
		waitForReferenceCallbacks(listener, 1);
	}
	
	private static void waitForReferenceCallbacks(StructureTestListener listener, int nrCallbacksExpected) throws InterruptedException {
		assertTrue("Event missing of type " + EventType.RESOURCE_DELETED, listener.awaitEvent(EventType.RESOURCE_DELETED));
		assertTrue("Event missing of type " + EventType.RESOURCE_CREATED, listener.awaitEvent(EventType.RESOURCE_CREATED));
		Thread.sleep(500);
		assertEquals("Unexpected number of callbacks: ",nrCallbacksExpected,listener.getEventCount(EventType.RESOURCE_DELETED));
		assertEquals("Unexpected number of callbacks: ",nrCallbacksExpected,listener.getEventCount(EventType.RESOURCE_CREATED));
	}
	
	private static void waitForSingleEventCallback(StructureTestListener listener, EventType type) throws InterruptedException {
		waitForEventCallbacks(listener, type, 1);
	}
	
	private static void waitForEventCallbacks(StructureTestListener listener, EventType type, int nrCallbacksExpected) throws InterruptedException {
		assertTrue("Event missing of type " + type, listener.awaitEvent(type));
		Thread.sleep(500);
		assertEquals("Unexpected number of callbacks: ",nrCallbacksExpected,listener.getEventCount(type));
	}
	
	

}
