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

import org.ogema.exam.DemandTestListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.PowerResource;

import static org.ogema.exam.ResourceAssertions.assertDeleted;
import static org.ogema.exam.ResourceAssertions.assertDirectSubresource;
import static org.ogema.exam.ResourceAssertions.assertExists;
import static org.ogema.exam.ResourceAssertions.assertIsVirtual;
import static org.ogema.exam.ResourceAssertions.assertReference;

import org.ogema.model.locations.Location;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.targetranges.PowerTargetRange;
import org.ogema.resourcemanager.impl.ConnectedResource;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class DeleteTest extends OsgiTestBase {

	@Test(expected = AssertionError.class)
	public void testAssertionsEnabled() {
		assert (false);
	}

	@Test
	public void simpleSubResourceDeleteWorks() {
		PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		assertIsVirtual(pe.location());
		pe.location().create();
		assertExists(pe.location());
		pe.location().delete();
		assertDeleted(pe.location());
	}

	@Test
	public void deleteRemovesSubTree() {
		PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		Resource loc = pe.location().geographicLocation().latitudeArcMinutes();
		assertIsVirtual(pe.location());
		assertIsVirtual(loc);
		loc.create();
		assertExists(pe.location());
		assertExists(loc);
		pe.location().delete();
		assertDeleted(pe.location());
		assertDeleted(pe.location().geographicLocation().latitudeArcMinutes());
		assertDeleted(loc);
	}

	@Test
	public void deleteWorksOnTopLevelResource() {
		String name = newResourceName();
		PhysicalElement pe = resMan.createResource(name, PhysicalElement.class);
		pe.location().geographicLocation().latitudeArcMinutes().create();
		assertNotNull(resAcc.getResource("/" + name));

		pe.delete();
		assertNull(resAcc.getResource("/" + name));
		assertDeleted(pe);
	}

	@Test
	public void deleteWorksOnTopLevelResourceList() {
		String name = newResourceName();
		@SuppressWarnings("unchecked")
		ResourceList<Resource> l = resMan.createResource(name, ResourceList.class);
		l.setElementType(Resource.class);
		l.add(resMan.createResource(newResourceName(), StringResource.class));
		assertNotNull(resAcc.getResource("/" + name));

		l.delete();
		assertNull(resAcc.getResource("/" + name));
		assertDeleted(l);
	}

	@Test
	public void deletionIsVisibleOnOldResourceReference() {
		PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		Resource loc = pe.location();
		assertIsVirtual(pe.location());
		assertIsVirtual(loc);
		pe.location().create();
		assertExists(loc);
		pe.location().delete();
		assertDeleted(loc);
	}

	@Test
	public void deleteCausesResourceUnavailableCallbacks() throws InterruptedException {
		final PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		DemandTestListener<Location> listener = new DemandTestListener<>(pe.location());
		getApplicationManager().getResourceAccess().addResourceDemand(Location.class, listener);
		pe.location().create().activate(false);

		listener.assertAvailable();
		pe.location().delete();
		listener.assertUnavailable();
		assertDeleted(pe.location());
	}

	@Test
	public void deletingAReferenceDeletesOnlyTheReference() {
		final PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		final PhysicalElement pe2 = resMan.createResource(newResourceName(), PhysicalElement.class);

		pe2.location().create();
		pe.location().setAsReference(pe2.location());

		assertExists(pe2.location());
		assertDirectSubresource(pe2.location());

		assertExists(pe.location());
		assertReference(pe.location());
		assertTrue(pe.location().equalsLocation(pe2.location()));

		pe.location().delete();

		assertExists(pe2.location());
		assertDirectSubresource(pe2.location());
		assertDeleted(pe.location());
		assertDirectSubresource(pe.location());
		assertFalse(pe.location().equalsLocation(pe2.location()));
	}

	@Test
	public void deletingAReferencedResourceRemovesReference() {
		// (virtual resources cannot be referenced)
		final PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		final PhysicalElement pe2 = resMan.createResource(newResourceName(), PhysicalElement.class);

		pe2.location().create();
		pe.location().setAsReference(pe2.location());

		assertExists(pe2.location());
		assertFalse(pe2.location().isReference(false));
		assertExists(pe.location());
		assertTrue(pe.location().isReference(false));

		pe2.location().delete();
		assertDeleted(pe2.location());
		assertDeleted(pe.location());
		assertFalse(pe.location().isReference(false));
	}

	@Test
	public void deletedResourcesAreVirtualAndInactive() {
		final PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		pe.location().geographicLocation().latitudeArcMinutes().create();
		pe.activate(true);
		assertTrue(pe.location().geographicLocation().latitudeArcMinutes().exists());
		assertTrue(pe.location().exists());
		assertTrue(pe.location().geographicLocation().latitudeArcMinutes().isActive());
		assertTrue(pe.location().isActive());

		pe.location().delete();
		assertDeleted(pe.location().geographicLocation().latitudeArcMinutes());
		assertDeleted(pe.location().geographicLocation());
		assertDeleted(pe.location());
		assertFalse(pe.location().geographicLocation().latitudeArcMinutes().isActive());
		assertFalse(pe.location().isActive());
	}

	@Test
	public void simpleResourcesKeepLastValueAfterDeletion() { // until system shutdown/restart
		final PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		pe.location().geographicLocation().latitudeArcMinutes().create();
		pe.location().geographicLocation().latitudeArcMinutes().setValue(47.11f);
		assertEquals(47.11f, pe.location().geographicLocation().latitudeArcMinutes().getValue(), 0f);
		pe.location().delete();
		assertEquals(47.11f, pe.location().geographicLocation().latitudeArcMinutes().getValue(), 0f);
	}

	@Test
	public void simpleResourcesAreReadOnlyAfterDeletion() {
		final PhysicalElement pe = resMan.createResource(newResourceName(), PhysicalElement.class);
		pe.location().geographicLocation().latitudeFullDegrees().create();
		pe.location().geographicLocation().latitudeFullDegrees().setValue(4711);
		assertEquals(4711, pe.location().geographicLocation().latitudeFullDegrees().getValue());
		pe.location().delete();
		pe.location().geographicLocation().latitudeFullDegrees().setValue(2);
		assertEquals(4711, pe.location().geographicLocation().latitudeFullDegrees().getValue());
	}

	@Test
	public void scheduleReferencesCanBeDeleted() {
		PowerSensor powerSensor = resMan.createResource(newResourceName(), PowerSensor.class);
		Schedule schedule = powerSensor.reading().forecast();
		schedule.create().activate(false);
		PowerSensor powerSensor2 = resMan.createResource(newResourceName(), PowerSensor.class);
		Schedule schedule2 = powerSensor2.reading().forecast();
		schedule2.create().activate(false);
		schedule.setAsReference(schedule2);
		schedule2.delete();
		assertIsVirtual(schedule);
		assertIsVirtual(schedule2);
		schedule2.create();
		schedule.setAsReference(schedule2);
		schedule.delete();
		assertIsVirtual(schedule);
		assertExists(schedule2);
		powerSensor.reading().forecast(); // ensure schedule can be accessed from parent
		schedule.create();
		assert (!schedule.isReference(false)) : "Schedule should not be a reference";
		assertEquals("Schedule should not be a reference", schedule.getPath(), schedule.getLocation());
		powerSensor.delete();
		powerSensor2.delete();
	}

	@Test
	public void deleteReferenceDeletesSubSchedule() {
		PowerSensor powerSensor = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerResource reading = powerSensor.reading();
		reading.forecast().create().activate(false);
		PowerSensor powerSensor2 = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerResource reading2 = powerSensor2.reading();
		reading2.forecast().create().activate(false);
		reading.setAsReference(reading2);
		reading2.delete();
		assertIsVirtual(reading2.forecast());
		assertIsVirtual(powerSensor.reading().forecast());
		assertIsVirtual(reading.forecast()); // ResourceNotFoundException
		powerSensor.delete();
		powerSensor2.delete();
	}

	@Test
	public void XYdeleteReferenceDeletesSubResource() {
		PowerSensor powerSensor = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerTargetRange deviceSettings = powerSensor.deviceSettings();
		assertEquals(((ConnectedResource) deviceSettings).getTreeElement(), ((ConnectedResource) powerSensor
				.deviceSettings()).getTreeElement());
		deviceSettings.setpoint().create();
		assertEquals(((ConnectedResource) deviceSettings).getTreeElement(), ((ConnectedResource) powerSensor
				.deviceSettings()).getTreeElement());
		PowerSensor powerSensor2 = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerTargetRange deviceSettings2 = powerSensor2.deviceSettings();
		deviceSettings2.setpoint().create();
		System.out.println(deviceSettings.getPath());
		System.out.println(deviceSettings2.getPath());
		deviceSettings.setAsReference(deviceSettings2);
		assertTrue(deviceSettings2.setpoint().equalsLocation(deviceSettings.setpoint()));

		System.out.println("-----------------------------");

		deviceSettings2.delete();

		System.out.println("-----------------------------");
		//assertFalse(settings2.setpoint().equalsLocation(settings.setpoint()));
		//System.out.println(settings.setpoint().getLocation());
		//System.out.println(settings2.setpoint().getLocation());
		System.out.println(((ConnectedResource) deviceSettings).getTreeElement().getPath());
		System.out.println(((ConnectedResource) deviceSettings.setpoint()).getTreeElement().getPath());
		assertIsVirtual(deviceSettings2);
		assertIsVirtual(deviceSettings);
		System.out.println("deviceSettings.getPath()=" + deviceSettings.getPath());
		System.out.println("deviceSettings.getLocation()=" + deviceSettings.getLocation());
		System.out.println("deviceSettings.setpoint().getPath()=" + deviceSettings.setpoint().getPath());
		System.out.println("deviceSettings.setpoint().getLocation()=" + deviceSettings.setpoint().getLocation());
		assertIsVirtual(deviceSettings.setpoint());
		assertIsVirtual(powerSensor.deviceSettings().setpoint());
		assertEquals(((ConnectedResource) deviceSettings).getTreeElement(), ((ConnectedResource) powerSensor
				.deviceSettings()).getTreeElement());
		assertIsVirtual(deviceSettings); // ok
		assertIsVirtual(deviceSettings.setpoint()); // fails
		powerSensor.delete();
		powerSensor2.delete();
	}

	/**
	 * Similar to {@link #deleteReferenceDeletesSubSchedule()},
	 * but caches the subschedule resource. Plus checks that the subschedule is not 
	 * a reference after being deleted.
	 */
	@Test
	public void deleteReferenceRemovesSubScheduleReference() {
		PowerSensor powerSensor = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerResource reading = powerSensor.reading();
		Schedule schedule = reading.forecast();
		schedule.create().activate(false);
		assert (schedule.exists()) : "Schedule resource does not exist";
		PowerSensor powerSensor2 = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerResource reading2 = powerSensor2.reading();
		reading2.forecast().create().activate(false);
		reading.setAsReference(reading2);
		reading2.delete();
		assertIsVirtual(schedule); // exists
		schedule.create();
		assert (!schedule.isReference(false));
		assertEquals(schedule.getPath(), schedule.getLocation());
		assert (schedule.exists()) : "Schedule resource does not exist";
		powerSensor.delete();
		powerSensor2.delete();
	}

	/**
	 * Equivalent to {@link DeleteTest#deleteReferenceRemovesSubScheduleReference()},
	 * except that is uses a FloatResource as subresource, instead of a Schedule
	 */
	@Test
	public void deleteReferenceRemovesSubresourceReference() {
		PowerSensor powerSensor = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerResource reading = powerSensor.reading();
		FloatResource subfloat = reading.getSubResource("subfloat", FloatResource.class);
		subfloat.create().activate(false);
		assert (subfloat.exists()) : "Float resource does not exist";
		PowerSensor powerSensor2 = resMan.createResource(newResourceName(), PowerSensor.class);
		PowerResource reading2 = powerSensor2.reading();
		reading2.getSubResource("subfloat2", FloatResource.class).create().activate(false);
		reading.setAsReference(reading2);
		reading2.delete();
		assertIsVirtual(subfloat); // exists
		subfloat.create();
		assert (!subfloat.isReference(false));
		assert (subfloat.getPath().equals(subfloat.getLocation()));
		assert (subfloat.exists()) : "Float resource does not exist";
		powerSensor.delete();
		powerSensor2.delete();
	}

}
