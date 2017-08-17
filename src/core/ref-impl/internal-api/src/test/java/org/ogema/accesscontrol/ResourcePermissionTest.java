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
package org.ogema.accesscontrol;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.persistence.ResourceDB;
import org.ogema.resourcetree.TreeElement;

public class ResourcePermissionTest extends OsgiAppTestBase {

	@Inject
	ResourceDB db;

	@Test
	public void testCreatePermConstructor1() {
		ResourcePermission rp = new ResourcePermission("*", "*");
		// TestCase.assertEquals("*", rp.typeName);
		// TestCase.assertEquals(true, rp.recursive);
		assertEquals(Integer.MAX_VALUE, rp.getCount());
		assertEquals("*", rp.getPath());
		assertEquals(true, rp.isWced());
		assertEquals(null, rp.getType());
		assertEquals(ResourcePermission.ALLACTIONS, rp.getActions());
		assertEquals(ResourcePermission._ALLACTIONS, rp.getActionsAsMask());
	}

	@Test
	public void testCreatePermConstructor2() {
		ResourcePermission rp = new ResourcePermission(
				"type=org.ogema.core.model.simple.FloatResource\n,\fpath=myPath/myTopLevelResource/itsColor, count=1000		",
				ResourcePermission.CREATE);
		assertEquals(1000, rp.getCount());
		assertEquals("myPath/myTopLevelResource/itsColor", rp.getPath());
		assertEquals(false, rp.isWced());
		assertEquals(FloatResource.class.getName(), rp.getType());
		assertEquals(ResourcePermission.CREATE, rp.getActions());
		assertEquals(ResourcePermission._CREATE, rp.getActionsAsMask());
	}

	@Test
	public void testCreatePermConstructor3() {
		ResourcePermission rp = new ResourcePermission("myPath/myTopLevelResource/itsColor/*", Resource.class, 222);
		// TestCase.assertEquals("org.ogema.core.model.Resource", rp.typeName);
		// TestCase.assertEquals(true, rp.recursive);
		assertEquals(222, rp.getCount());
		assertEquals("myPath/myTopLevelResource/itsColor/", rp.getPath());
		assertEquals(true, rp.isWced());
		assertEquals(Resource.class.getName(), rp.getType());
		assertEquals(ResourcePermission.CREATE, rp.getActions());
		assertEquals(ResourcePermission._CREATE, rp.getActionsAsMask());
	}

	@Test
	public void resourceListTypeImply() {
		TreeElement te = db.addResource("TestResourceList", ResourceList.class, null);
		ResourcePermission rpTE = new ResourcePermission("*", te, Integer.MAX_VALUE);
		ResourcePermission rp = new ResourcePermission("type=org.ogema.core.model.simple.StringResource", "*");

		boolean implies = rp.implies(rpTE);
		assertTrue(implies); // An empty unspecified ResourceList can be implied by any other ResourcePermission,
								// whereas ResourceList permission implies ResourceList ResourcePermission only.
		
		te.setResourceListType(FloatResource.class);
		rpTE = new ResourcePermission("*", te, Integer.MAX_VALUE);
		implies = rp.implies(rpTE);
		assertFalse(implies);

		te = db.addResource("TestResourceList2", ResourceList.class, null);		
		te.setResourceListType(StringResource.class);
		rpTE = new ResourcePermission("*", te, Integer.MAX_VALUE);
		implies = rp.implies(rpTE);
		assertTrue(implies);
	}
}
