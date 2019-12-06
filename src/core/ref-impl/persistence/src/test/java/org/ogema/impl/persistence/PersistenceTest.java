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
package org.ogema.impl.persistence;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.impl.persistence.testmodels.ProgramPowerCurve;
import org.ogema.impl.persistence.testmodels.RelativeTimeRow;
import org.ogema.impl.persistence.testmodels.TestPhysicalDevice;
import org.ogema.model.communication.CommunicationInformation;
import org.ogema.model.communication.CommunicationStatus;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.persistence.DBConstants;
import org.ogema.resourcetree.TreeElement;
import org.ops4j.pax.exam.MavenUtils;

import junit.framework.TestCase;

public class PersistenceTest extends DBBasicTest {

	String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");

	Object[][] params = new Object[][] { { 1, "step1" }, { 2, "step2" }, { 3, "step3" }, { 4, "step4" },
			{ 5, "step5" } };
	private static int executionOrder = 0;
	private String name;

	// public PersistenceTest(int count, String name) {
	// this.executionOrder = count;
	// this.name = name;
	// }

	@BeforeClass
	public static void init() {
		System.setProperty("org.ogema.persistence", "active");
	}

	// Just an example appID, which has to be unique and probably will have the
	// format of a file path.
	private String testAppID = "/persistence/target/persistence-2.0-SNAPSHOT.jar";

	@Before
	public void before() throws InterruptedException {

		System.setProperty(DBConstants.DB_PATH_PROP,
				"persistenceTest" + params[executionOrder][0] + params[executionOrder][1]);
		executionOrder++;
		db = new ResourceDBImpl();
		db.setName("PersistenceTest");
		db.resourceIO.reset();
		db.restart();
	}

	@After
	public void after() throws InterruptedException {

	}

	@Test
	public void deleteRecreateStartUnclean() {
		System.out.println("Test: deleteRecreateStartUnclean, Executionorder: " + executionOrder);
		TreeElement fridge = (TreeElementImpl) db.getToplevelResource("a");
		if (fridge == null)
			fridge = db.addResource("a", CoolingDevice.class, testAppID);

		TreeElement res = fridge.getChild("temperatureSensor");
		if (res == null)
			res = fridge.addChild("temperatureSensor", TemperatureSensor.class, false);
		TreeElement reading = res.getChild("reading");
		if (reading == null)
			reading = res.addChild("reading", TemperatureResource.class, false);
		db.deleteResource(reading);
		reading = res.addChild("reading", TemperatureResource.class, false);
		restartAndCompareDynamicData();
	}

	@Test
	public void setResourceListElementType() {
		System.out.println("Test: setResourceListElementType, Executionorder: " + executionOrder);
		TreeElementImpl list = (TreeElementImpl) db.getToplevelResource("b");
		if (list == null)
			list = (TreeElementImpl) db.addResource("b", ResourceList.class, testAppID);
		if (!list.isActive()) {
			list.setResourceListType(PhysicalElement.class);
			list.setActive(false);
		}
		restartAndCompareDynamicData();
	}

