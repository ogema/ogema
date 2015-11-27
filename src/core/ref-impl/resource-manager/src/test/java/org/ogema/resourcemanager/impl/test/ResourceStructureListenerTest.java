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
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.generators.ElectricHeater;
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
	@Ignore("FIXME")
	//FIXME
	public void listenersAreNotNotifiedOnRemovedReferences() throws InterruptedException {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		StructureTestListener l = new StructureTestListener();
		sw2.ratedValues().upperLimit().create();
		sw.ratedValues().upperLimit().addStructureListener(l);

		sw.ratedValues().setAsReference(sw2.ratedValues());
		assertTrue(sw.ratedValues().upperLimit().equalsLocation(sw2.ratedValues().upperLimit()));

		//remove the reference and check that no callbacks are received for sw2//*
		sw.addOptionalElement(sw.ratedValues().getName());
		sw.ratedValues().upperLimit().create();
		System.out.println(sw.ratedValues().upperLimit().getLocation());
		System.out.println(sw.ratedValues().getPath());
		System.out.println(sw.ratedValues().getLocation());
		System.out.println(sw.ratedValues().upperLimit().getPath());
		System.out.println(sw.ratedValues().upperLimit().getLocation());
		assertFalse(sw.ratedValues().equalsLocation(sw2.ratedValues()));
		assertFalse(sw.ratedValues().upperLimit().equalsLocation(sw2.ratedValues().upperLimit()));

		assertFalse(l.eventReceived(RESOURCE_ACTIVATED));
		sw2.ratedValues().upperLimit().activate(true);

		assertFalse("received event from wrong resource", l.awaitEvent(RESOURCE_ACTIVATED));
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
		listener.latch.await(5, TimeUnit.SECONDS);
		assertEquals(EventType.REFERENCE_REMOVED, listener.lastType); // -> RESOURCE_DELETED instead
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
		sw.delete(); // -> null pointer exception
	}

	@Test
	public void doubleReferencesWork() throws InterruptedException {
		ElectricHeater a = resMan.createResource("a", ElectricHeater.class);
		Room b = resMan.createResource("b", Room.class);
		TemperatureSensor c = resMan.createResource("c", TemperatureSensor.class);
		c.reading().create();
		final CountDownLatch latch = new CountDownLatch(1);

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

}
