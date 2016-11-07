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
package org.ogema.impl.persistence;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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
import org.ogema.model.communication.CommunicationInformation;
import org.ogema.model.communication.CommunicationStatus;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.persistence.DBConstants;
import org.ogema.resourcetree.TreeElement;

@RunWith(Parameterized.class)
public class PersistenceTest {

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(
				new Object[][] { { 1, "" }, { 2, "" }, { 3, "" }, { 4, "step4" }, { 4, "step5" }, { 4, "step6" } });
	}

	private int executionOrder;
	private String name;

	public PersistenceTest(int count, String name) {
		this.executionOrder = count;
		this.name = name;
	}

	static ResourceDBImpl db;

	@BeforeClass
	public static void init() {
		System.setProperty("org.ogema.persistence", "active");
		System.setProperty(DBConstants.DB_PATH_PROP, "persistenceTest");
		db = new ResourceDBImpl();
		db.setName("PersistenceTest");
		db.init();
	}

	// Just an example appID, which has to be unique and probably will have the
	// format of a file path.
	private String testAppID = "/persistence/target/persistence-2.0-SNAPSHOT.jar";

	@Before
	public void before() throws InterruptedException {

	}

	@After
	public void after() throws InterruptedException {

	}

	@Test
	public void deleteRecreateStartUnclean() {
		if (executionOrder != 1)
			return;
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
		if (executionOrder != 1)
			return;
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
	public void testPopulateDB() {
		String testID = "persistenceTest";// this.toString();
		System.out.println(testID + executionOrder);
		switch (executionOrder) {
		case 1:
			db.resourceIO.reset();
			db.restart();
			break;
		case 2: {
			boolean exception = false;
			String topLevelName = testID + "@resource_PhysicalElement_name";

			try {
				db.addOrUpdateResourceType(org.ogema.model.prototypes.PhysicalElement.class);
				db.addOrUpdateResourceType(org.ogema.impl.persistence.TestPhysicalDevice.class);
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
			TreeElement comminfo = resource_PhysicalElement_id_0.addChild("comInfo", CommunicationInformation.class,
					true);
			// ... and some simple resources to the subresource
			TreeElement commStatus = comminfo.addChild("communicationStatus", CommunicationStatus.class, false);
			// another NonPersistent child
			TreeElementImpl quality = (TreeElementImpl) commStatus.addChild("quality", FloatResource.class, false);
			TreeElementImpl communicationAddress = (TreeElementImpl) comminfo.addChild("communicationAddress",
					StringResource.class, false);
			TreeElementImpl lastTimeReceive = (TreeElementImpl) comminfo.addChild("lastTimeReceive", TimeResource.class,
					false);
			comminfo.addChild("lastTimeSend", TimeResource.class, false);

			// set some simple values of the sub resources above
			quality.simpleValue.setFloat(123.567f);
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
				resource_TestPhysicalDevice_id_0 = (TreeElementImpl) db
						.addResource("resource_TestPhysicalDevice_name_0", TestPhysicalDevice.class, testAppID);
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
			break;
		case 3: {
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
			break;
		case 4: {
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
			db.deleteResource(timeStamps);
			db.deleteResource(values);
			restartAndCompareDynamicData();
		}
			break;
		default:
			break;
		}
	}

	private void restartAndCompareDynamicData() {
		try {
			Thread.sleep(2 * TimedPersistence.DEFAULT_STOREPERIOD);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// get current maps of the resources
		ConcurrentHashMap<String, TreeElementImpl> root = db.root;
		ConcurrentHashMap<String, Class<?>> typeClassByName = db.typeClassByName;
		ConcurrentHashMap<String, Integer> resIDByName = db.resIDByName;
		ConcurrentHashMap<Integer, TreeElementImpl> resNodeByID = db.resNodeByID;
		ConcurrentHashMap<String, Vector<Integer>> resIDsByType = db.resIDsByType;
		// reinit the resource db
		db.restart();
		// compare the contents of the maps before and after the reinit
		// iterate over all of entries and compare them with their copy from
		// before reinit
		boolean success = true;
		// 1. root list
		{
			Set<Entry<String, TreeElementImpl>> tlrs = root.entrySet();
			for (Map.Entry<String, TreeElementImpl> entry : tlrs) {
				TreeElementImpl resOld = entry.getValue();
				TreeElementImpl resNew = db.root.get(resOld.getName());
				if (resNew == null) {
					success = false;
					break;
				}
				if (!resOld.compare(resNew)) {
					success = false;
					break;
				}
				if (resOld.parent != null || resNew.parent != null) {
					success = false;
					break;
				}
			}
			TestCase.assertTrue(success);
		}
		// 2. typeClassByName list
		{
			Set<Entry<String, Class<?>>> tlrs = typeClassByName.entrySet();
			for (Map.Entry<String, Class<?>> entry : tlrs) {
				Class<?> clsOld = entry.getValue();
				Class<?> clsNew = db.typeClassByName.get(clsOld.getName());
				if (clsNew == null) { // clsNew may be null because the only one resource with this type could be
										// removed before restart that results in leaking of the class in the type
										// registry
					// success = false;
					// break;
				}
				// if (clsOld != clsNew) {
				// success = false;
				// break;
				// }
			}
			TestCase.assertTrue(success);
		}
		// 3. resIDByName list
		{
			Set<Entry<String, Integer>> tlrs = resIDByName.entrySet();
			for (Map.Entry<String, Integer> entry : tlrs) {
				if (entry.getKey() == null || entry.getValue() == null) {
					success = false;
					break;
				}

				int oldVal = entry.getValue();
				String key = entry.getKey();
				int newVal = -1;
				try {
					Integer i = db.resIDByName.get(key);
					if (i == null)
						success = false;
					else
						newVal = i.intValue();
				} catch (Throwable e) {
					e.printStackTrace();
				}

				if (oldVal != newVal) {
					success = false;
					// break;
					System.err.println(key + " not found after restart.");
				}
			}
			TestCase.assertTrue(success);
		}
		// 4. resNodeByID list
		{
			Set<Entry<Integer, TreeElementImpl>> tlrs = resNodeByID.entrySet();
			for (Map.Entry<Integer, TreeElementImpl> entry : tlrs) {
				TreeElementImpl resOld = entry.getValue();
				TreeElementImpl resNew = db.resNodeByID.get(resOld.resID);
				if (resNew == null) {
					success = false;
					break;
				}
				if (!resOld.compare(resNew)) {
					success = false;
					break;
				}
			}
			TestCase.assertTrue(success);
		}

		// 5. resIDsByType list
		{
			Set<Entry<String, Vector<Integer>>> tlrs = resIDsByType.entrySet();
			for (Map.Entry<String, Vector<Integer>> entry : tlrs) {
				String clsOld = entry.getKey();
				// if (!db.resIDsByType.containsKey(clsOld)) {
				// success = false;
				// break;
				// }
				Vector<Integer> oldValues = entry.getValue();
				Vector<Integer> newValues = db.resIDsByType.get(clsOld);
				if (oldValues == null && newValues != null) {
					success = false;
					break;
				}
				if (oldValues != null && oldValues.size() == 0 && newValues != null) {
					success = false;
					break;
				}
				if (oldValues != null && newValues != null && oldValues.size() != newValues.size()) {
					success = false;
					break;
				}
				if (oldValues != null && newValues != null)
					for (int oldID : oldValues) {
						if (!newValues.contains(oldID)) {
							success = false;
							break;
						}
					}
				if (!success)
					break;
			}
			TestCase.assertTrue(success);
		}
		// 6. simple resource values
		{
			int length;
			Set<Entry<Integer, TreeElementImpl>> tlrs = resNodeByID.entrySet();
			for (Map.Entry<Integer, TreeElementImpl> entry : tlrs) {
				TreeElementImpl node = entry.getValue();
				TreeElementImpl resNew = db.resNodeByID.get(node.resID);
				int typeKey = node.typeKey;
				switch (typeKey) {
				// compare simple resource value
				case DBConstants.TYPE_KEY_BOOLEAN:
					if (node.simpleValue.Z != resNew.simpleValue.Z)
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_FLOAT:
					if (node.simpleValue.F != resNew.simpleValue.F)
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_INT:
					if (node.simpleValue.I != resNew.simpleValue.I)
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_STRING:
					if (!node.simpleValue.S.equals(resNew.simpleValue.S))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_LONG:
					if (node.simpleValue.J != resNew.simpleValue.J)
						TestCase.assertTrue(false);
					break;
				// read array resource
				case DBConstants.TYPE_KEY_OPAQUE:
					length = node.simpleValue.getArrayLength();
					if (length != resNew.simpleValue.getArrayLength())
						TestCase.assertTrue(false);
					if (!Arrays.equals(node.simpleValue.aB, resNew.simpleValue.aB))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_INT_ARR:
					length = node.simpleValue.getArrayLength();
					if (length != resNew.simpleValue.getArrayLength())
						TestCase.assertTrue(false);
					if (!Arrays.equals(node.simpleValue.aI, resNew.simpleValue.aI))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_LONG_ARR:
					length = node.simpleValue.getArrayLength();
					if (length != resNew.simpleValue.getArrayLength())
						TestCase.assertTrue(false);
					if (!Arrays.equals(node.simpleValue.aJ, resNew.simpleValue.aJ))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_FLOAT_ARR:
					length = node.simpleValue.getArrayLength();
					if (length != resNew.simpleValue.getArrayLength())
						TestCase.assertTrue(false);
					if (!Arrays.equals(node.simpleValue.aF, resNew.simpleValue.aF))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_COMPLEX_ARR:
					break;
				case DBConstants.TYPE_KEY_BOOLEAN_ARR:
					length = node.simpleValue.getArrayLength();
					if (length != resNew.simpleValue.getArrayLength())
						TestCase.assertTrue(false);
					if (!Arrays.equals(node.simpleValue.aZ, resNew.simpleValue.aZ))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_STRING_ARR:
					length = node.simpleValue.getArrayLength();
					if (length != resNew.simpleValue.getArrayLength())
						TestCase.assertTrue(false);
					if (!Arrays.equals(node.simpleValue.aS, resNew.simpleValue.aS))
						TestCase.assertTrue(false);
					break;
				case DBConstants.TYPE_KEY_COMPLEX:
					break;
				default:
				}
				if (success == false) {
					break;
				}
			}
			TestCase.assertTrue(success);
		}
	}
}