	@Test
	public void noResourceLossAfterRestart1() {
		System.out.println("Test: noResourceLossAfterRestart1, Executionorder: " + executionOrder);
		String testID = "persistenceTest";// this.toString();
		System.out.println(testID + executionOrder);
		boolean exception = false;
		String topLevelName = testID + "@resource_PhysicalElement_name";

		try {
			db.addOrUpdateResourceType(org.ogema.model.prototypes.PhysicalElement.class);
			db.addOrUpdateResourceType(org.ogema.impl.persistence.testmodels.TestPhysicalDevice.class);
		} catch (Exception e) {
			TestCase.assertTrue(true);
			e.printStackTrace();
		}

		// create two different top level resources after the type
		// definitions
		TreeElementImpl resource_PhysicalElement_id_0 = null; // are added
		// which
		// has to
		// succeed.
		try {
			resource_PhysicalElement_id_0 = (TreeElementImpl) db.addResource(topLevelName,
					org.ogema.model.prototypes.PhysicalElement.class, testAppID);
			TestCase.assertTrue(db.hasResource(topLevelName));
		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}

		TestCase.assertFalse(exception);
		exception = false;

		// add a child to the top level resource ...
		TreeElement comminfo = resource_PhysicalElement_id_0.addChild("comInfo", CommunicationInformation.class, true);
		// ... and some simple resources to the subresource
		TreeElement commStatus = comminfo.addChild("communicationStatus", CommunicationStatus.class, false);
		// another NonPersistent child
		//TreeElementImpl quality = (TreeElementImpl) commStatus.addChild("quality", FloatResource.class, false);
		TreeElementImpl communicationAddress = (TreeElementImpl) comminfo.addChild("communicationAddress",
				StringResource.class, false);
		TreeElementImpl lastTimeReceive = (TreeElementImpl) comminfo.addChild("lastTimeReceive", TimeResource.class,
				false);
		comminfo.addChild("lastTimeSend", TimeResource.class, false);

		// set some simple values of the sub resources above
		//quality.simpleValue.setFloat(123.567f);
		// radioQuality.simpleValue.setFloat(987.456f);
		// communicationStatus.simpleValue.setInt(123456789);
		// communicationNotInitialized.simpleValue.setBoolean(true);
		communicationAddress.simpleValue.setString("192.168.0.111:8080");
		lastTimeReceive.simpleValue.setLong(123456789987654321L);

		TreeElementImpl resource_TestPhysicalDevice_id_0 = null;
		try {
			resource_TestPhysicalDevice_id_0 = (TreeElementImpl) db.addResource("resource_TestPhysicalDevice_name",
					TestPhysicalDevice.class, testAppID);
			TestCase.assertTrue(db.hasResource("resource_TestPhysicalDevice_name"));
		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}
		TestCase.assertFalse(exception);
		exception = false;

		// two new resources with different names but the same type as the
		// existing resources
		try {
			resource_PhysicalElement_id_0 = null;
			resource_PhysicalElement_id_0 = (TreeElementImpl) db.addResource("resource_PhysicalElement_name_0",
					org.ogema.model.prototypes.PhysicalElement.class, testAppID);
			TestCase.assertFalse(resource_PhysicalElement_id_0 == null);
			TestCase.assertTrue(db.hasResource("resource_PhysicalElement_name_0"));

		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}
		TestCase.assertFalse(exception);
		exception = false;

		try {
			resource_TestPhysicalDevice_id_0 = null;
			resource_TestPhysicalDevice_id_0 = (TreeElementImpl) db.addResource("resource_TestPhysicalDevice_name_0",
					TestPhysicalDevice.class, testAppID);
			TestCase.assertTrue(resource_TestPhysicalDevice_id_0 != null);
			TestCase.assertTrue(db.hasResource("resource_TestPhysicalDevice_name_0"));
		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}
		TestCase.assertFalse(exception);
		exception = false;
		// Add an array resource as decorator
		resource_TestPhysicalDevice_id_0.addChild("stringArray", StringArrayResource.class, true);
		String strarr[] = { "hello", "ogema", "world" };
		resource_TestPhysicalDevice_id_0.getChild("stringArray").getData().setStringArr(strarr);

		restartAndCompareDynamicData();
	}

	@Test
	public void noResourceLossAfterRestart2() {
		System.out.println("Test: noResourceLossAfterRestart2, Executionorder: " + executionOrder);
		boolean exception = false;
		TreeElementImpl top3 = null;
		String top3Name = "simpleBooleanAsTopLevel";
		try {
			top3 = (TreeElementImpl) db.addResource(top3Name, BooleanResource.class, testAppID);
			TestCase.assertTrue(db.hasResource(top3Name));
		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}

		TestCase.assertFalse(exception);
		exception = false;

		// Add an array resource as decorator
		TreeElementImpl deco = (TreeElementImpl) top3.addChild("longArrayDecorator", TimeArrayResource.class, true);
		long longArr[] = new long[64 << 2];
		Arrays.fill(longArr, 0xDEADBEEFDEADB00BL);
		top3.getChild("longArrayDecorator").getData().setLongArr(longArr);
		db.deleteResource(deco);
		restartAndCompareDynamicData();
	}

