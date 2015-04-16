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

import junit.framework.TestCase;

import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;

public class ResourcePermissionTest {

	@Test
	public void testCreatePermConstructor1() {
		ResourcePermission rp = new ResourcePermission("*", "*");
		// TestCase.assertEquals("*", rp.typeName);
		// TestCase.assertEquals(true, rp.recursive);
		TestCase.assertEquals(Integer.MAX_VALUE, rp.count);
		TestCase.assertEquals("*", rp.path);
		TestCase.assertEquals(true, rp.wced);
		TestCase.assertEquals(null, rp.type);
		TestCase.assertEquals(ResourcePermission.ALLACTIONS, rp.actions);
		TestCase.assertEquals(ResourcePermission._ALLACTIONS, rp.actionsAsMask);
	}

	@Test
	public void testCreatePermConstructor2() {
		ResourcePermission rp = new ResourcePermission(
				"type=org.ogema.core.model.simple.FloatResource\n,\fpath=myPath/myTopLevelResource/itsColor, count=1000		",
				ResourcePermission.CREATE);
		TestCase.assertEquals(1000, rp.count);
		TestCase.assertEquals("myPath/myTopLevelResource/itsColor", rp.path);
		TestCase.assertEquals(false, rp.wced);
		TestCase.assertEquals(FloatResource.class.getName(), rp.type);
		TestCase.assertEquals(ResourcePermission.CREATE, rp.actions);
		TestCase.assertEquals(ResourcePermission._CREATE, rp.actionsAsMask);
	}

	@Test
	public void testCreatePermConstructor3() {
		ResourcePermission rp = new ResourcePermission("myPath/myTopLevelResource/itsColor/*", Resource.class, 222);
		// TestCase.assertEquals("org.ogema.core.model.Resource", rp.typeName);
		// TestCase.assertEquals(true, rp.recursive);
		TestCase.assertEquals(222, rp.count);
		TestCase.assertEquals("myPath/myTopLevelResource/itsColor/", rp.path);
		TestCase.assertEquals(true, rp.wced);
		TestCase.assertEquals(Resource.class.getName(), rp.type);
		TestCase.assertEquals(ResourcePermission.CREATE, rp.actions);
		TestCase.assertEquals(ResourcePermission._CREATE, rp.actionsAsMask);
	}
}
