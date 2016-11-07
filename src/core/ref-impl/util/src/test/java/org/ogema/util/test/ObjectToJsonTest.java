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
package org.ogema.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.sensors.FlowSensor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectToJsonTest extends OsgiAppTestBase {

	private SerializationManager sman;

	@Before
	public void setup() {
		sman = getApplicationManager().getSerializationManager();
	}

	@Test
	public void toJsonWithoutResourceAttribute() {
		TestObjectWithoutResourceAttribute testObj = new TestObjectWithoutResourceAttribute();
		String json = sman.toJson(testObj);

		assertTrue("Invalid JSON string:\n" + json, isJsonValid(json));

		try {
            @SuppressWarnings("deprecation")
			final JsonParser parser = new ObjectMapper().getJsonFactory()
					.createJsonParser(json);
			Class<? extends TestObjectWithoutResourceAttribute> testObjClass = testObj.getClass();
			for (JsonToken token = parser.nextToken(); token != null; token = parser.nextValue()) {
				if(parser.getCurrentName() != null && !parser.getCurrentName().equals("myCollection")) {
					try {
						assertEquals("Value in JSON string is not equals with value of object!",
								testObjClass.getField(parser.getCurrentName()).get(testObj).toString(), parser.getText());
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
			
			// check collection values manually here ...:
			assertTrue(json.contains("myCollection"));
			assertTrue(json.contains("Test 1"));
			assertTrue(json.contains("Test 2"));
			
		} catch (JsonParseException jpe) {
		} catch (IOException ioe) {
		}
	}

	@Test
	@Ignore
	public void toJsonWithResourceAttribute() {
		TestObjectWithResourceAttribute test = new TestObjectWithResourceAttribute();
		String json = sman.toJson(test);

		System.out.println(json);
		assertTrue("Invalid JSON string:\n" + json, isJsonValid(json));
	}

	private boolean isJsonValid(String json) {
		boolean valid = false;
		try {
            @SuppressWarnings("deprecation")
			final JsonParser parser = new ObjectMapper().getJsonFactory().createJsonParser(json);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (JsonParseException jpe) {
		} catch (IOException ioe) {
		}

		return valid;
	}

	private class TestObjectWithoutResourceAttribute {
		// all fields public because we're checking in the test via reflection ...
		public float myFloat = 1.0f;
		public double myDouble = 1.0;
		public int myInt = 1;
		public String myString = "hello world";
		public char myChar = 'a';
		public Collection<Object> myCollection = Arrays.asList(new Object[] { new String("Test 1"),
				new String("Test 2") });

		// getter / setter needed for jackson ...
		public float getMyFloat() {
			return myFloat;
		}

		public void setMyFloat(float myFloat) {
			this.myFloat = myFloat;
		}

		public double getMyDouble() {
			return myDouble;
		}

		public void setMyDouble(double myDouble) {
			this.myDouble = myDouble;
		}

		public int getMyInt() {
			return myInt;
		}

		public void setMyInt(int myInt) {
			this.myInt = myInt;
		}

		public String getMyString() {
			return myString;
		}

		public void setMyString(String myString) {
			this.myString = myString;
		}

		public char getMyChar() {
			return myChar;
		}

		public void setMyChar(char myChar) {
			this.myChar = myChar;
		}

		public Collection<Object> getMyCollection() {
			return myCollection;
		}

		public void setMyCollection(Collection<Object> myCollection) {
			this.myCollection = myCollection;
		}
	}

	private class TestObjectWithResourceAttribute {
		// all fields public because we're checking in the test via reflection ...
		public float myFloat = 1.0f;
		public double myDouble = 1.0;
		public int myInt = 1;
		public String myString = "hello world";
		public char myChar = 'a';
		public Resource myResource;

		public Collection<Object> myCollection = Arrays.asList(new Object[] { new String("Test 1"),
				new String("Test 2") });

		public TestObjectWithResourceAttribute() {
			ResourceManagement resMan = getApplicationManager().getResourceManagement();
			myResource = resMan.createResource("MyTestResource", FlowSensor.class);
		}

		// getter / setter needed for jackson ...
		public float getMyFloat() {
			return myFloat;
		}

		public void setMyFloat(float myFloat) {
			this.myFloat = myFloat;
		}

		public double getMyDouble() {
			return myDouble;
		}

		public void setMyDouble(double myDouble) {
			this.myDouble = myDouble;
		}

		public int getMyInt() {
			return myInt;
		}

		public void setMyInt(int myInt) {
			this.myInt = myInt;
		}

		public String getMyString() {
			return myString;
		}

		public void setMyString(String myString) {
			this.myString = myString;
		}

		public char getMyChar() {
			return myChar;
		}

		public void setMyChar(char myChar) {
			this.myChar = myChar;
		}

		public Collection<Object> getMyCollection() {
			return myCollection;
		}

		public void setMyCollection(Collection<Object> myCollection) {
			this.myCollection = myCollection;
		}

		public Resource getMyResource() {
			return myResource;
		}

		public void setMyResource(Resource myResource) {
			this.myResource = myResource;
		}
	}
}
