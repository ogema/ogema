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
package org.ogema.persistence.impl.mem;

import static org.junit.Assert.*;
import org.junit.Test;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.resourcetree.TreeElement;

/**
 * 
 * @author jlapp
 */
public class ResourceDBTest extends TestBase {

	@Test
	public void addResourceTypeWorks() throws Exception {
		db.addOrUpdateResourceType(TEST_TYPE);
		assertTrue(db.hasResourceType(TEST_TYPE.getCanonicalName()));
	}

	@Test
	public void addingSameResourceTypeAgainWorks() throws Exception {
		db.addOrUpdateResourceType(TEST_TYPE);
		assertTrue(db.hasResourceType(TEST_TYPE.getCanonicalName()));
		db.addOrUpdateResourceType(TEST_TYPE);
		assertTrue(db.hasResourceType(TEST_TYPE.getCanonicalName()));
	}

	@Test
	public void addResourceWorks() throws Exception {
		addResourceTypeWorks();
		String resourceName = resname();
		TreeElement el = db.addResource(resourceName, TEST_TYPE, getAppId());
		assertNotNull(el);
		assertEquals(el, db.getToplevelResource(resourceName));
		assertEquals(resourceName, el.getName());
		assertTrue(db.getAllToplevelResources().contains(el));
	}

	@Test
	public void deleteResourceWorks() throws Exception {
		db.addOrUpdateResourceType(OnOffSwitch.class);
		String resourceName = resname();
		TreeElement el = db.addResource(resourceName, OnOffSwitch.class, getAppId());
		assertEquals(el, db.getToplevelResource(resourceName));
		assertTrue(db.getAllToplevelResources().contains(el));
		db.deleteResource(el);
		assertFalse(db.getAllToplevelResources().contains(el));
	}

}