	@Test
	public void noResourceLossAfterRestart3() {
		System.out.println("Test: noResourceLossAfterRestart3, Executionorder: %s" + executionOrder);
		boolean exception = false;

		try {
			db.addOrUpdateResourceType(ProgramPowerCurve.class);
		} catch (Exception e) {
			TestCase.assertTrue(true);
			e.printStackTrace();
		}

		// create two different top level resources after the type
		// definitions
		TreeElementImpl resource_PhysicalElement_id_0 = null; // are added
		// which
		// has to
		// succeed.
		try {
			resource_PhysicalElement_id_0 = (TreeElementImpl) db.addResource(name + "_01", ProgramPowerCurve.class,
					testAppID);
			TestCase.assertTrue(db.hasResource(name + "_01"));
		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}

		TestCase.assertFalse(exception);
		exception = false;

		// add a child to the top level resource ...
		TreeElementImpl estimation = (TreeElementImpl) resource_PhysicalElement_id_0.addChild("estimation",
				RelativeTimeRow.class, false);
		// ... and some simple resources to the subresource
		// note this is NonPersistent
		TreeElementImpl timeStamps = (TreeElementImpl) estimation.addChild("timeStamps", TimeArrayResource.class,
				false);
		// TreeElementImpl radioQuality = (TreeElementImpl)
		// comminfo.addChild("radioQuality", FloatResource.class,
		// false);
		// another NonPersistent child
		TreeElementImpl values = (TreeElementImpl) estimation.addChild("values", FloatArrayResource.class, false);
		TreeElementImpl maxDuration = (TreeElementImpl) resource_PhysicalElement_id_0.addChild("maxDuration",
				TimeResource.class, false);

		// Set some simple values of the sub resources above.
		// Following action has to fail because of setting of a single float
		// value on a TimeArrayResource
		try {
			timeStamps.simpleValue.setFloat(123.567f);
		} catch (UnsupportedOperationException e1) {
			exception = true;
		}
		TestCase.assertTrue(exception);

		// Setting of a float array as value of TimeArrayResource has to
		// fail too.
		exception = false;
		float f[] = new float[64 + 64 >> 2];
		Arrays.fill(f, 10.5432f);
		try {
			timeStamps.simpleValue.setFloatArr(f);
		} catch (Throwable e1) {
			exception = true;
		}
		TestCase.assertTrue(exception);

		// Setting of the right type has to succeed.
		exception = false;
		long l[] = new long[64 + 64 >> 2];
		Arrays.fill(l, 0x1234567890ABCDFEL);
		try {
			timeStamps.simpleValue.setLongArr(l);
		} catch (Throwable e1) {
			exception = true;
		}
		TestCase.assertFalse(exception);

		float f2[] = new float[64 >> 2];
		Arrays.fill(f2, -145.987f);
		try {
			values.simpleValue.setFloatArr(f2);
		} catch (Throwable e1) {
			exception = true;
		}
		TestCase.assertFalse(exception);

		maxDuration.simpleValue.setLong(0xFEEDB00BDEADBEEFL);

		resource_PhysicalElement_id_0.addChild("stringArray", StringArrayResource.class, true);
		String strarr[] = { "program", "power", "curve" };
		resource_PhysicalElement_id_0.getChild("stringArray").getData().setStringArr(strarr);

		estimation.addChild("stringArray", org.ogema.model.actors.MultiSwitch.class, true);

		timeStamps.addChild("stringArray", StringArrayResource.class, true);
		String strarr2[] = { "program", "power", "curve", "estimation", "timeStamps" };
		timeStamps.getChild("stringArray").getData().setStringArr(strarr2);

		values.addChild("stringArray", StringArrayResource.class, true);
		String strarr3[] = { "program", "power", "curve", "estimation", "timeStamps" };
		values.getChild("stringArray").getData().setStringArr(strarr3);

		TreeElementImpl top4 = null;
		String top4Name = name + "_simpleBooleanAsTopLevel";
		try {
			top4 = (TreeElementImpl) db.addResource(top4Name, BooleanResource.class, testAppID);
			TestCase.assertTrue(db.hasResource(top4Name));
		} catch (Exception e) {
			exception = true;
			e.printStackTrace();
		}

		TestCase.assertFalse(exception);
		exception = false;

		// Add an array resource as decorator
		top4.addChild("booleanDecorator", TimeArrayResource.class, true);
		long longArr[] = new long[64 << 2];
		Arrays.fill(longArr, 0xDEADBEEFDEADB00BL);
		top4.getChild("booleanDecorator").getData().setLongArr(longArr);

		db.deleteResource(top4);
		// db.deleteResource(timeStamps);
		db.deleteResource(values);
		// try {
		restartAndCompareDynamicData();
		// } catch (Throwable e) {
		// e.printStackTrace();
		// }
	}

}
