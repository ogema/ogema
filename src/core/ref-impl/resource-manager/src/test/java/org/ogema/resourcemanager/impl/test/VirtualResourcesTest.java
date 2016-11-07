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
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.locations.Location;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.BinaryRange;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class VirtualResourcesTest extends OsgiTestBase {

	@Test
	public void test() {
		OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		assertNotNull(sw);
		assertTrue(sw.exists());
		BinaryRange ratedValues = sw.ratedValues();
		assertNotNull(ratedValues);
		assertFalse(ratedValues.exists());
		assertEquals(BinaryRange.class, ratedValues.getResourceType());
		assertEquals(sw, ratedValues.getParent());
		assertEquals(ratedValues, sw.ratedValues());
		assertNotNull(ratedValues.upperLimit());
		assertEquals(ratedValues.upperLimit(), sw.ratedValues().upperLimit());
		assertFalse(sw.ratedValues().upperLimit().program().exists());

		assertTrue(sw.ratedValues().create().exists());
		assertTrue(sw.ratedValues().exists());
		assertEquals(ratedValues, sw.ratedValues());
		assertTrue(ratedValues.exists());
	}

	@Test
	public void listenerRegisteredOnVirtualResourceWorksAfterCreate() throws InterruptedException {
		final CountDownLatch cdl = new CountDownLatch(1);
		ResourceValueListener<Resource> l = new ResourceValueListener<Resource>() {

			@Override
			public void resourceChanged(Resource resource) {
				cdl.countDown();
			}
		};

		OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		sw.stateControl().addValueListener(l, true);
		assertFalse(sw.stateControl().exists());
		assertTrue(sw.stateControl().create().exists());
		sw.stateControl().activate(false);
		assertEquals(1, cdl.getCount());
		sw.stateControl().setValue(true);
		assertTrue(cdl.await(10, TimeUnit.SECONDS));
	}

	@Test
	public void resourceDemandsWorkForResourcesCreatedWithCreate() throws InterruptedException {
		final Object lock = new Object();
		final Resource[] lastResource = new Resource[1];

		ResourceDemandListener<Location> l = new ResourceDemandListener<Location>() {

			@Override
			public void resourceAvailable(Location resource) {
				synchronized (lock) {
					lastResource[0] = resource;
				}
			}

			@Override
			public void resourceUnavailable(Location resource) {
				//
			}
		};

		OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		resAcc.addResourceDemand(Location.class, l);

		assertFalse(sw.location().exists());

		synchronized (lock) {
			assertTrue(sw.location().create().exists());
			sw.location().activate(true);
			lock.wait(2000);
		}

		assertEquals(sw.location(), lastResource[0]);
	}

	@Test
	public void navigationOverVirtualDecoratorsWorks() {
		OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		Schedule sched = sw.stateControl().getSubResource("switch_schedule", Schedule.class);
		assertNotNull(sched);
		assertTrue(Schedule.class.isAssignableFrom(sched.getResourceType()));
	}

	@Test
	// also tests creation of decorators obtained as virtual resource via getSubResource(name, type)
	public void navigationOverExistingDecoratorsWorks() {
		OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		StringResource fnord = sw.stateControl().getSubResource("xyyz", StringResource.class);
		fnord.create();
		assertTrue(fnord.exists());
		StringResource foo = sw.stateControl().getSubResource("xyyz", StringResource.class);
		assertTrue(foo.exists());
		assertEquals(fnord, foo);
	}

	@Test
	public void setAsReferenceWorks() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		BooleanResource sc1 = sw1.stateControl();
		BooleanResource sc2 = sw2.stateControl();
		sc2.create();
		assertTrue(sc2.exists());
		sc1.setAsReference(sc2);
		assertTrue(sw1.stateControl().exists());
		assertTrue(sc1.exists());
		assertEquals(sc2.getLocation(), sc1.getLocation());
		assertEquals(sc2.getLocation(), sw1.stateControl().getLocation());
	}

	@Test(expected = VirtualResourceException.class)
	public void virtualResourcesCannotBeUsedAsReference() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		assertFalse(sw2.stateControl().exists());
		sw1.stateControl().setAsReference(sw2.stateControl());
	}

	//	@Test(expected = VirtualResourceException.class)  // behaviour changed
	@Test
	public void referencesCannotBeSetOnVirtualResources() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		assertFalse(sw2.physDim().exists());
		sw1.physDim().length().create();
		assertTrue(sw1.physDim().length().exists());
		sw2.physDim().length().setAsReference(sw1.physDim().length());
	}

	@Test
	public void getSubResourceReturnsVirtualResourceOnOptionalElementsOnly() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		assertNotNull(sw1.stateFeedback());
		assertNotNull(sw1.getSubResource("stateFeedback"));
		assertFalse(sw1.getSubResource("stateFeedback").exists());
		assertNull(sw1.getSubResource("noSuchResource"));

		assertNotNull(sw1.getSubResource("newDecorator", Resource.class));
		assertTrue(sw1.getSubResource("newDecorator", Resource.class).isDecorator());
	}

	@Test(expected = NoSuchResourceException.class)
	public void getSubResourceThrowsExceptionOnIncompatibleResource() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		sw1.getSubResource("newDecorator", FloatResource.class).create();
		sw1.getSubResource("newDecorator", BooleanResource.class);
	}
    
   	@Test(expected = NoSuchResourceException.class)
	public void getSubResourceThrowsExceptionOnIncompatibleVirtualResource() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		FloatResource v = sw1.getSubResource("newDecorator", FloatResource.class);
        ResourceAssertions.assertIsVirtual(v);
		sw1.getSubResource("newDecorator", BooleanResource.class);
	}
    
	@Test
	public void getSubResourceWorksWithSuperType() {
		OnOffSwitch sw1 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		sw1.getSubResource("newDecorator", TemperatureSensor.class).create();
		PhysicalElement pe = sw1.getSubResource("newDecorator", PhysicalElement.class);
		assertTrue(pe.exists());
		assertEquals(TemperatureSensor.class, pe.getResourceType());
		assertTrue(pe instanceof TemperatureSensor);
	}

	@Test
	public void getSubResourceWorksForVirtualScheduleDecorator() {
		TemperatureSensor s1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureResource mmxTemp = s1.reading();
		assertFalse(mmxTemp.exists());
		mmxTemp.create();

		assertNotNull(s1.ratedValues().getSubResource("yyz", Resource.class));
		s1.ratedValues().create();
		assertNotNull(s1.ratedValues().getSubResource("yyz", Resource.class));

		Resource x = s1.reading().getSubResource("foo", FloatResource.class);
		assertNotNull(x);
		assertFalse(x.exists());

		Schedule ds = s1.reading().getSubResource("data", Schedule.class);
		assertNotNull(ds);
		assertFalse(ds.exists());
	}

	/**
	 * Tests that a create() call on virtual resource A.B.C is possible, even if
	 * B was virtual when the reference was obtained but became an existing
	 * object in the meantime.
	 */
	@Test
	public void creatingWorksWhenPartOfPathHadBeenCreatedInMeantime() {
		final CoolingDevice fridge = resMan.createResource(newResourceName(), CoolingDevice.class);
		final TemperatureSensor tempSens = fridge.temperatureSensor();
		final FloatResource minTemp = tempSens.settings().controlLimits().lowerLimit();
		final FloatResource maxTemp = tempSens.settings().controlLimits().upperLimit();

		assertFalse(tempSens.exists());
		tempSens.create();
		assertTrue(tempSens.exists());

		assertFalse(tempSens.settings().controlLimits().exists());

		minTemp.create();
		assertTrue(minTemp.exists());
		assertTrue(tempSens.settings().controlLimits().exists());

		assertFalse(maxTemp.exists());
		maxTemp.create();
		assertTrue(maxTemp.exists());
	}

	@Test
	public void creatingVirtualDecoratedScheduleWorks() {
		final FloatResource resource = resMan.createResource(newResourceName(), FloatResource.class);
		final Schedule schedule = resource.getSubResource("myTestDecorator", Schedule.class);
		assertNotNull(schedule);
		assertFalse(schedule.exists());
		schedule.create();
		assertTrue(schedule.exists());
	}

	@Test
	public void creatingVirtualOptionalScheduleWorks() {
		final FloatResource resource = resMan.createResource(newResourceName(), FloatResource.class);
		final Schedule schedule = resource.program();
		assertNotNull(schedule);
		assertFalse(schedule.exists());
		schedule.create();
		assertTrue(schedule.exists());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void virtualSchedulesAreReadable() { //...but return only null
		final FloatResource resource = resMan.createResource(newResourceName(), FloatResource.class);
		final Schedule schedule = resource.program();
		assertNotNull(schedule);
		assertFalse(schedule.exists());
		assertNull(schedule.getValue(42));
		assertTrue(schedule.getValues(0).isEmpty());
		assertNull(schedule.getTimeOfLatestEntry());
	}

	@Test
	public void virtualSchedulesIgnoreWrites() {
		final FloatResource resource = resMan.createResource(newResourceName(), FloatResource.class);
		final Schedule schedule = resource.program();
		assertNotNull(schedule);
		assertFalse(schedule.exists());
		schedule.addValue(47, new FloatValue(11));
		assertTrue(schedule.getValues(0).isEmpty());
	}

	@SuppressWarnings("unused")
	@Test
	public void replacingReferencesIsVisibleOnExistingResourceObjects() {
		final OnOffSwitch sw = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw2 = resMan.createResource(newResourceName(), OnOffSwitch.class);
		final OnOffSwitch sw3 = resMan.createResource(newResourceName(), OnOffSwitch.class);

		Resource sw2setpoint = sw2.settings().setpoint().create();
		Resource sw3setpoint = sw3.settings().setpoint().create();

		Resource storedResourceL1 = sw.settings();
		Resource storedResourceL2 = sw.settings().setpoint();

		sw.settings().setAsReference(sw2.settings());
		assertTrue("creating reference yields inconsistent view", storedResourceL1.equalsLocation(sw2.settings()));
		assertTrue("creating reference yields inconsistent view of subresources", storedResourceL2.equalsLocation(sw2
				.settings().setpoint()));

		sw.settings().setAsReference(sw3.settings());
		assertTrue("replacing reference yields inconsistent view", storedResourceL1.equalsLocation(sw3.settings()));
		assertTrue("replacing reference yields inconsistent view of subresources", storedResourceL2.equalsLocation(sw3
				.settings().setpoint()));
	}

	@Test
	public void creatingVirtualResourcesWithSubtreeWorks() {
		Thermostat th = resMan.createResource(newResourceName(), Thermostat.class);
		TemperatureResource setpoint = th.temperatureSensor().settings().setpoint(); // virtual
		th.temperatureSensor().create(); 
		th.temperatureSensor().settings().setpoint();
		Assert.assertEquals("Something went wrong during creation of a virtual resource",th.temperatureSensor().settings(), setpoint.getParent());
		th.delete();
	}
	
	@Test
	public void virtualSubelementsDontFailWhenParentIsRecreated() {
		SingleSwitchBox box1 = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		SingleSwitchBox box2 = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		box1.electricityConnection().create();
		box2.electricityConnection().create();
		PowerResource sensor = box1.electricityConnection().powerSensor().reading();
		box1.electricityConnection().powerSensor().reading(); // create virtual resources
		box1.electricityConnection().setAsReference(box2.electricityConnection());
		sensor.create();
		ResourceAssertions.assertExists(sensor);
		ResourceAssertions.assertLocationsEqual(sensor, box2.electricityConnection().powerSensor().reading());
		box1.delete();
		box2.delete();
	}
	
	@Test
	public void existingSubelementsDontFailWhenParentIsSetAsReference() {
		SingleSwitchBox box1 = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		SingleSwitchBox box2 = resMan.createResource(newResourceName(), SingleSwitchBox.class);
		box1.electricityConnection().create();
		box2.electricityConnection().create();
		PowerSensor sensor = box1.electricityConnection().powerSensor().create();
		box1.electricityConnection().setAsReference(box2.electricityConnection());
		ResourceAssertions.assertIsVirtual(sensor);
		sensor.create();
		ResourceAssertions.assertExists(sensor);
		ResourceAssertions.assertLocationsEqual(sensor, box2.electricityConnection().powerSensor());
		box1.delete();
		box2.delete();
	}


}
