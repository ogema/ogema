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

import org.junit.Test;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.resourcemanager.ResourceException;

import static org.junit.Assert.*;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.communication.KNXAddress;
import org.ogema.resourcemanager.impl.test.types.AllSimpleTypes;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Test get and set on the different primitive array resource types (int, float, long (=time), boolean, String)
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ArrayResourceTest extends OsgiTestBase {
	
	@Test
	public void settingByteArrayWorks() throws ResourceException {
		ByteArrayResource bar = resMan.createResource(newResourceName(), ByteArrayResource.class);
		bar.setValues("test".getBytes());
		assertEquals(ByteArrayResource.class, bar.getResourceType());
		assertEquals("test", new String(bar.getValues()));
	}

	@Test
	public void settingFloatArrayWorks() throws ResourceException {
		MultiSwitch swtch = resMan.createResource(newResourceName(), MultiSwitch.class);
		FloatArrayResource ar = swtch.electricPowerFunction().values().create();
		float[] newVal = { 0, 1, 1, 2, 3, 5, 8, 13 };
		ar.setValues(Arrays.copyOf(newVal, newVal.length));

		// test getValue()
		assertTrue(Arrays.equals(newVal, ar.getValues()));

		// test getElementValue
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(i), 0.f);
		}

		// test setElementValue
		for (int i = 0; i < newVal.length; i++) {
			int idx = newVal.length - 1 - i;
			ar.setElementValue(newVal[idx], i);
		}
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(newVal.length - 1 - i), 0.f);
		}
	}

	@Test
	public void settingIntegerArrayWorks() throws ResourceException {
		KNXAddress res = resMan.createResource(newResourceName(), KNXAddress.class);
		res.groupAddress().create();
		IntegerArrayResource ar = res.groupAddress();
		int[] newVal = { 0, 1, 1, 2, 3, 5, 8, 13 };
		ar.setValues(Arrays.copyOf(newVal, newVal.length));
		assertTrue(Arrays.equals(newVal, ar.getValues()));
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(i));
		}
		for (int i = 0; i < newVal.length; i++) {
			int idx = newVal.length - 1 - i;
			ar.setElementValue(newVal[idx], i);
		}
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(newVal.length - 1 - i));
		}
	}

	@Test
	public void settingStringArrayWorks() throws ResourceException {
		StringArrayResource ar = resMan.createResource(newResourceName(), StringArrayResource.class);
		String[] newVal = { "0", "1", "1", "2", "3", "5", "8", "13" };
		ar.setValues(Arrays.copyOf(newVal, newVal.length));
		assertTrue(Arrays.equals(newVal, ar.getValues()));
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(i));
		}
		for (int i = 0; i < newVal.length; i++) {
			int idx = newVal.length - 1 - i;
			ar.setElementValue(newVal[idx], i);
		}
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(newVal.length - 1 - i));
		}
	}

	@Test
	public void settingTimeArrayWorks() throws ResourceException {
		////		d.addOptionalElement("timeStamps");
		TimeArrayResource ar = resMan.createResource(newResourceName(), TimeArrayResource.class);
		//                        d.keys();
		long[] newVal = { 0, 1, 1, 2, 3, 5, 8, 13 };
		ar.setValues(Arrays.copyOf(newVal, newVal.length));
		assertTrue(Arrays.equals(newVal, ar.getValues()));
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(i));
		}
		for (int i = 0; i < newVal.length; i++) {
			int idx = newVal.length - 1 - i;
			ar.setElementValue(newVal[idx], i);
		}
		for (int i = 0; i < newVal.length; i++) {
			assertEquals(newVal[i], ar.getElementValue(newVal.length - 1 - i));
		}
	}

	@Test
	public void settingBooleanArrayWorks() throws Exception {
		AllSimpleTypes testRes = resMan.createResource(newResourceName(), AllSimpleTypes.class);
		BooleanArrayResource a = (BooleanArrayResource) testRes.booleanArray().create();
		boolean[] val = { true, false, true };
		a.setValues(Arrays.copyOf(val, val.length));

		assertTrue(a.getElementValue(0));
		assertFalse(a.getElementValue(1));
		assertTrue(a.getElementValue(2));

		a.setElementValue(false, 2);

		assertTrue(a.getElementValue(0));
		assertFalse(a.getElementValue(1));
		assertFalse(a.getElementValue(2));
	}

}
