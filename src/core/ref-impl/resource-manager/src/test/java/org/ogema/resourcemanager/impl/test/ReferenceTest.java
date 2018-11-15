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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceGraphException;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.exam.ResourceAssertions;

import static org.ogema.exam.ResourceAssertions.assertActive;
import static org.ogema.exam.ResourceAssertions.assertDeleted;
import static org.ogema.exam.ResourceAssertions.assertExists;
import org.ogema.exam.StructureTestListener;

import org.ogema.model.locations.Room;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.buildingtechnology.ElectricLight;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.locations.PhysicalDimensions;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.time.CalendarEntry;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ReferenceTest extends OsgiTestBase {

	public static final String RESNAME = "ResourceTestResource";

	@Before
	@Override
	public void doBefore() {
        OgemaLogger l = (OgemaLogger) LoggerFactory.getLogger("org.ogema.resourcemanager.virtual.DefaultVirtualResourceDB");
        l.setMaximumLogLevel(LogOutput.CONSOLE, LogLevel.TRACE);
	}

	/*
	 * test 'transitive' references i.e. A references (B references C) looks like A=C
	 */
	@Test
	public void transitiveReferencesWork() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		// sw1.addOptionalElement("ratedSwitchingCurrent");
		// FloatResource rsc1 = sw1.ratedSwitchingCurrent();
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		// sw2.addOptionalElement("ratedSwitchingCurrent");
		// FloatResource rsc2 = sw2.ratedSwitchingCurrent();
		OnOffSwitch sw3 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw3.heatCapacity().create();

		sw3.heatCapacity().setValue(3.f);

		sw2.heatCapacity().setAsReference(sw3.heatCapacity());
		sw1.heatCapacity().setAsReference(sw2.heatCapacity());

		assertEquals(3.f, sw1.heatCapacity().getValue(), 0.f);
	}

	/*
	 * test 'transitive' references i.e. A references (B references C) looks like A=C
	 */
	@Test
	public void transitiveReferencesWork2() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.settings().setpoint().create();
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw2.settings().setpoint().create();
		OnOffSwitch sw3 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.settings().setAsReference(sw2.settings());
		assertEquals(sw1.settings().setpoint().getLocation(), sw2.settings().setpoint().getLocation());
		sw3.settings().setpoint().create();
		sw2.settings().setAsReference(sw3.settings());
		System.out.println("  A " + sw1.settings().setpoint().getLocation() + ", " + sw1.settings().getLocation());
		System.out.println("  B " + sw2.settings().setpoint().getLocation() + ", " + sw2.settings().getLocation());
		System.out.println("  C " + sw3.settings().setpoint().getLocation() + ", " + sw3.settings().getLocation());
		assert (sw1.settings().setpoint().equalsLocation(sw3.settings().setpoint())) : "Rereference forwarding failed";
		assert (sw1.settings().equalsLocation(sw3.settings())) : "Rereference forwarding failed";
		sw1.delete();
		sw2.delete();
		sw3.delete();
	}
    
    @Test
	public void transitiveReferencesWorkWithCreate() {
		getApplicationManager().getLogger().setMaximumLogLevel(LogOutput.CONSOLE, LogLevel.TRACE);
        ElectricHeater a = resMan.createResource(newResourceName(), ElectricHeater.class);
        Room b = resMan.createResource(newResourceName(), Room.class);
        TemperatureSensor c = resMan.createResource(newResourceName(), TemperatureSensor.class);
        a.activate(true);
        b.activate(true);
        c.activate(true);
        a.location().room().setAsReference(b);
        b.temperatureSensor().setAsReference(c);
        c.reading().create();
        assertExists(c.reading());
        assertExists(b.temperatureSensor().reading());
        assertExists(a.location().room().temperatureSensor().reading());
        c.reading().activate(false);
        assertActive(c.reading());
        assertActive(b.temperatureSensor().reading());
        assertActive(a.location().room().temperatureSensor().reading());
        assertExists(a.location().room().temperatureSensor().reading());
        assertActive(a.location().room().temperatureSensor().reading());
	}

	@Test
	public void referenceForwardingWorks() {
		OnOffSwitch sw0 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw0.stateFeedback().create();
		sw0.stateFeedback().setValue(false);
		sw1.stateFeedback().setAsReference(sw0.stateFeedback());
		sw2.stateFeedback().create();
		sw0.stateFeedback().setAsReference(sw2.stateFeedback());
		sw2.stateFeedback().setValue(true);
		System.out.printf("  sw0 (%s) stateFeedback = %s%n", sw0.getName(), sw0.stateFeedback().getLocation());
		System.out.printf("  sw1 (%s) stateFeedback = %s%n", sw1.getName(), sw1.stateFeedback().getLocation());
		System.out.printf("  sw2 (%s) stateFeedback = %s%n", sw2.getName(), sw2.stateFeedback().getLocation());
        
		assertTrue(sw0.stateFeedback().equalsLocation(sw1.stateFeedback()));
		assertTrue(sw0.stateFeedback().isReference(false));
		assertTrue(sw1.stateFeedback().isReference(false));

		assertEquals("Rereference forwarding failed",
                sw1.stateFeedback().getValue(), sw2.stateFeedback().getValue());
		assertEquals("Rereference forwarding failed",
                sw1.stateFeedback().getLocation(), sw2.stateFeedback().getLocation());

		sw0.delete();
		sw1.delete();
		sw2.delete();
	}

	@Test
	public void equalsResourceWorks() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.heatCapacity().create();
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw2.heatCapacity().setAsReference(sw1.heatCapacity());

		assertNotSame(sw1.heatCapacity(), sw2.heatCapacity());
		assertTrue(sw1.heatCapacity().equalsLocation(sw2.heatCapacity()));
	}

	// isReference(false)
	@Test
	public void isReferenceWorks() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.addOptionalElement("stateFeedback");
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw2.setOptionalElement("stateFeedback", sw1.stateFeedback());
		assertFalse(sw1.stateFeedback().isReference(false));
		assertTrue(sw2.stateFeedback().isReference(false));
	}

	// isReference(true)
	@Test
	public void isReferencePathWorks() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.addOptionalElement("stateFeedback");
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw2.setOptionalElement("stateFeedback", sw1.stateFeedback());
		assertFalse(sw1.stateFeedback().isReference(true));
		assertTrue(sw2.stateFeedback().isReference(true));
	}

	@Test
	public void loopsCanBeCreated() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.addOptionalElement("stateFeedback");
		sw1.stateFeedback().addDecorator("fnord", sw1);
		assertEquals(2, sw1.getSubResources(true).size());
	}

	@Test
	public void referencesCanBeReplaced() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.heatCapacity().create();
		sw1.heatCapacity().setValue(1);
		sw1.heatCapacity().addDecorator("test", FloatResource.class);

		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw2.heatCapacity().create();
		sw2.heatCapacity().setValue(2);

		sw2.addDecorator("foo", sw1.heatCapacity());
		assertEquals(1, ((FloatResource) (sw2.getSubResource("foo"))).getValue(), 0f);

		FloatResource foo = (FloatResource) sw2.getSubResource("foo");
		assertEquals(1, foo.getValue(), 0f);

		sw2.addDecorator("foo", sw2.heatCapacity());
		assertEquals(2, ((FloatResource) (sw2.getSubResource("foo"))).getValue(), 0f);

		assertTrue(sw1.heatCapacity().exists());
		assertTrue(sw1.heatCapacity().getSubResource("test").exists());

		assertEquals(2, foo.getValue(), 0f);
	}

	@Test
	public void referencesToTopLevelCanBeSafelyReplaced() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		ElectricHeater heater = resMan.createResource(newResourceName(), ElectricHeater.class);

		sw1.stateControl().create();
		sw2.stateControl().create();

		heater.onOffSwitch().setAsReference(sw1);
		assertTrue(heater.onOffSwitch().stateControl().exists());

		heater.onOffSwitch().setAsReference(sw2);
		assertTrue(heater.onOffSwitch().stateControl().exists());
		assertTrue(heater.onOffSwitch().stateControl().equalsLocation(sw2.stateControl()));

		assertTrue("sw1 is still complete", sw1.stateControl().exists());
	}

	@Test
	public void referencesToTopLevelCanBeSafelyDeleted() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		ElectricHeater heater = resMan.createResource(newResourceName(), ElectricHeater.class);
		sw1.stateControl().create();

		heater.onOffSwitch().setAsReference(sw1);

		assertTrue(heater.onOffSwitch().stateControl().equalsLocation(sw1.stateControl()));
		heater.onOffSwitch().delete();
		assertTrue("sw1 is still complete", sw1.stateControl().exists());
	}

	/* when an application holds a resource R which is a reference and this
	   reference is replaced with a new reference, R should still be valid and point
	   to the new reference (the path remains the same) */
	@Test
	public void replacedReferencesStillValid() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		OnOffSwitch sw3 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		sw1.addOptionalElement("physDim");
		sw2.addOptionalElement("physDim");
		sw3.setOptionalElement("physDim", sw1.physDim());
		PhysicalDimensions pdim = sw3.physDim();
		assertTrue(pdim.isReference(true));
		assertTrue(pdim.getLocation("/").startsWith(sw1.getName()));
		sw3.setOptionalElement("physDim", sw2.physDim());
		assertTrue(pdim.isReference(true));
		assertTrue(pdim.getLocation("/").startsWith(sw2.getName()));
	}

	/**
	 * Checks that subresources of a resource with a decorator-reference in its path can be accessed.
	 */
	@Test
	public void refencedDecoratorsWork() {
		final String DECORATORNAME = "decorator";

		// set up the resources.
		final OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		final FloatResource value = (FloatResource) sw1.heatCapacity().create();
		value.setValue(1);
		sw1.activate(true);
		final OnOffSwitch superSwitch = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		superSwitch.addDecorator(DECORATORNAME, sw1);

		// get sw1 via the path with decorator-reference.
		final OnOffSwitch referencedSwitch = (OnOffSwitch) superSwitch.getSubResource(DECORATORNAME);
		assertNotNull(referencedSwitch);

		final List<Resource> subresources = referencedSwitch.getSubResources(false);
		assertEquals(subresources.size(), 1);

		final Resource listEntry = subresources.get(0);
		assertNotNull(listEntry);
		final FloatResource refValue = (FloatResource) referencedSwitch.heatCapacity();
		assertNotNull(refValue);

		assert (refValue.equalsLocation(listEntry));
		assert (refValue.equalsLocation(value));
		assert (!refValue.equalsPath(value));

		value.setValue(3.f);
		assertEquals(refValue.getValue(), 3.f, 1.e-6);
		refValue.setValue(4.f);
		assertEquals(value.getValue(), 4.f, 1.e-6);

		CalendarEntry meeting1 = resMan.createResource("meeting", CalendarEntry.class);
		StringResource subject1 = (StringResource) meeting1.addOptionalElement("subject");
		CalendarEntry meeting2 = resMan.createResource("meeting2", CalendarEntry.class);
		StringResource subject2 = (StringResource) meeting2.addOptionalElement("subject");
		subject1.setValue("Subject1");
		subject2.setValue("OtherSubject");

		meeting1.activate(true);
		meeting2.activate(true);

		superSwitch.addDecorator("meeting", meeting1);
		Resource subResource = superSwitch.getSubResource("meeting");
		assertTrue(subResource.equalsLocation(meeting1));
		assertFalse(subResource.equalsLocation(meeting2));

		StringResource firstSubject = (StringResource) subResource.getSubResource("subject");
		assertEquals(subject1.getValue(), firstSubject.getValue());
		superSwitch.addDecorator("meeting", meeting2);
		subResource = superSwitch.getSubResource("meeting");
		StringResource secondSubject = (StringResource) subResource.getSubResource("subject");
		assertFalse(subResource.equalsLocation(meeting1));
		assertTrue(subResource.equalsLocation(meeting2));

		assertEquals(subject2.getValue(), secondSubject.getValue());

	}

	@Test
	public void getReferencingElementsWorks() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw3 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		sw1.stateControl().create();
		BooleanResource ctrl1 = sw1.stateControl();

		assertTrue(ctrl1.getReferencingResources(Resource.class).isEmpty());

		//add a reference (optional element)
		sw2.stateControl().setAsReference(ctrl1);
		assertEquals(1, ctrl1.getReferencingResources(null).size());
		assertEquals(sw2, ctrl1.getReferencingResources(null).get(0));

		//add another reference (optional element)
		sw3.stateControl().setAsReference(ctrl1);
		assertEquals(2, ctrl1.getReferencingResources(null).size());
		assertTrue(ctrl1.getReferencingResources(null).contains(sw3));
		assertTrue(ctrl1.getReferencingResources(null).contains(sw2));

		//check method calls on a reference
		assertEquals(0, sw2.stateControl().getReferencingResources(null).size());

		//filter by refering type
		assertEquals(2, ctrl1.getReferencingResources(null).size());
		assertEquals(2, ctrl1.getReferencingResources(OnOffSwitch.class).size());
		assertEquals(0, ctrl1.getReferencingResources(Room.class).size());

		//replace existing reference (optional element)
		//  sw3.stateControl().exists(), so we cannot create the optional element
		//  by calling sw3.stateControl().create()
		sw3.addOptionalElement("stateControl");
		//sw3.stateControl().create();
		assertEquals(1, ctrl1.getReferencingResources(null).size());
		assertFalse(ctrl1.getReferencingResources(null).contains(sw3));

		//add reference (as decorator)
		sw3.addDecorator("foo", ctrl1);
		assertEquals(2, ctrl1.getReferencingResources(null).size());
		assertTrue(ctrl1.getReferencingResources(null).contains(sw3));

		//replace existing reference (decorator)
		BooleanResource dec = sw2.addDecorator("bar", BooleanResource.class);
		sw3.addDecorator("foo", dec);
		assertTrue(dec.equalsLocation(sw3.getSubResource("foo")));
		assertEquals(1, ctrl1.getReferencingResources(null).size());
		assertFalse(ctrl1.getReferencingResources(null).contains(sw3));

		//top level resources used as references have correct referencing resources
		sw3.addDecorator("wibble", sw1);
		assertEquals(1, sw1.getReferencingResources(null).size());
		assertTrue(sw1.getReferencingResources(null).contains(sw3));

	}

	@Test
	public void referencingOwnOptionalElementWorks() {
		final OnOffSwitch swtch = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		final Resource stateControl = swtch.stateControl().create();
		assertNotNull(stateControl);
		final Resource stateFeedback = swtch.stateFeedback().setAsReference(stateControl);
		assertNotNull(stateFeedback);
		assertFalse(stateControl.equalsPath(stateFeedback));
		assertTrue(stateControl.equalsLocation(stateFeedback));
	}

	/** @see #replaceReferenceByItself()  */
	@Test(expected = ResourceGraphException.class)
	public void replacingResourceWithAReferenceToItselfCausesException() {
		final OnOffSwitch swtch = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		final OnOffSwitch swtch2 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		final Resource stateControl = swtch.stateControl().create();
		assertNotNull(stateControl);
		swtch2.stateControl().setAsReference(stateControl);
		@SuppressWarnings("unused")
		final Resource stateControl2 = swtch.stateControl().setAsReference(swtch2.stateControl());
	}
	
	@Test(expected=ResourceGraphException.class)
	public void complexReferenceLoopCausesException() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		
		sensor2.setAsReference(sensor3);
		sensor1.setAsReference(sensor2);
		sensor4.setAsReference(sensor1);
		sensor3.setAsReference(sensor4); // here the loop closes: 1 -> 2 -> 3 -> 4 -> 1
	}

	@Test
	public void switchingReferenceToSameLocationTargetWorks1() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		
		sensor1.setAsReference(sensor2);
		sensor3.setAsReference(sensor2);
		sensor1.setAsReference(sensor3); // switch reference to another path but same location
		
		sensor2.delete();
		assertFalse(sensor1.exists());
		assertFalse(sensor2.exists());
		assertFalse(sensor3.exists());
		for (int i=1;i<4;i++) {
			resAcc.getResource("fridge" + i + "_" + suffix).delete();
		}
	}
	
	@Test
	public void switchingReferenceToSameLocationTargetWorks2() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		
		sensor2.setAsReference(sensor4);
		sensor3.setAsReference(sensor4);
		sensor1.setAsReference(sensor2);
		sensor1.setAsReference(sensor3); // switch reference to another path but same location
		
		sensor2.delete();
		assertTrue(sensor1.exists());
		assertLocationsEqual(sensor4, sensor1);
		
		sensor4.delete();
		ResourceAssertions.assertDeleted(sensor1);
        ResourceAssertions.assertDeleted(sensor2);
        ResourceAssertions.assertDeleted(sensor3);
        ResourceAssertions.assertDeleted(sensor4);
		for (int i=1;i<5;i++) {
			resAcc.getResource("fridge" + i + "_" + suffix).delete();
		}
	}
	
	/** @see #replacingResourceWithAReferenceToItselfCausesException() */
	@Test
	public void replaceReferenceByItself() {
		OnOffSwitch sw1 = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		BooleanResource reference = resMan.createResource(RESNAME + counter++, BooleanResource.class);
		sw1.stateControl().setAsReference(reference);
		sw1.stateControl().setAsReference(reference);
	}
    
    @Test
    public void replacingReferencesIsOnlyConstrainedByDeclaredType() {
        Sensor s = resMan.createResource(newResourceName(), Sensor.class);
        ValueResource vr = s.reading();
        ResourceAssertions.assertIsVirtual(vr);
        assertEquals(vr.getResourceType(), ValueResource.class);
        FloatResource f = resMan.createResource(newResourceName(), FloatResource.class);
        IntegerResource i = resMan.createResource(newResourceName(), IntegerResource.class);
        FloatResource fAsRef = s.reading().setAsReference(f);
        ResourceAssertions.assertExists(s.reading());
        assertEquals(vr.getResourceType(), FloatResource.class);
        assertEquals(s.reading().getResourceType(), FloatResource.class);
        fAsRef.setValue(47.11f);
        assertEquals(47.11f, fAsRef.getValue(), 0.1f);
        s.reading().setAsReference(i);
        //this might change the type of existing Resource objects
        assertEquals(vr.getResourceType(), IntegerResource.class);
        assertEquals(s.reading().getResourceType(), IntegerResource.class);
        //the FloatResource now has resourceType IntegerResource
        assertEquals(fAsRef.getResourceType(), IntegerResource.class);
        //fAsRef.setValue(47.11f); //fails: UnsupportedOperationException
        ((IntegerResource)s.reading()).setValue(42);
        assertEquals(42, ((IntegerResource)s.reading()).getValue());
    }

	@Test
	public void deletingReferencesWorks() {
		Room room = resMan.createResource(RESNAME + counter++, Room.class);
		room.create().activate(false);
		StringResource name = resMan.createResource(RESNAME + counter++, StringResource.class);
		name.create().activate(false);
		room.name().setAsReference(name);
		assertTrue(room.name().exists());
		assertTrue(room.name().isActive());
		room.name().delete();
		assertTrue(name.exists());
		assertTrue(name.isActive());
		assertFalse(room.name().exists());
		assertFalse(room.name().isActive());
		room.delete();
		name.delete();
	}

	@Test
	public void multipleReferencesFromSameParentCanBeDeleted() {
		Room room = resMan.createResource(newResourceName(), Room.class);
		room.create().activate(false);
		StringResource name = resMan.createResource(newResourceName(), StringResource.class);
		name.create().activate(false);

		room.addDecorator("x1", name);
		room.addDecorator("x2", name);
        
		ResourceAssertions.assertExists(room.getSubResource("x1", StringResource.class));
		ResourceAssertions.assertExists(room.getSubResource("x2", StringResource.class));

		name.delete();

		ResourceAssertions.assertDeleted(room.getSubResource("x1", StringResource.class));
		ResourceAssertions.assertDeleted(room.getSubResource("x2", StringResource.class));

	}

	@Test
	public void resourceCanBeReplacedByReferenceOfDerivedType() {
		MultiSwitch swtch = resMan.createResource("testDeviceTop", MultiSwitch.class);
		swtch.addDecorator("testdevice", PhysicalElement.class);
		SingleSwitchBox ssb = resMan.createResource("testDeviceTop2", SingleSwitchBox.class);
		swtch.addDecorator("testdevice", ssb); // replaces resource "testDevice"
		// now we perform the same test again, this time with an optional element instead of a decorator
		swtch.settings().setpoint().create();
		TemperatureResource tr = resMan.createResource("testTempRes", TemperatureResource.class);
		swtch.settings().setpoint().setAsReference(tr);

		swtch.delete();
		tr.delete();
		ssb.delete();
	}

	@Test
	public void deletingReferencesWorksInCaseOfMultipleReferences() {
		Room room = resMan.createResource(RESNAME + counter++, Room.class);
		@SuppressWarnings("unchecked")
		ResourceList<PhysicalElement> list = room.addDecorator("list", ResourceList.class);
		list.setElementType(PhysicalElement.class);
		ElectricLight light = resMan.createResource(RESNAME + counter++, ElectricLight.class);
		ElectricLight sublight = list.addDecorator("light1", light);
		assertTrue(sublight.exists());
		// add another random reference to the target resource
		Room room2 = resMan.createResource(RESNAME + counter++, Room.class);
		ElectricLight auxReference = room2.addDecorator("randomDeco", light);
		sublight.delete();
		assertFalse(sublight.exists());
		assertTrue(auxReference.exists());
		assertTrue(auxReference.equalsLocation(light));
		room.delete();
		light.delete();
		room2.delete();
	}

    @Test
	public void resourceListEntryCanBeRereferenced() {
		@SuppressWarnings("unchecked")
		ResourceList<PhysicalElement> list = resMan.createResource(newResourceName(), ResourceList.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		list.setElementType(PhysicalElement.class);
		PhysicalElement sub1 = list.add(light);
		//XXX this is the specified behaviour for references - should lists be different?
		sub1.setAsReference(fridge); // fails
		list.delete();
		light.delete();
		fridge.delete();
	}

	@Test
	public void recreateDeletedReferenceWorks() {
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		PhysicalElement el = switchBox.device();
        assert (el.getResourceType().equals(PhysicalElement.class));
		el.setAsReference(fridge);
        assert (el.getResourceType().equals(CoolingDevice.class));
		//el.delete();
		//switchBox.device().delete();
        fridge.delete();
        assertDeleted(el);
		el.create();
		assertEquals(PhysicalElement.class, el.getResourceType());
		switchBox.delete();
		fridge.delete();
	}
    
	@Test
	public void resetOptionalElementReferenceToNewSubtypeWorks() {
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		switchBox.device().setAsReference(light);
		switchBox.device().setAsReference(fridge);
		// same test again, but now resource created with addDecorator
		switchBox.device().delete();
		PhysicalElement el = switchBox.addDecorator("device", PhysicalElement.class); // fails
		el.setAsReference(fridge);
		switchBox.delete();
		light.delete();
		fridge.delete();
	}

	@Ignore
	@Test(expected = ResourceGraphException.class)
	public void nonExistentOptionalElementCausesGraphException() {
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		ElectricLight lightReference = switchBox.device().setAsReference(light);
		lightReference.delete();
		assertFalse(lightReference.luminousFluxSensor().exists());
		switchBox.device().setAsReference(fridge);
		// now the lightReference is no longer an ElectricLight resource, but a CoolingDevice, hence 
		// the "luminousFluxSensor" subresource should no longer be present
		lightReference.luminousFluxSensor().exists(); // exception expected, get instead a virtual resource
	}

	/*
	 * This is the same test as above, except that we do not explicitly delete the lightReference
	 * resource (which should not make a difference, since it is rereferenced immediately afterwards
	 * anyway). However, instead of a virtual resource, the lightReference.luminousFluxSensor() method 
	 * in the last line returns null here.
	 */
	@Ignore
	@Test(expected = ResourceGraphException.class)
	public void nonExistentOptionalElementCausesGraphException2() {
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		ElectricLight lightReference = switchBox.device().setAsReference(light);
		switchBox.device().setAsReference(fridge);
		// now the lightReference is no longer an ElectricLight resource, but a CoolingDevice, hence 
		// the "luminousFluxSensor" subresource should no longer be present
		lightReference.luminousFluxSensor().exists(); // -> NullPointerException
	}
	
	// caused ResourceNotFoundException
	@Test
	public void multipleReferencesWork() {
		int nr = 10;  // failed iff nr > 3
		String suffix = newResourceName();
		CoolingDevice[] devices = new CoolingDevice[nr];
		for (int i=0;i<nr;i++) {
			devices[i] = resMan.createResource("fridge" + i + "_" + suffix, CoolingDevice.class);
			devices[i].temperatureSensor().reading().create();
		}
		for (int i=0 ;i<nr-1;i++) {
            System.out.println("i="+i);
			devices[i].temperatureSensor().setAsReference(devices[i+1].temperatureSensor());
            assertEquals("wrong number of references: " + devices[i+1].temperatureSensor().getReferencingResources(null),
                    i+1, devices[i+1].temperatureSensor().getReferencingResources(null).size());
		}
	}

	// similar to #multipleReferencesWork, except that referenced resources do not directly reference other resources again,
	// but only one of their subresources. 
	private void multipleReferencesWorkTest(int nrTopResources) {
		String suffix = newResourceName();
		CoolingDevice[] devices = new CoolingDevice[nrTopResources];
		for (int i=0;i<nrTopResources;i++) {
			devices[i] = resMan.createResource("fridge" + i + "_" + suffix, CoolingDevice.class);
			CoolingDevice cd = devices[i].getSubResource("test1", CoolingDevice.class).getSubResource("test2", CoolingDevice.class);
            assertNotNull("getSubResource(name, type) returned null", cd);
			cd.temperatureSensor().reading().create();
		}
		for (int i=0 ;i<nrTopResources-1;i++) {
			CoolingDevice target = devices[i+1].getSubResource("test1", CoolingDevice.class);
			devices[i].getSubResource("test1", CoolingDevice.class).getSubResource("test2", CoolingDevice.class).setAsReference(target);
			assertLocationsEqual(target, devices[i].getSubResource("test1", CoolingDevice.class).getSubResource("test2", CoolingDevice.class));
			// check transitive locations
			CoolingDevice found = devices[0].getSubResource("test1", CoolingDevice.class);
			for (int j=0; j<=i; j++) {
				found = found.getSubResource("test2");
			}
			assertLocationsEqual(target, found);
			assertLocationsEqual(target.temperatureSensor().reading(), found.temperatureSensor().reading());
		}
		// clean up and check that reference deletion works properly
		for (int i=0; i< nrTopResources; i++) {
			int k = nrTopResources - i - 1;
			devices[k].delete();
			if (k > 0)
				assertFalse(devices[k-1].getSubResource("test1", CoolingDevice.class).getSubResource("test2", CoolingDevice.class).exists());
		}
	}
	
	/**
	 * Similar to {@link #multipleReferencesWork()}, but with a more complex reference chain
	 */
	@Test
	public void multipleReferencesWork2() {
		multipleReferencesWorkTest(5);
	}
	
	/**
	 * It turns out that the test result may be quite sensitive to the number of references, so 
	 * we better check for a larger number as well.
	 */
	//@Ignore("Unexpected failure")
	@Test
	public void multipleReferencesWork3() {
		multipleReferencesWorkTest(15);
	}
	
	/**
	 * This test even verifies that garbage collection does not interfere with the weak references in
	 * the DefaultVirtualResourceDB
	 */
	//@Ignore("Unexpected failure")
	@Test
	public void multipleReferencesWork4() {
		multipleReferencesWorkTest(100);
	}
	
	/*
	 * Set a couple of transitive references, and verify that the locations of the resources coincide in the end.
	 */
	@Test
	public void multipleReferencesWorkAndLocationsCoincide() {
		int nr = 10;
		String suffix = newResourceName();
		CoolingDevice[] devices = new CoolingDevice[nr];
		for (int i=0;i<nr;i++) {
			devices[i] = resMan.createResource("fridge" + i + "_" + suffix, CoolingDevice.class);
			devices[i].temperatureSensor().reading().create();
		}
		for (int i=0 ;i<nr-1;i++) {
			devices[i].temperatureSensor().setAsReference(devices[i+1].temperatureSensor()); // -> ResourceNotFoundException, see also #multipleReferencesWork... this is not the focus of this test
			assertLocationsEqual(devices[i+1].temperatureSensor().reading(), devices[i].temperatureSensor().reading());
            assertEquals("wrong number of references: " + devices[i+1].temperatureSensor().getReferencingResources(null),
                    i+1, devices[i+1].temperatureSensor().getReferencingResources(null).size());
		}
		// test again, since it occured in a similar situation that only the renewed test failed, after all references were set
		for (int i=0 ;i<nr-1;i++) {
			assertLocationsEqual(devices[i+1].temperatureSensor().reading(), devices[i].temperatureSensor().reading());
		}
		assertLocationsEqual(devices[nr-1].temperatureSensor().reading(), devices[0].temperatureSensor().reading());
		
	}
	
	@Test
	public void referenceSwitchingGetsLocationsRight() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		
		sensor1.setAsReference(sensor2);
        
        assertEquals(1, sensor2.getReferencingResources(null).size());
		sensor2.setAsReference(sensor3);
		
        assertEquals(sensor2.getReferencingResources(null).toString(), 1, sensor2.getReferencingResources(null).size());
        assertEquals(sensor3.getReferencingResources(null).toString(), 2, sensor3.getReferencingResources(null).size());
        
		assertEquals(sensor3.getPath(), sensor3.getLocation());
		assertNotEquals(sensor2.getPath(), sensor2.getLocation());
		assertNotEquals(sensor1.getPath(), sensor1.getLocation());
		assertLocationsEqual(sensor3, sensor1);
		assertLocationsEqual(sensor3, sensor2);
		assertLocationsEqual(sensor2, sensor1);
		
		// now we switch one reference, and check whether everything still works correctly
		
		sensor2.setAsReference(sensor4);
        assertEquals(sensor4.getReferencingResources(null).toString(), 2, sensor4.getReferencingResources(null).size());
        //sensor2.<CoolingDevice>getParent().temperatureSensor().setAsReference(sensor4.<CoolingDevice>getParent().temperatureSensor());
        
        assertLocationsEqual(sensor2, sensor4);
		assertNotEquals(sensor2.getPath(), sensor2.getLocation()); 
        sensor1.getLocation();
		assertNotEquals(sensor1.getPath(), sensor1.getLocation());
		assertEquals(sensor3.getPath(), sensor3.getLocation());
		assertEquals(sensor4.getPath(), sensor4.getLocation());
		System.out.println("   sensor4   path: " + sensor4.getPath() + ";   location: " + sensor4.getLocation());
		System.out.println("   sensor1   path: " + sensor1.getPath() + ";   location: " + sensor1.getLocation());
		System.out.println("   sensor2   path: " + sensor2.getPath() + ";   location: " + sensor2.getLocation());
		assertNotEquals(sensor2.getPath(), sensor2.getLocation()); // same test as above, but now it fails (until one comments out the Sysout lines above...)
		assertLocationsEqual(sensor2, sensor1); 
		assertLocationsEqual(sensor4, sensor1); // fails 
		assertLocationsEqual(sensor4, sensor2);
	}
	
	/**
	 * Similar to {@link #referenceSwitchingGetsLocationsRight()}, but with more switching of references
	 */
	@Test
	public void iteratedReferenceSwitchingGetsLocationsRight() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor5 = resMan.createResource("fridge5_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor6 = resMan.createResource("fridge6_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor7 = resMan.createResource("fridge7_" + suffix, CoolingDevice.class).temperatureSensor().create();
		
		// initial set-up 
		sensor1.setAsReference(sensor2);
		sensor2.setAsReference(sensor3);
		sensor3.setAsReference(sensor4);
		
		assertEquals(sensor4.getPath(), sensor4.getLocation());
		assertNotEquals(sensor3.getPath(), sensor3.getLocation());
		assertNotEquals(sensor2.getPath(), sensor2.getLocation());
		assertNotEquals(sensor1.getPath(), sensor1.getLocation());
		assertLocationsEqual(sensor4, sensor1);
		assertLocationsEqual(sensor4, sensor2);
		assertLocationsEqual(sensor4, sensor3);
		assertLocationsEqual(sensor3, sensor1);
		
		// now we switch one reference, and check whether everything still works correctly
		
		sensor3.setAsReference(sensor5);
		assertEquals(sensor5.getPath(), sensor5.getLocation());
		assertEquals(sensor4.getPath(), sensor4.getLocation());
		assertNotEquals(sensor3.getPath(), sensor3.getLocation());
		assertNotEquals(sensor2.getPath(), sensor2.getLocation());
		assertNotEquals(sensor1.getPath(), sensor1.getLocation());
		assertLocationsEqual(sensor5, sensor3); 
		assertLocationsEqual(sensor5, sensor2); 
		assertLocationsEqual(sensor5, sensor1);  
		assertLocationsEqual(sensor3, sensor1);
		
		// now we switch once again the same reference, and check whether everything still works correctly
		
		sensor3.setAsReference(sensor6);
		assertEquals(sensor5.getPath(), sensor5.getLocation());
		assertEquals(sensor6.getPath(), sensor6.getLocation());
		assertNotEquals(sensor3.getPath(), sensor3.getLocation());
		assertNotEquals(sensor2.getPath(), sensor2.getLocation());
		assertNotEquals(sensor1.getPath(), sensor1.getLocation());
		assertLocationsEqual(sensor6, sensor3); 
		assertLocationsEqual(sensor6, sensor2); 
		assertLocationsEqual(sensor6, sensor1); 
		assertLocationsEqual(sensor3, sensor1);
		
		// now we set a different reference, and check whether everything still works correctly
		
		sensor2.setAsReference(sensor7);
		assertEquals(sensor5.getPath(), sensor5.getLocation());
		assertEquals(sensor6.getPath(), sensor6.getLocation());
		assertEquals(sensor7.getPath(), sensor7.getLocation());
		assertNotEquals(sensor3.getPath(), sensor3.getLocation());
		assertNotEquals(sensor2.getPath(), sensor2.getLocation());
		assertNotEquals(sensor1.getPath(), sensor1.getLocation());
		assertNotEquals(sensor1.getLocation(), sensor3.getLocation());
		assertLocationsEqual(sensor6, sensor3);  
		assertLocationsEqual(sensor7, sensor2);  
		assertLocationsEqual(sensor7, sensor1);
		
		sensor7.delete();
		assertTrue(sensor3.exists());
		assertFalse(sensor2.exists());
		assertFalse(sensor1.exists());
		sensor6.delete();
		assertFalse(sensor3.exists());
		sensor5.delete();
		sensor4.delete();
	}
	
	/**
	 * Similar to {@link #referenceSwitchingGetsLocationsRight()}, but with longer chain of resources
	 * TODO check (when other tests run through)
	 */
	@Test
	public void multipleReferencesSurviveReferenceSwitching() {
		int nr = 10;  
		String suffix = newResourceName();
		CoolingDevice[] devices = new CoolingDevice[nr];
		for (int i=0;i<nr;i++) {
			devices[i] = resMan.createResource("fridge" + i + "_" + suffix, CoolingDevice.class);
			devices[i].temperatureSensor().reading().create();
		}
		for (int i=0 ;i<nr-1;i++) {
			devices[i].temperatureSensor().setAsReference(devices[i+1].temperatureSensor());
            assertEquals("wrong number of references: " + devices[i+1].temperatureSensor().getReferencingResources(null),
                    i+1, devices[i+1].temperatureSensor().getReferencingResources(null).size());
		}
		CoolingDevice newFridge1 = resMan.createResource("newFridge1", CoolingDevice.class);
		CoolingDevice newFridge2 = resMan.createResource("newFridge2", CoolingDevice.class);
		newFridge1.temperatureSensor().reading().create();
		newFridge2.temperatureSensor().reading().create();
		devices[nr-2].temperatureSensor().setAsReference(newFridge1.temperatureSensor()); // switch one reference
		for (int i=0; i<nr-1; i++) {
            System.out.println(" checking " + devices[i].temperatureSensor() );
			assertLocationsEqual(newFridge1.temperatureSensor(), devices[i].temperatureSensor());
		}
		assertNotEquals(newFridge1.temperatureSensor(), devices[nr-1].temperatureSensor());
		devices[2].temperatureSensor().setAsReference(newFridge2.temperatureSensor()); // switch another reference
        
		assertLocationsEqual(newFridge2.temperatureSensor(), devices[0].temperatureSensor());
		assertLocationsEqual(newFridge2.temperatureSensor(), devices[1].temperatureSensor());
		assertLocationsEqual(newFridge2.temperatureSensor(), devices[2].temperatureSensor());
		assertLocationsEqual(devices[0].temperatureSensor(),devices[2].temperatureSensor());
		for (int i=3; i<nr-1; i++) {
            System.out.println(" checking " + devices[i].temperatureSensor() );
			assertLocationsEqual(newFridge1.temperatureSensor(), devices[i].temperatureSensor());
			assertNotEquals(newFridge1.temperatureSensor(), devices[i].temperatureSensor());
		}
		newFridge1.delete();
		newFridge2.delete();
		for (int i=0;i<nr;i++) {
			devices[i].delete();
		}
	}
	
	/**
	 *  Similar to {@link #referenceSwitchingGetsLocationsRight()}, but here the references are not directly transitive,
	 *  instead there are references at different depths in the resource trees
	 *  
	 *  @see ReferenceTest#referenceSwitchingGetsLocationsRight()
	 */
	@Test
	public void referenceSwitchingGetsLocationsRight2() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor subsensor1 = sensor1.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor2 = sensor2.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor3 = sensor3.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor4 = sensor4.getSubResource("subsensor", TemperatureSensor.class).create();
		
		subsensor1.setAsReference(sensor2);
		subsensor2.setAsReference(sensor3);
		
		assertEquals(sensor3.getPath(), sensor3.getLocation());
		assertNotEquals(subsensor2.getPath(), subsensor2.getLocation());
		assertNotEquals(subsensor1.getPath(), subsensor1.getLocation());
		assertLocationsEqual(sensor3, subsensor1.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor3, subsensor2);
		assertLocationsEqual(sensor2, sensor1.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(subsensor3, subsensor1.getSubResource("subsensor", TemperatureSensor.class).getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(subsensor3, subsensor2.getSubResource("subsensor", TemperatureSensor.class));
		
		// now we switch one reference, and check whether everything still works correctly
		
		subsensor2.setAsReference(sensor4);
		assertNotEquals(subsensor2.getPath(), subsensor2.getLocation()); 
		assertNotEquals(subsensor1.getPath(), subsensor1.getLocation());
		assertEquals(sensor3.getPath(), sensor3.getLocation());
		assertEquals(sensor4.getPath(), sensor4.getLocation());
		assertLocationsEqual(sensor2, sensor1.getSubResource("subsensor", TemperatureSensor.class)); 
		assertLocationsEqual(sensor4, subsensor1.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor4, subsensor2);
		assertLocationsEqual(subsensor4, subsensor1.getSubResource("subsensor", TemperatureSensor.class).getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(subsensor4, subsensor2.getSubResource("subsensor", TemperatureSensor.class));
		
		sensor4.delete();
		sensor3.delete();
		sensor2.delete();
		sensor1.delete();
	}
	
	/**
	 * Similar to {@link #referenceSwitchingGetsLocationsRight2()}, but with longer reference chains
	 * 
	 * @see #iteratedReferenceSwitchingGetsLocationsRight()
	 * @see #referenceSwitchingGetsLocationsRight2()
	 */
	@Test
	public void iteratedReferenceSwitchingGetsLocationsRight2() {
		String suffix = newResourceName();
		TemperatureSensor sensor1 = resMan.createResource("fridge1_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor2 = resMan.createResource("fridge2_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor3 = resMan.createResource("fridge3_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor4 = resMan.createResource("fridge4_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor5 = resMan.createResource("fridge5_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor sensor6 = resMan.createResource("fridge6_" + suffix, CoolingDevice.class).temperatureSensor().create();
		TemperatureSensor subsensor1 = sensor1.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor2 = sensor2.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor3 = sensor3.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor4 = sensor4.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor5 = sensor5.getSubResource("subsensor", TemperatureSensor.class).create();
		TemperatureSensor subsensor6 = sensor6.getSubResource("subsensor", TemperatureSensor.class).create();
		
		subsensor1.setAsReference(sensor2);
		subsensor2.setAsReference(sensor3);
		subsensor3.setAsReference(sensor4);
		
		// now we switch one reference, and check that still everything works fine
		
		subsensor3.setAsReference(sensor5);
		assertNotEquals(subsensor3.getPath(), subsensor3.getLocation()); 
		assertNotEquals(subsensor2.getPath(), subsensor2.getLocation()); 
		assertNotEquals(subsensor1.getPath(), subsensor1.getLocation());
		assertEquals(subsensor4.getPath(), subsensor4.getLocation());
		assertEquals(subsensor5.getPath(), subsensor5.getLocation());
		assertLocationsEqual(sensor2, subsensor1); 
		assertLocationsEqual(sensor3, subsensor1.getSubResource("subsensor", TemperatureSensor.class)); 
		assertLocationsEqual(sensor5, subsensor1.getSubResource("subsensor", TemperatureSensor.class).getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor3, subsensor2); 
		assertLocationsEqual(sensor5, subsensor2.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor5, subsensor3);
		
		// now we switch another reference, and check that still everything works fine

		subsensor2.setAsReference(sensor6);
		assertNotEquals(subsensor3.getPath(), subsensor3.getLocation()); 
		assertNotEquals(subsensor2.getPath(), subsensor2.getLocation()); 
		assertNotEquals(subsensor1.getPath(), subsensor1.getLocation());
		assertEquals(subsensor4.getPath(), subsensor4.getLocation());
		assertEquals(subsensor5.getPath(), subsensor5.getLocation());
		assertEquals(subsensor6.getPath(), subsensor6.getLocation());
		assertLocationsEqual(sensor2, subsensor1); 
		assertLocationsEqual(sensor6, subsensor1.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor6, subsensor2); 
		assertLocationsEqual(sensor5, subsensor3);
		
		// now we switch another reference, and check that still everything works fine

		subsensor6.setAsReference(sensor3);
		assertLocationsEqual(sensor3, subsensor6);
		assertLocationsEqual(sensor5, subsensor6.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor6, subsensor1.getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor3, subsensor1.getSubResource("subsensor", TemperatureSensor.class).getSubResource("subsensor", TemperatureSensor.class));
		assertLocationsEqual(sensor5, subsensor1.getSubResource("subsensor", TemperatureSensor.class)
				.getSubResource("subsensor", TemperatureSensor.class).getSubResource("subsensor", TemperatureSensor.class));
		
		sensor4.delete();
		sensor5.delete();
		sensor6.delete();
		sensor3.delete();
		sensor2.delete();
		sensor1.delete();
	}
	
	@Test
	public void decoratorTypeChangesWork() {
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		String decoratorName = "testDecorator";
		// set a first reference
		switchBox.addDecorator(decoratorName, light);
		// set a reference to a resource whose type is incompatible to the existing one 
		switchBox.addDecorator(decoratorName, fridge);
		Resource decorator = switchBox.getSubResource(decoratorName);
		Assert.assertNotNull("subresource not found", decorator);
		assertLocationsEqual(decorator, fridge);
		assertLocationsDiffer(decorator, light);
		assertEquals("Unexpected resource type", CoolingDevice.class, decorator.getResourceType());
		// once again, this time using a different method to set the reference
		decorator.setAsReference(light); 
		Assert.assertNotNull("subresource not found", decorator);
		assertLocationsEqual(decorator, light);
		assertLocationsDiffer(decorator, fridge);
		assertEquals("Unexpected resource type", ElectricLight.class, decorator.getResourceType());
		light.delete();
		switchBox.delete();
		fridge.delete();
	}
	
	@Test
	public void decoratorTypeChangesWorksForVirtualSubresource() {
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		String sub = "testDecorator";
		// set a first reference
		Resource subRes = switchBox.getSubResource(sub, ElectricCurrentSensor.class); // virtual resource
		subRes.setAsReference(light);
		assertLocationsEqual(subRes, light);
		Assert.assertEquals(ElectricLight.class, switchBox.getSubResource(sub).getResourceType()) ;
		switchBox.delete();
		light.delete();
	}
	
	@Test 
	public void resourceListElementTypeChangesWork() {
		@SuppressWarnings("unchecked")
		ResourceList<PhysicalElement> devices =  resMan.createResource(newResourceName(), ResourceList.class);
		devices.setElementType(PhysicalElement.class);
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		
		Resource element1 = devices.add(switchBox);
		element1.setAsReference(light);
		
	}
		
	// comprises several tests involving type changes of resource list elements; cf. the basic test above
	@Test 
	public void resourceListElementTypeChangesWork2() {
		@SuppressWarnings("unchecked")
		ResourceList<PhysicalElement> devices =  resMan.createResource(newResourceName(), ResourceList.class);
		devices.setElementType(PhysicalElement.class);
		SingleSwitchBox switchBox = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		ElectricCurrentSensor currentSens = switchBox.electricityConnection().currentSensor().create();
		
		// we create 3 list elements in different ways, and perform some tests for all of them
		Resource element1 = devices.add(switchBox);
		Resource element2 = devices.add(CoolingDevice.class);
		Resource element3 = devices.addDecorator("testDecorator", currentSens);
		
		// set a reference to a resource whose type is incompatible to the existing one 
		Resource ref1 = element1.setAsReference(light);
		Resource ref1a = devices.getSubResource(element1.getName()); // actually the same resource as ref1
		
		assertLocationsEqual(ref1, light);
		assertLocationsDiffer(ref1, switchBox);
		assertEquals("Unexpected resource type", ElectricLight.class, ref1.getResourceType());
		Assert.assertNotNull("subresource not found", ref1a);
		Assert.assertEquals("Resources should be equal", ref1, ref1a);
		
		// change references several times 
		element2.setAsReference(switchBox).setAsReference(element1).setAsReference(currentSens);
		
		assertLocationsEqual(element2, currentSens);
		assertLocationsDiffer(element2, element1);
		assertLocationsDiffer(element2, switchBox);
		assertLocationsEqual(element1, ref1);
		assertLocationsEqual(element1, light);
		
		// this replaces the element 2 reference by a new reference to light
		Resource ref2 = devices.addDecorator(element2.getName(), light); 
		Assert.assertEquals(element2, ref2);
		assertLocationsEqual(element2, light);
		assertLocationsDiffer(element2, currentSens);
		
		// set a reference to a resource whose type is incompatible to the existing one 
		Resource sub3 = devices.getSubResource(element3.getName());
		Assert.assertNotNull("subresource not found", sub3);
		sub3.setAsReference(switchBox);
		
		Assert.assertEquals(sub3, element3);
		assertLocationsEqual(element3, switchBox);
		assertLocationsDiffer(element3, currentSens);

		// basic consistency check
		Assert.assertEquals("Unexpected number of elements in a resource list", 3, devices.getAllElements().size());
		
		devices.delete();
		currentSens.delete();
		switchBox.delete();
		light.delete();
	}
	
	@Test(expected=ResourceAlreadyExistsException.class)
	public void resourceListElementTypeIsConstrainedToListType() {
		@SuppressWarnings("unchecked")
		ResourceList<SingleSwitchBox> devices =  resMan.createResource(newResourceName(), ResourceList.class);
		devices.setElementType(SingleSwitchBox.class);
		SingleSwitchBox switchBox = devices.add();
		ElectricLight light = resMan.createResource(newResourceName(), ElectricLight.class);
		switchBox.setAsReference(light); // -> exception expected
	}
	
	@Test(expected=VirtualResourceException.class)
	public void settingReferenceOnVirtualResourceThrowsException() {
		final TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		Resource dec = tempSens.location().room().addDecorator("a", tempSens);
	}
	
	@Test(timeout=5000)
	public void referenceLoopsWork1() {
		final TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final Room room = resMan.createResource(newResourceName(), Room.class);
		tempSens.location().room().create().addDecorator("a", tempSens); // loop
		tempSens.location().room().create().addDecorator("b", tempSens); // loop
        /*
        String path = "ReferenceTest_1/location/room/a/location/room/a/location/room/a/location/room/a/location/room/a/location/room/a/location/room/a/location/room/b/location/room/b/location/room/b/location/room/a/location/room/a/location/room/a/location/room/b/location/room/a/location/room/b/location/room/b/location/room/a/location/room/a/location/room/a/location/room/a";
        Resource r = resAcc.getResource(path);
        assertExists(r);
        assertEquals(path, r.getPath());
        final Resource res = resMan.createResource(newResourceName(), Resource.class);
        Resource dec = res.addDecorator("fnord", r);
        Resource decLoc = dec.getLocationResource();
        assertEquals(decLoc.getReferencingNodes(true).toString(), 1, decLoc.getReferencingNodes(true).size());
        */
		tempSens.location().room().setAsReference(room); // breaks loop
        tempSens.delete();
		room.delete();
	}

	// like test above, but with a more complex reference loop, involving two subtrees
	@Test(timeout=5000)
	public void referenceLoopsWork2() {
		final TemperatureSensor tempSens1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureSensor tempSens2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final Room room = resMan.createResource(newResourceName(), Room.class);
		tempSens1.location().room().create().addDecorator("a", tempSens2);
		tempSens1.location().room().create().addDecorator("b", tempSens2);
		tempSens2.location().room().create().addDecorator("a", tempSens1);
		tempSens2.location().room().create().addDecorator("b", tempSens1);
		tempSens2.location().room().setAsReference(room);
		tempSens1.location().room().setAsReference(tempSens2.location().room());
		tempSens1.delete();
		tempSens2.delete();
		room.delete();
	}
	
	// like test above, but with a more complex reference loop, involving many subtrees
	@Test(timeout=5000)
	public void referenceLoopsWork3() {
		final TemperatureSensor tempSens1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureSensor tempSens2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureSensor tempSens3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureSensor tempSens4 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureSensor tempSens5 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		tempSens1.location().room().create().addDecorator("a", tempSens2);
		tempSens1.location().room().create().addDecorator("b", tempSens4);
		tempSens2.location().room().<Room> create().temperatureSensor().setAsReference(tempSens3);
		tempSens4.location().room().<Room> create().temperatureSensor().setAsReference(tempSens5);
		tempSens3.location().room().<Room> create().temperatureSensor().setAsReference(tempSens5);
		tempSens5.location().room().<Room> create().addDecorator("a", tempSens1); // loop closed
		tempSens5.location().room().<Room> create().addDecorator("b", tempSens1); // loop closed
		tempSens1.location().room().setAsReference(tempSens3.location().room());
		tempSens2.location().room().setAsReference(tempSens4.location().room());
		tempSens3.location().room().setAsReference(tempSens5.location().room());
		tempSens5.location().room().setAsReference(tempSens2.location().room()); // loop closed
		
		tempSens1.delete();
		tempSens2.delete();
		tempSens3.delete();
		tempSens4.delete();
		tempSens5.delete();
	}
	
	private static void assertLocationsEqual(Resource expected, Resource found) {
		Assert.assertTrue("Reference location not equal to reference's path; expected " + expected.getLocation() + ", got " + found.getLocation(),
				expected.equalsLocation(found));
	}
	
	private static void assertLocationsDiffer(Resource res1, Resource res2) {
		Assert.assertNotEquals("Reference locations unexpectedly equal for paths " + res1.getPath() + " and " + res2.getPath(),
				res1.getLocation(), res2.getLocation());
	}
    
    //used in loop to expose concurrency bugs
    @Test public void listenerCallbacksWorkOnDoubleReferences() throws InterruptedException {
        final CountDownLatch heaterDemandLatch = new CountDownLatch(1);
        final StructureTestListener[] stlA = new StructureTestListener[1];
        final AtomicBoolean alreadyActive = new AtomicBoolean(false);
        
        final ElectricHeater heater = resMan.createResource("Heater_"+newResourceName(), ElectricHeater.class);
        
        ResourceDemandListener<ElectricHeater> rdl = new ResourceDemandListener<ElectricHeater>() {

            @Override
            public void resourceAvailable(ElectricHeater resource) {
                if (!resource.equals(heater)) {
                    return;
                }
                System.out.println("resource available: " + resource.getPath());
                stlA[0] = new StructureTestListener();
                Objects.requireNonNull(resource.location(), "location");
                Objects.requireNonNull(resource.location().room(), "location.room");
                Objects.requireNonNull(resource.location().room().temperatureSensor(), "location.room.temperatureSensor");
                Objects.requireNonNull(resource.location().room().temperatureSensor().reading(), "location.room.temperatureSensor.reading");
                TemperatureResource t = resource.location().room().temperatureSensor().reading();
                t.addStructureListener(stlA[0]);
                alreadyActive.set(t.isActive());
                System.out.printf("added structure listener, resource active: %b%n", alreadyActive.get());
                heaterDemandLatch.countDown();
            }

            @Override
            public void resourceUnavailable(ElectricHeater resource) {
            }
        };
        getApplicationManager().getResourceAccess().addResourceDemand(ElectricHeater.class, rdl);
        
        Room room = resMan.createResource("Room_"+newResourceName(), Room.class);
        TemperatureSensor tempSens = resMan.createResource("TempSens_"+newResourceName(), TemperatureSensor.class);        
        
        heater.activate(true);
        room.activate(true);
        tempSens.activate(true);
        
        heater.location().room().setAsReference(room);
        assertEquals(Arrays.asList(heater.location()), room.getReferencingResources(Resource.class));

        room.temperatureSensor().setAsReference(tempSens);
        assertEquals(Arrays.asList(room), tempSens.getReferencingResources(Resource.class));
        
        tempSens.reading().create();
        tempSens.reading().activate(false);
        
        assertTrue(heaterDemandLatch.await(5, TimeUnit.SECONDS));
        assertTrue("not active and no callback", alreadyActive.get() || stlA[0].awaitActivate(5, TimeUnit.SECONDS));
        
        getApplicationManager().getResourceAccess().removeResourceDemand(ElectricHeater.class, rdl);
        heater.location().room().temperatureSensor().reading().removeStructureListener(stlA[0]);
        
        heater.delete();
        room.delete();
        tempSens.delete();
    }
    
    @Test
    public void setReferenceOnReferenceWorks() {
    	final TemperatureSensor sensor = resMan.createResource(newResourceName(), TemperatureSensor.class);
    	final Room room = resMan.createResource(newResourceName(), Room.class);
    	Assert.assertNotNull(sensor.location().room()); // -> comment out this line and it works!
        ResourceAssertions.assertIsVirtual(sensor.location().room());
    	final PhysicalElement someResource = resMan.createResource(newResourceName(), PhysicalElement.class);
    	final TemperatureSensor sensorRef = someResource.addDecorator("sensor", sensor);
    	sensorRef.location().room().setAsReference(room);
    	ResourceAssertions.assertLocationsEqual(room, sensor.location().room());
    	sensor.delete();
    	room.delete();
    	someResource.delete();
    }
    
    @Test
    public void deletingReferenceDoesNotTriggerCallbackOnLocation() throws InterruptedException {
    	final TemperatureSensor sensor = resMan.createResource(newResourceName(), TemperatureSensor.class);
    	final Room room = resMan.createResource(newResourceName(), Room.class);
    	Assert.assertNotNull(sensor.location().room());
        ResourceAssertions.assertIsVirtual(sensor.location().room());
    	final PhysicalElement someResource = resMan.createResource(newResourceName(), PhysicalElement.class);
    	final TemperatureSensor sensorRef = someResource.addDecorator("sensor", sensor);
    	sensorRef.location().room().setAsReference(room);
    	ResourceAssertions.assertLocationsEqual(room, sensor.location().room());
        StructureTestListener stl = new StructureTestListener();
        StructureTestListener refStructureListener = new StructureTestListener();
        sensor.addStructureListener(stl);
        sensorRef.addStructureListener(refStructureListener);
        sensorRef.delete();
        assertExists(sensor);
        assertTrue(refStructureListener.awaitEvent(ResourceStructureEvent.EventType.RESOURCE_DELETED));
        assertFalse("resource deleted event for location resource", stl.awaitEvent(ResourceStructureEvent.EventType.RESOURCE_DELETED));
    	sensor.delete();
    	room.delete();
    	someResource.delete();
    }
    
    @Test
    public void setReferenceOnReferenceWorks2() {
    	final TemperatureSensor sensor = resMan.createResource(newResourceName(), TemperatureSensor.class);
    	final Room room = resMan.createResource(newResourceName(), Room.class);
    	Assert.assertNotNull(sensor.location().room());
    	final PhysicalElement someResource = resMan.createResource(newResourceName(), PhysicalElement.class);
    	final TemperatureSensor sensorRef = someResource.addDecorator("sensor", sensor);
    	sensorRef.getSubResource("room", Room.class).setAsReference(room);
    	ResourceAssertions.assertLocationsEqual(room, sensor.getSubResource("room"));
    	sensor.delete();
    	room.delete();
    	someResource.delete();
    }

    @Test
    public void createdCallbackForReferencedResourceWorks() throws InterruptedException {
    	final TemperatureSensor sensor = resMan.createResource(newResourceName(), TemperatureSensor.class);
    	final StructureTestListener listener = new StructureTestListener();
    	sensor.location().room().addStructureListener(listener);
    	final Room room = resMan.createResource(newResourceName(), Room.class);
    	final PhysicalElement someResource = resMan.createResource(newResourceName(), PhysicalElement.class);
    	final TemperatureSensor sensorRef = someResource.addDecorator("sensor", sensor);
    	sensorRef.location().room().setAsReference(room);
        assertEquals(1, room.getReferencingNodes(true).size());
    	Assert.assertTrue("Missing create callback",listener.awaitCreate(5, TimeUnit.SECONDS));
    	sensor.delete();
    	room.delete();
    	someResource.delete();
    }
  
	
}
