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

import org.ogema.exam.DemandTestListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.ogema.core.model.Resource;
import static org.ogema.exam.ResourceAssertions.assertDeleted;
import static org.ogema.exam.ResourceAssertions.assertDirectSubresource;
import static org.ogema.exam.ResourceAssertions.assertExists;
import static org.ogema.exam.ResourceAssertions.assertIsVirtual;
import static org.ogema.exam.ResourceAssertions.assertReference;
import org.ogema.model.locations.Location;
import org.ogema.model.prototypes.PhysicalElement;
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

}
