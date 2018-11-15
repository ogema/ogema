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
package org.ogema.accesscontrol;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
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

	@Test(expected = IllegalArgumentException.class)
	public void invalidFilterString() {
		ResourcePermission rp;
		ResourcePermission rp2;

		rp = new ResourcePermission("path=", "*");
		rp2 = new ResourcePermission("path=*", "*");
		rp.implies(rp2);
	}

	@Test
	public void testWildcardedPath() {
		ResourcePermission rp;
		ResourcePermission rp2;

		// 11P
		rp = new ResourcePermission("path=*", "*");
		rp2 = new ResourcePermission("path=*", "*");
		assertTrue(rp.implies(rp2));

		// 12P
		rp = new ResourcePermission("path=*", "*");
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertTrue(rp.implies(rp2));

		// 13P
		rp = new ResourcePermission("path=*", "*");
		rp2 = new ResourcePermission("path=myPath", "*");
		assertTrue(rp.implies(rp2));

		// 21N
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=*", "*");
		assertFalse(rp.implies(rp2));

		// 22P
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertTrue(rp.implies(rp2));

		// 22P
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertTrue(rp.implies(rp2));

		// 22N
		rp = new ResourcePermission("path=myPath/sub2/*", "*");
		rp2 = new ResourcePermission("path=myPath/sub/*", "*");
		assertFalse(rp.implies(rp2));

		// 22N
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath2/sub/*", "*");
		assertFalse(rp.implies(rp2));

		// 23P
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath", "*");
		assertTrue(rp.implies(rp2));

		// 23P
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath/sub/sub", "*");
		assertTrue(rp.implies(rp2));

		// 23N
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath2", "*");
		assertFalse(rp.implies(rp2));

		// 23N
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath2/sub/sub", "*");
		assertFalse(rp.implies(rp2));

		// 31N
		rp = new ResourcePermission("path=myPath", "*");
		rp2 = new ResourcePermission("path=*", "*");
		assertFalse(rp.implies(rp2));

		// 32N
		rp = new ResourcePermission("path=myPath/", "*");
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertFalse(rp.implies(rp2));

		// 33P
		rp = new ResourcePermission("path=myPath/", "*");
		rp2 = new ResourcePermission("path=myPath", "*");
		assertTrue(rp.implies(rp2));

		// 33P
		rp = new ResourcePermission("path=myPath", "*");
		rp2 = new ResourcePermission("path=myPath/", "*");
		assertTrue(rp.implies(rp2));

		// 33N
		rp = new ResourcePermission("path=myPath", "*");
		rp2 = new ResourcePermission("path=myPath/sub", "*");
		assertFalse(rp.implies(rp2));
	}

	@Test
	public void testWildcardedPath3() {
		ResourcePermission rp;
		ResourcePermission rp2;
		// 14P
		rp = new ResourcePermission("path=*", "*");
		rp2 = new ResourcePermission("path=-", "*");
		assertTrue(rp.implies(rp2));

		// 15P
		rp = new ResourcePermission("path=*", "*");
		rp2 = new ResourcePermission("path=myPath/-", "*");
		assertTrue(rp.implies(rp2));

		// 24

		// 25
		rp = new ResourcePermission("path=myPath/*", "*");
		rp2 = new ResourcePermission("path=myPath/sub/-", "*");
		assertTrue(rp.implies(rp2));

		// 34
		rp = new ResourcePermission("path=myPath", "*");
		rp2 = new ResourcePermission("path=-", "*");
		assertFalse(rp.implies(rp2));

		// 35
		rp = new ResourcePermission("path=myPath/", "*");
		rp2 = new ResourcePermission("path=myPath/-", "*");
		assertFalse(rp.implies(rp2));

		// 41
		rp = new ResourcePermission("path=-", "*"); // '-' excepts the parent resource only if it's explicitly specified
													// and it's not root
		rp2 = new ResourcePermission("path=*", "*");
		assertTrue(rp.implies(rp2));

		// 43
		rp = new ResourcePermission("path=-", "*");
		rp2 = new ResourcePermission("path=myPath", "*");
		assertTrue(rp.implies(rp2));

		// 42
		rp = new ResourcePermission("path=-", "*");
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertTrue(rp.implies(rp2));

		// 51
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=*", "*");
		assertFalse(rp.implies(rp2));

		// 52
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertFalse(rp.implies(rp2));

		// 52
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath/sub/*", "*");
		assertTrue(rp.implies(rp2));

		// 53N
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath2", "*");
		assertFalse(rp.implies(rp2));

		// 53
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath", "*");
		assertFalse(rp.implies(rp2));

		// 53
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath/sub/sub", "*");
		assertTrue(rp.implies(rp2));

		// 55
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath/-", "*");
		assertTrue(rp.implies(rp2));

		// 55
		rp = new ResourcePermission("path=myPath/-", "*");
		rp2 = new ResourcePermission("path=myPath/sub/-", "*");
		assertTrue(rp.implies(rp2));
	}

	@Test
	public void testWildcardedPath2() {
		TreeElement te;
		try {
			te = db.addResource("myPath/", ResourceList.class, null);
		} catch (ResourceAlreadyExistsException e) {
			te = db.getToplevelResource("mypath/");
		}
		// 33P
		ResourcePermission rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		ResourcePermission rp2 = new ResourcePermission("path=myPath", "*");
		assertTrue(rp2.implies(rp));

		// 33N
		rp2 = new ResourcePermission("path=myPath2", "*");
		assertFalse(rp2.implies(rp));

		// 23P
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertTrue(rp2.implies(rp));

		// 23N
		rp2 = new ResourcePermission("path=myPath2/*", "*");
		assertFalse(rp2.implies(rp));

		// 23N
		rp2 = new ResourcePermission("path=myPath/sub/*", "*");
		assertFalse(rp2.implies(rp));

		try {
			te = db.addResource("myPath", ResourceList.class, null);
		} catch (ResourceAlreadyExistsException e) {
			te = db.getToplevelResource("mypath");
		}
		// 33N
		rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		rp2 = new ResourcePermission("path=myPath/sub", "*");
		assertFalse(rp2.implies(rp));

		// 33P
		rp2 = new ResourcePermission("path=myPath/", "*");
		assertTrue(rp2.implies(rp));

		// 13P
		rp2 = new ResourcePermission("path=*", "*");
		assertTrue(rp2.implies(rp));

		// 13P
		rp2 = new ResourcePermission("path=/*", "*");
		assertTrue(rp2.implies(rp));

		// 33P
		rp2 = new ResourcePermission("path=myPath", "*");
		assertTrue(rp2.implies(rp));

		try {
			te = te.addChild("sub", StringResource.class, true);
		} catch (ResourceAlreadyExistsException e) {
			te = te.getChild("sub");
		}
		// 33P
		rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		rp2 = new ResourcePermission("path=myPath/sub", "*");
		assertTrue(rp2.implies(rp));

		// 13P
		rp2 = new ResourcePermission("path=*", "*");
		assertTrue(rp2.implies(rp));

		// 33N
		rp2 = new ResourcePermission("path=myPath/", "*");
		assertFalse(rp2.implies(rp));

		// 23P
		rp2 = new ResourcePermission("path=myPath/*", "*");
		assertTrue(rp2.implies(rp));

		// 23N
		rp2 = new ResourcePermission("path=myPath/sub2/*", "*");
		assertFalse(rp2.implies(rp));

		// 23N
		rp2 = new ResourcePermission("path=myPath/sub/sub/*", "*");
		assertFalse(rp2.implies(rp));

		// 23P
		rp2 = new ResourcePermission("path=myPath*", "*");
		assertTrue(rp2.implies(rp));

	}

	@Test
	public void testWildcardedPath4() {
		TreeElement te;
		try {
			te = db.addResource("myPath/", ResourceList.class, null);
		} catch (ResourceAlreadyExistsException e) {
			te = db.getToplevelResource("mypath/");
		}
		// 53N
		ResourcePermission rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		ResourcePermission rp2 = new ResourcePermission("path=myPath/-", "*");
		assertFalse(rp2.implies(rp));

		// 53N
		rp2 = new ResourcePermission("path=myPath-", "*");
		assertFalse(rp2.implies(rp));

		try {
			te = te.addChild("sub", StringResource.class, true);
		} catch (ResourceAlreadyExistsException e) {
			te = te.getChild("sub");
		}
		// 53P
		rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		rp2 = new ResourcePermission("path=myPath/-", "*");
		assertTrue(rp2.implies(rp));

		// 53N
		rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		rp2 = new ResourcePermission("path=myPath/sub/-", "*");
		assertFalse(rp2.implies(rp));

		// 53N
		rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		rp2 = new ResourcePermission("path=myPath/sub-", "*");
		assertFalse(rp2.implies(rp));

		// 43P
		rp2 = new ResourcePermission("path=-", "*");
		assertTrue(rp2.implies(rp));

		// 43N not reachable over real resource instances

		try {
			te = te.addChild("sub", StringResource.class, true);
		} catch (ResourceAlreadyExistsException e) {
			te = te.getChild("sub");
		}
		// 53P
		rp = new ResourcePermission("*", te, Integer.MAX_VALUE);
		rp2 = new ResourcePermission("path=myPath/-", "*");
		assertTrue(rp2.implies(rp));

		// 43P
		rp2 = new ResourcePermission("path=-", "*");
		assertTrue(rp2.implies(rp));

	}
}
