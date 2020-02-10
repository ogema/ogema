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
package org.ogema.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
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

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
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
