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
package org.ogema.tools.resource.util.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resource.util.ValueResourceUtils;

public class ValueTest extends OsgiAppTestBase {

	private ResourceManagement rm;
//	private ResourceAccess ra;
	private StringResource stringRes;
	private FloatResource floatRes;
	private BooleanResource booleanRes;
	private IntegerResource intRes;
	private TimeResource timeRes;

	public ValueTest() {
		super(true);
	}

	@Before
	public void setup() {
		rm = getApplicationManager().getResourceManagement();
//		ra = getApplicationManager().getResourceAccess();
		stringRes = rm.createResource("stringRes", StringResource.class);
		floatRes = rm.createResource("floatRes", FloatResource.class);
		booleanRes = rm.createResource("booleanRes", BooleanResource.class);
		intRes = rm.createResource("intRes", IntegerResource.class);
		timeRes = rm.createResource("timeRes", TimeResource.class);
	}

	@After
	public void cleanUp() {
		stringRes.delete();
		floatRes.delete();
		booleanRes.delete();
		intRes.delete();
		timeRes.delete();
	}

	@Test
	public void setAndGetValueTest() {
		String str = "23";
		ValueResourceUtils.setValue(intRes, str);
		String msg = "Unexpected value";
		assertEquals(msg, 23, intRes.getValue());
		assertEquals(str, ValueResourceUtils.getValue(intRes));
		str = "23.34";
		ValueResourceUtils.setValue(floatRes, str);
		assertEquals(msg, 23.34, floatRes.getValue(), 0.01);
		assertEquals(str, ValueResourceUtils.getValue(floatRes));
		str = "32142324331";
		ValueResourceUtils.setValue(timeRes, str);
		assertEquals(Long.parseLong(str), timeRes.getValue());
		assertEquals(str, ValueResourceUtils.getValue(timeRes));
	}

}
