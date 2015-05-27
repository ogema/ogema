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

import org.ogema.impl.persistence.testmodels.TempStorageSwitchCapacity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.OpaqueResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.core.model.units.VolumeResource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.model.locations.Building;
import org.ogema.model.locations.BuildingPropertyUnit;
import org.ogema.model.locations.Room;
import org.ogema.model.actors.MultiSwitch;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.devices.whitegoods.FreezeCombi;
import org.ogema.persistence.DBConstants;
import org.ogema.resourcetree.TreeElement;

/**
 * abstract: the methods of this class test the functionality of interface ResourceDB test-id:
 * 
 * @author Joerg Brandstetter (bjg), Fraunhofer IIS
 */
@RunWith(Parameterized.class)
public class TreeElementTest extends DBBasicTest {

	/*
	 * Names of the elements of a FreezeCombi - used in the tests.
	 */
	private static final String name_tempSensCool = "temperatureSensorCooling";
	private static final String name_tempSensFreeze = "temperatureSensorFreezing";
	private static final String name_swtch = "onOffSwitch";
	private static final String name_elConn = "electricityConnection";
	private static final String name_ratedVolumeCool = "volumeCooling";
	private static final String name_ratedVolumeFreeze = "volumeFreezing";

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][] { { 1 } /* , { 2 }, { 3 }, { 4 } */});
	}

	public TreeElementTest(int count) {
	}

	@BeforeClass
	public static void init() {
		System.setProperty("org.ogema.persistence", "inactive");
		System.setProperty(DBConstants.DB_PATH_PROP, "treeElementTest");
		db = new ResourceDBImpl();
		db.init();
	}

	@AfterClass
	public static void wait4Storage() {
		try {
			Thread.sleep(3 * TimedPersistence.DEFAULT_STOREPERIOD);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void before() throws InterruptedException {

	}

	@After
	public void after() throws InterruptedException {

	}

	// resource type
	Class<? extends Resource> type_PhysicalElement_id = null;
	Class<? extends Resource> type_TestPhysicalDevice_id = null;

	final String type_PhysicalElement_name = "org.ogema.model.prototypes.PhysicalElement";
	final String type_TestPhysicalDevice_name = "org.ogema.impl.persistence.TestPhysicalDevice";

	final String testAppID = "MyTestApp";

	// resource
	TreeElementImpl resource_TestPhysicalDevice_id_0 = null;
	TreeElementImpl sub_resource_TestSensor_id_0 = null;
	TreeElementImpl sub_resource_TestActor_id_0 = null;

	TreeElementImpl resource_PhysicalElement_id_0 = null;
	TreeElementImpl sub_resource_RoomInformation_id_0 = null;
	TreeElementImpl sub_resource_FirmwareInfo_id_0 = null;
	TreeElementImpl sub_resource_PhysicalDimensions_id_0 = null;

	/**
	 * abstract: test function addOrUpdateResourceType(Class<? extends Resource> type) test-id: scope: single tested
	 * functions of ResourceDB interface:
	 * 
	 * addOrUpdateResourceType(Class<? extends Resource> type)
	 */
	@Test
	public void addOrUpdateResourceType() {
		System.out.println("Inside testaddOrUpdateResourceType");
		boolean exception_thrown = false;
		checkDynamicData();
		// valid return id
		try {
			type_PhysicalElement_id = db.addOrUpdateResourceType(PhysicalElement.class);
		} catch (Exception e) {
			exception_thrown = true;
		}
		TestCase.assertFalse("Class PhysicalRlrmrnt is null.", type_PhysicalElement_id == null);
		TestCase.assertFalse(exception_thrown);

		// If the Type is already registered it will be updated. No Exception is
		// expected.
		exception_thrown = false;
		try {
			db.addOrUpdateResourceType(PhysicalElement.class);
		} catch (Exception e) {
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);

		// InvalidResourceTypeException ??? does not compile with a class not
		// extending Resource, how to test
		exception_thrown = false;

		try {
			db.addOrUpdateResourceType(Sensor.class);
		} catch (Exception e) {
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);
		checkDynamicData();
	}

	/**
	 * abstract: test the deleting of a resource and a resource type test-id: scope: group
	 * 
	 * tested functions of ResourceDB interface: List<Integer> deleteResource(int id) deleteResourceType(String name)
	 * 
	 * tested additional functions of ResourceDB interface: boolean hasResourceType(String name) boolean
	 * hasResource(String name);
	 */
	public void deleteTopLevelResource() {

		System.out.println("Inside deleteTopLevelResource");
		int id = resource_PhysicalElement_id_0.resID;
		String name = resource_PhysicalElement_id_0.path;
		String cls = resource_PhysicalElement_id_0.type.getName();
		TestCase.assertTrue(db.hasResource("resource_PhysicalElement_name_0"));
		// delete resource
		db.deleteResource(resource_PhysicalElement_id_0);
		TestCase.assertFalse(db.hasResource("resource_PhysicalElement_name_0"));
		TestCase.assertNull(db.getToplevelResource(name));
		// is the deleted resource removed from all internal tables too?
		TestCase.assertTrue(db.resIDByName.get(name) == null);
		TestCase.assertTrue(db.resNodeByID.get(id) == null);
		TestCase.assertFalse(db.resIDsByType.get(cls).contains(new Integer(id)));
	}

	@Test
	public void testaddReferenceFromOtherTypeAsDefined() {
		System.out.println("Inside testaddReferenceFromOtherTypeAsDefined");
		boolean exception = false;
		checkDynamicData();
		// first register resource
		try {
			db.addOrUpdateResourceType(AbstractModel.class);
			db.addOrUpdateResourceType(TempStorageSwitchCapacity.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);

		TreeElementImpl freezeCombi = null;
		String resourceName = "yetAnotherFreezeCombi";

		// create a top level resource
		try {
			freezeCombi = (TreeElementImpl) db.addResource(resourceName, AbstractModel.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		try {
			db.addOrUpdateResourceType(TemperatureSensor.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);

		try {
			db.addOrUpdateResourceType(MultiSwitch.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);

		TreeElementImpl node_anySens = null;

		// create a top level TempSens to add it as Reference to freezeCombi
		TreeElementImpl anyTempSens = (TreeElementImpl) db.addResource("yetAnotherTempSens", TemperatureSensor.class,
				testAppID);
		TreeElementImpl anyMultiSwitch = (TreeElementImpl) db.addResource("anyMultiSwitch", MultiSwitch.class,
				testAppID);
		TestCase.assertTrue(db.hasResource("yetAnotherTempSens"));

		try {
			// node_tempSensCool = (TreeElementImpl)
			// freezeCombi.addChild(name_tempSensCool, TemperatureSensor.class, false);
			node_anySens = (TreeElementImpl) freezeCombi.addReference(anyTempSens, "anySensor", false);
			freezeCombi.addReference(anyMultiSwitch, "anyActor", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TreeElement.getReference()
		TestCase.assertTrue(node_anySens.getReference() == anyTempSens);
		// TreeElement.isDecorator()
		TestCase.assertFalse(node_anySens.isDecorator());
		// TreeElement.isReference()
		TestCase.assertTrue(node_anySens.isReference());

		// add a child to the reference
		TreeElementImpl switchCap;
		switchCap = (TreeElementImpl) node_anySens.addChild("switchCap", TempStorageSwitchCapacity.class, true);

		TestCase.assertTrue(node_anySens == (TreeElementImpl) freezeCombi.getChild("anySensor"));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_anySens.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get("anySensor") == null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") ==
		// null);
		TestCase.assertTrue(freezeCombi.requireds.get("anyActor") != null);
		// TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") ==
		// null);
		TestCase.assertTrue(freezeCombi.getChildren().contains(node_anySens));
		TestCase.assertTrue(node_anySens.getChildren().contains(switchCap));

		// add leaves to the referenced resource
		TreeElementImpl moftpk = (TreeElementImpl) switchCap.addChild("meanOffTimePerKelvin", FloatResource.class,
				false);
		TreeElementImpl montpk = (TreeElementImpl) switchCap
				.addChild("meanOnTimePerKelvin", FloatResource.class, false);
		// try to reach the leaves from the the top level resource
		TestCase
				.assertTrue(freezeCombi.getChild("anySensor").getChild("switchCap").getChild("meanOffTimePerKelvin") == moftpk);
		TestCase
				.assertTrue(freezeCombi.getChild("anySensor").getChild("switchCap").getChild("meanOnTimePerKelvin") == montpk);

		// set values of the leaves and read them back
		moftpk.getData().setFloat(300.00f);
		montpk.getData().setFloat(288.88f);
		TestCase.assertTrue(freezeCombi.getChild("anySensor").getChild("switchCap").getChild("meanOffTimePerKelvin")
				.getData().getFloat() == 300.00f);
		TestCase.assertTrue(freezeCombi.getChild("anySensor").getChild("switchCap").getChild("meanOnTimePerKelvin")
				.getData().getFloat() == 288.88f);

		// delete sub resource
		db.deleteResource(switchCap);
		TestCase.assertTrue(freezeCombi.optionals.get("anySensor") == null);
		TestCase.assertTrue(node_anySens == (TreeElementImpl) freezeCombi.getChild("anySensor"));
		TestCase.assertFalse(switchCap == (TreeElementImpl) node_anySens.getChild("switchCap"));

		// add sub resource again
		switchCap = (TreeElementImpl) node_anySens.addChild("switchCap", TempStorageSwitchCapacity.class, true);
		TestCase.assertTrue(node_anySens == (TreeElementImpl) freezeCombi.getChild("anySensor"));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_anySens.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get("anySensor") == null);
		TestCase.assertTrue(freezeCombi.requireds.get("anySensor") != null);

		// delete child of top level resource which is a reference
		// the referenced node has to exist after deleting of the reference
		db.deleteResource(node_anySens);
		TestCase.assertTrue(freezeCombi.getChild("anySensor") == null);
		TestCase.assertTrue(freezeCombi.optionals.get("anySensor") != null);
		TestCase.assertTrue(freezeCombi.requireds.get("anySensor") == null);

		// referenced resource must exist!
		TestCase.assertTrue(db.hasResource(anyTempSens.name));

		exception = false;
		try {
			node_anySens.getChild("switchCap");
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
		checkDynamicData();
	}

	@Test
	public void testaddDeleteReference() {
		System.out.println("Inside addDeleteReference");
		boolean exception = false;
		Class<? extends Resource> freezeModel = null;
		checkDynamicData();
		// first register resource
		try {
			freezeModel = db.addOrUpdateResourceType(FreezeCombi.class);
			db.addOrUpdateResourceType(TempStorageSwitchCapacity.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse("Class FreezeCombi is null.", freezeModel == null);
		TestCase.assertFalse(exception);

		TreeElementImpl freezeCombi = null;
		String resourceName = "ReferencingFreezeCombi";

		// OnOffSwitch onOffSwitch();
		// ElectricityConnection electricityConnection();
		// TemperatureSensor temperatureSensorCooling();
		// TemperatureSensor temperatureSensorFreezing();
		// VolumeResource volumeCooling();
		// VolumeResource volumeFreezing();
		// create a top level resource
		try {
			freezeCombi = (TreeElementImpl) db.addResource(resourceName,
					org.ogema.model.devices.whitegoods.FreezeCombi.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		TreeElementImpl node_tempSensCool = null;

		// create a top level TempSens to add it as Reference to freezeCombi
		TreeElementImpl anyTempSens = (TreeElementImpl) db.addResource("anyTempSens", TemperatureSensor.class,
				testAppID);
		TestCase.assertTrue(db.hasResource("anyTempSens"));

		try {
			// node_tempSensCool = (TreeElementImpl)
			// freezeCombi.addChild(name_tempSensCool, TemperatureSensor.class, false);
			node_tempSensCool = (TreeElementImpl) freezeCombi.addReference(anyTempSens, name_tempSensCool, false);
			freezeCombi.addChild(name_tempSensFreeze, TemperatureSensor.class, false);
			freezeCombi.addChild(name_swtch, OnOffSwitch.class, false);
			freezeCombi.addChild(name_elConn, ElectricityConnection.class, false);
			freezeCombi.addChild(name_ratedVolumeCool, VolumeResource.class, false);
			freezeCombi.addChild(name_ratedVolumeFreeze, VolumeResource.class, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TreeElement.getReference()
		TestCase.assertTrue(node_tempSensCool.getReference() == anyTempSens);
		// TreeElement.isDecorator()
		TestCase.assertFalse(node_tempSensCool.isDecorator());
		// TreeElement.isReference()
		TestCase.assertTrue(node_tempSensCool.isReference());

		// add a child to the reference
		TreeElementImpl switchCap;
		switchCap = (TreeElementImpl) node_tempSensCool.addChild("switchCap", TempStorageSwitchCapacity.class, true);

		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_tempSensCool.getReference().getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") ==
		// null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		// TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") ==
		// null);
		TestCase.assertTrue(freezeCombi.getChildren().contains(node_tempSensCool));
		/*
		 * 
		 */
		TestCase.assertTrue(node_tempSensCool.getChildren().contains(switchCap));

		// add leaves to the referenced resource
		TreeElementImpl moftpk = (TreeElementImpl) switchCap.addChild("meanOffTimePerKelvin", FloatResource.class,
				false);
		TreeElementImpl montpk = (TreeElementImpl) switchCap
				.addChild("meanOnTimePerKelvin", FloatResource.class, false);
		// try to reach the leaves from the the top level resource
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool).getChild("switchCap").getChild(
				"meanOffTimePerKelvin") == moftpk);
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool).getChild("switchCap").getChild(
				"meanOnTimePerKelvin") == montpk);

		// set values of the leaves and read them back
		moftpk.getData().setFloat(300.00f);
		montpk.getData().setFloat(288.88f);
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool).getChild("switchCap").getChild(
				"meanOffTimePerKelvin").getData().getFloat() == 300.00f);
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool).getChild("switchCap").getChild(
				"meanOnTimePerKelvin").getData().getFloat() == 288.88f);

		// delete sub resource
		db.deleteResource(switchCap);
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") !=
		// null);
		// TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) !=
		// null);
		// TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") ==
		// null);
		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertFalse(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));

		// add sub resource again
		switchCap = (TreeElementImpl) node_tempSensCool.addChild("switchCap", TempStorageSwitchCapacity.class, true);
		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") ==
		// null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		// TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") !=
		// null);

		// delete child of top level resource which is a reference
		// the referenced node has to exist after deleting of the reference
		db.deleteResource(node_tempSensCool);
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool) == null);
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) != null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") !=
		// null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") ==
		// null);

		// referenced resource must exist!
		TestCase.assertTrue(db.hasResource(anyTempSens.name));

		exception = false;
		try {
			node_tempSensCool.getChild("switchCap");
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
		checkDynamicData();
	}

	public void deleteSubResource() {
		System.out.println("Inside deleteSubResource");
		boolean exception = false;
		Class<? extends Resource> freezeCombiClass = null;

		// first register resource
		try {
			freezeCombiClass = db.addOrUpdateResourceType(FreezeCombi.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse("Class FreezeCombi is null.", freezeCombiClass == null);
		TestCase.assertFalse(exception);

		TreeElementImpl freezeCombi = null;
		String resourceName = "myFreezeCombi";

		// create a top level resource
		try {
			freezeCombi = (TreeElementImpl) db.addResource(resourceName,
					org.ogema.model.devices.whitegoods.FreezeCombi.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		TreeElementImpl node_tempSensCool = null;

		/*
		 * do it once
		 */
		node_tempSensCool = (TreeElementImpl) freezeCombi.addChild(name_tempSensCool, TemperatureSensor.class, false);
		freezeCombi.addChild(name_tempSensFreeze, TemperatureSensor.class, false);
		freezeCombi.addChild(name_swtch, OnOffSwitch.class, false);
		freezeCombi.addChild(name_elConn, ElectricityConnection.class, false);
		freezeCombi.addChild(name_ratedVolumeCool, VolumeResource.class, false);
		freezeCombi.addChild(name_ratedVolumeFreeze, VolumeResource.class, false);

		TestCase.assertTrue(node_tempSensCool != null);

		TreeElementImpl switchCap;
		switchCap = (TreeElementImpl) node_tempSensCool.addChild("switchCap", TempStorageSwitchCapacity.class, true);

		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") == null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") != null);

		// delete sub resource
		db.deleteResource(switchCap);
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") != null); // switchCap is no longer an
		// optional element of a TemperatureSensor, but only a test-model in persistence
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") == null);
		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertFalse(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));

		// add sub resource again
		switchCap = (TreeElementImpl) node_tempSensCool.addChild("switchCap", TempStorageSwitchCapacity.class, true);
		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") == null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") != null);

		// delete child of top level resource which is parent of a child
		db.deleteResource(node_tempSensCool);
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.getChild("switchCap") == null);
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) != null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") != null); // no longer an optional element
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) == null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") == null);
		exception = false;
		try {
			node_tempSensCool.getChild("switchCap");
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertTrue(exception);

		/*
		 * do it again
		 */
		try {
			node_tempSensCool = (TreeElementImpl) freezeCombi.addChild(name_tempSensCool, TemperatureSensor.class,
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		switchCap = (TreeElementImpl) node_tempSensCool.addChild("switchCap", TempStorageSwitchCapacity.class, true);

		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") == null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") != null);

		// delete sub resource
		db.deleteResource(switchCap);
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") != null); // no longer an optional element
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") == null);
		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertFalse(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));

		// add sub resource again
		switchCap = (TreeElementImpl) node_tempSensCool.addChild("switchCap", TempStorageSwitchCapacity.class, true);
		TestCase.assertTrue(node_tempSensCool == (TreeElementImpl) freezeCombi.getChild(name_tempSensCool));
		TestCase.assertTrue(switchCap == (TreeElementImpl) node_tempSensCool.getChild("switchCap"));
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) == null);
		TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") == null);
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) != null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") != null);

		// delete child of top level resource which is parent of a child
		db.deleteResource(node_tempSensCool);
		TestCase.assertTrue(freezeCombi.getChild(name_tempSensCool) == null);
		// TestCase.assertTrue(node_tempSensCool.getChild("switchCap") == null);
		TestCase.assertTrue(freezeCombi.optionals.get(name_tempSensCool) != null);
		// TestCase.assertTrue(node_tempSensCool.optionals.get("switchCap") != null); // no longer an optional element
		TestCase.assertTrue(freezeCombi.requireds.get(name_tempSensCool) == null);
		TestCase.assertTrue(node_tempSensCool.requireds.get("switchCap") == null);
		exception = false;
		try {
			node_tempSensCool.getChild("switchCap");
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
	}

	/**
	 * abstract: as adding and deleting of types/resources works the DB is now populated with all types and resources
	 * necessary to run the rest of the tests type: helper function
	 */
	@Test
	public void testPopulateDB() {
		System.out.println("Inside testPopulateDB");
		boolean exception = false;
		checkDynamicData();
		// create two different top level resources before the type definitions
		// are added which has to fail. @NOTE Adding of types is implicitly performed, so the following test hasn't
		// longer to fail.
		//
		try {
			resource_PhysicalElement_id_0 = (TreeElementImpl) db.addResource("resource_PhysicalElement_name_tmp",
					NeverRegisterType.class, testAppID);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertTrue(!!/* @NOTE */db.hasResource("resource_PhysicalElement_name_tmp"));
		TestCase.assertTrue(!/* @NOTE */exception);
		exception = false;

		// add the resource types
		try {
			type_PhysicalElement_id = db.addOrUpdateResourceType(org.ogema.model.prototypes.PhysicalElement.class);
			type_TestPhysicalDevice_id = db
					.addOrUpdateResourceType(org.ogema.impl.persistence.TestPhysicalDevice.class);
			db.addOrUpdateResourceType(TempStorageSwitchCapacity.class);
		} catch (Exception e1) {
			TestCase.assertTrue(true);
		}
		TestCase.assertTrue(type_PhysicalElement_id == org.ogema.model.prototypes.PhysicalElement.class);
		TestCase.assertTrue(type_TestPhysicalDevice_id == org.ogema.impl.persistence.TestPhysicalDevice.class);
		TestCase.assertTrue(db.hasResourceType(type_PhysicalElement_name));
		TestCase.assertTrue(db.hasResourceType(type_TestPhysicalDevice_name));

		// create two different top level resources after the type definitions
		// are added which has to succeed.
		try {
			resource_PhysicalElement_id_0 = (TreeElementImpl) db.addResource("resource_PhysicalElement_name",
					org.ogema.model.prototypes.PhysicalElement.class, testAppID);
			TestCase.assertTrue(db.hasResource("resource_PhysicalElement_name"));
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		try {
			resource_TestPhysicalDevice_id_0 = (TreeElementImpl) db.addResource("resource_TestPhysicalDevice_name",
					TestPhysicalDevice.class, testAppID);
			TestCase.assertTrue(db.hasResource("resource_TestPhysicalDevice_name"));
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		// recreate of the resources with the same name must fail
		try {
			resource_PhysicalElement_id_0 = null;
			resource_PhysicalElement_id_0 = (TreeElementImpl) db.addResource("resource_PhysicalElement_name",
					org.ogema.model.prototypes.PhysicalElement.class, testAppID);
			TestCase.assertTrue(resource_PhysicalElement_id_0 == null);

		} catch (Exception e) {
			exception = true;
		}
		// The resource is yet in the database
		TestCase.assertTrue(db.hasResource("resource_PhysicalElement_name"));

		TestCase.assertTrue(exception);
		exception = false;
		try {
			resource_TestPhysicalDevice_id_0 = null;
			resource_TestPhysicalDevice_id_0 = (TreeElementImpl) db.addResource("resource_TestPhysicalDevice_name",
					TestPhysicalDevice.class, testAppID);
			TestCase.assertTrue(resource_TestPhysicalDevice_id_0 == null);
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
		exception = false;
		// The resource is yet in the database
		TestCase.assertTrue(db.hasResource("resource_TestPhysicalDevice_name"));

		// two types are installed currently, verify it
		getAllTypesInstalled();

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
		}
		TestCase.assertFalse(exception);
		exception = false;

		setGetArray();

		setGetValue();

		getResourceName();

		persistentOptional();

		getResourceIdsAndNames();

		deleteSubResource();

		deleteTopLevelResource();
		checkDynamicData();
	}

	@Test
	public void testResourceLists() {
		System.out.println("Inside testResourceLists");
		boolean exception = false;
		Class<? extends Resource> buildingCls = null;
		checkDynamicData();
		// first register resource
		try {
			buildingCls = db.addOrUpdateResourceType(Building.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse("Class Building is null.", buildingCls == null);
		TestCase.assertFalse(exception);

		TreeElementImpl building = null;
		String resourceName = "myHouse";

		String name_propertyUnit = "propertyUnit1";

		// create a top level resource
		try {
			building = (TreeElementImpl) db.addResource(resourceName, Building.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		TreeElementImpl node_propertyUnitsArr = null;

		/*
		 * Add a complexArray Child to the building. 2013.09.13: ResopurceType Building has changed. 'propertyUnit' is
		 * no longer a child of it. The test has to fail.
		 */
		try {
			node_propertyUnitsArr = (TreeElementImpl) building.addChild(name_propertyUnit, ResourceList.class, false);
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertFalse(node_propertyUnitsArr != null);
		TestCase.assertTrue(exception);

		/*
		 * Add a complexArray Child to the building as decorator. The test has to succeed.
		 */
		exception = false;
		try {
			node_propertyUnitsArr = (TreeElementImpl) building.addChild(name_propertyUnit, ResourceList.class, true);
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertFalse(node_propertyUnitsArr == null);
		TestCase.assertTrue(db.hasResource0(node_propertyUnitsArr));
		TestCase.assertFalse(exception);

		/*
		 * Adding a non-decorating child to set the type of the ComplexArrayResource has to succeed.
		 */
		TreeElementImpl units = null;
		exception = false;
		try {
			units = (TreeElementImpl) node_propertyUnitsArr.addChild("units", BuildingPropertyUnit.class, false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(node_propertyUnitsArr == (TreeElementImpl) building.getChild(name_propertyUnit));
		TestCase.assertTrue(units != null);
		TestCase.assertTrue(db.hasResource0(units));

		/*
		 * Adding a non-decorating child of a wrong type to the ComplexArrayResource has to fail
		 */
		TreeElementImpl rooms = null;
		exception = false;
		try {
			rooms = (TreeElementImpl) node_propertyUnitsArr.addChild("rooms", Room.class, false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
		TestCase.assertTrue(node_propertyUnitsArr == (TreeElementImpl) building.getChild(name_propertyUnit));
		TestCase.assertTrue(rooms == null);

		/*
		 * Adding a child of a correct type to the ComplexArrayResource has to succeed
		 */
		TreeElementImpl propertyUnit = null;
		exception = false;
		try {
			propertyUnit = (TreeElementImpl) node_propertyUnitsArr
					.addChild("myUnit", BuildingPropertyUnit.class, false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(propertyUnit == (TreeElementImpl) node_propertyUnitsArr.getChild("myUnit"));
		TestCase.assertTrue(db.hasResource0(propertyUnit));
		/*
		 * Adding a ComplexArrayResource child of a wrong type to the element of a ComplexArrayResource has to succeed
		 */
		exception = false;
		try {
			rooms = (TreeElementImpl) propertyUnit.addChild("rooms", ResourceList.class, false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(rooms == (TreeElementImpl) node_propertyUnitsArr.getChild("myUnit").getChild("rooms"));
		TestCase.assertTrue(db.hasResource0(rooms));
		/*
		 * Adding a child of a wrong type to the ComplexArrayResource has to succeed
		 */
		TreeElementImpl room = null;
		exception = false;
		try {
			room = (TreeElementImpl) rooms.addChild("office1", Room.class, false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(room == (TreeElementImpl) node_propertyUnitsArr.getChild("myUnit").getChild("rooms")
				.getChild("office1"));
		TestCase.assertTrue(db.hasResource0(room));

		/*
		 * Remove a child of type ComplexArrayResource from top level resource building. Thereafter all sub resources of
		 * the removed one mustn't be known in the database.
		 */
		db.deleteResource(node_propertyUnitsArr);
		TestCase.assertFalse(db.hasResource0(node_propertyUnitsArr));
		TestCase.assertFalse(db.hasResource0(propertyUnit));
		TestCase.assertFalse(db.hasResource0(rooms));
		TestCase.assertFalse(db.hasResource0(room));

		/*
		 * After adding of the removed resource its sub resources mustn't exist.
		 */
		try {
			node_propertyUnitsArr = (TreeElementImpl) building.addChild(name_propertyUnit, ResourceList.class, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TestCase.assertTrue(db.hasResource0(node_propertyUnitsArr));
		TestCase.assertFalse(db.hasResource0(propertyUnit));
		TestCase.assertFalse(db.hasResource0(rooms));
		TestCase.assertFalse(db.hasResource0(room));
		checkDynamicData();
	}

	@Test
	public void testResourceListsDecorator() {
		System.out.println("Inside testResourceListsDecorator");
		boolean exception = false;
		Class<? extends Resource> buildingCls = null;
		checkDynamicData();
		// first register resource
		try {
			buildingCls = db.addOrUpdateResourceType(Building.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse("Class Building is null.", buildingCls == null);
		TestCase.assertFalse(exception);

		TreeElementImpl building = null;
		String resourceName = "myHouse2";
		// String name_propertyUnit = "propertyUnit2";

		// create a top level resource
		try {
			building = (TreeElementImpl) db.addResource(resourceName, Building.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		TreeElementImpl node_complexArray = null;
		TreeElementImpl node_ecp = null;

		// /*
		// * Add a complexArray Child to the building as decorator. This test has to fail because a decorator mustn't
		// have
		// * the same name as a model member. 2013.09.13: ResourceType Building has changed. 'propertyUnit' is no longer
		// a
		// * child of it. The test has to pass.
		// */
		// try {
		// node_propertyUnitsArr = (TreeElementImpl) building.addChild(name_propertyUnit, ResourceList.class, true);
		// } catch (Throwable e) {
		// exception = true;
		// }
		// TestCase.assertFalse(exception);
		// TestCase.assertTrue(db.hasResource0(node_propertyUnitsArr));

		/*
		 * Surrogate test for the test case above. It has to fail.
		 */
		exception = false;
		try {
			node_ecp = (TreeElementImpl) building.addChild("electricityConnectionBox", ElectricityConnectionBox.class,
					true);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(node_ecp != null);
		TestCase.assertTrue(exception);

		/*
		 * Try it again with a better choice of a name. Add a complexArray Child to the building as decorator.
		 */
		exception = false;
		try {
			node_complexArray = (TreeElementImpl) building.addChild("allUnits", ResourceList.class, true);
		} catch (Throwable e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertTrue(db.hasResource0(node_complexArray));
		TestCase.assertFalse(exception);

		/*
		 * The added decorator of type ComplexArrayResource hasn't yet a known type
		 */
		TestCase.assertTrue(node_complexArray.type == null);

		/*
		 * Adding a child as decorator to the ComplexArrayResource has to succeed. From now the type Information of this
		 * resource is set and must match types of all added children.
		 */
		TreeElementImpl rooms = null;
		exception = false;
		try {
			rooms = (TreeElementImpl) node_complexArray.addChild("allRooms", Room.class, true);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(node_complexArray == (TreeElementImpl) building.getChild("allUnits"));
		TestCase.assertTrue(rooms != null);
		TestCase.assertTrue(db.hasResource0(rooms));
		TestCase.assertTrue(rooms.decorator);
		TestCase.assertTrue(rooms.type == Room.class);

		/*
		 * Adding of a second decorator of another type as known one to the ComplexArrayResource has to succeed.
		 */
		TreeElementImpl propertyUnit = null;
		exception = false;
		try {
			propertyUnit = (TreeElementImpl) node_complexArray.addChild("myUnit", BuildingPropertyUnit.class, true);
		} catch (Throwable e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(db.hasResource0(propertyUnit));
		TestCase.assertTrue((TreeElementImpl) building.getChild("allUnits").getChild("myUnit") != null);

		/*
		 * Adding a ComplexArrayResource child of a wrong type to the element of a ComplexArrayResource has to succeed
		 * if its a decorator but a complexArray as child of a complexArray is not allowed even its a decorator.
		 */
		TreeElementImpl rooms2 = null;
		exception = false;
		try {
			rooms2 = (TreeElementImpl) node_complexArray.addChild("anotherAllRooms", ResourceList.class, true);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
		TestCase.assertTrue(rooms2 == null);
		TestCase.assertFalse(db.hasResource0(rooms2));

		/*
		 * Remove a child of type ComplexArrayResource from top level resource building. Thereafter all sub resources of
		 * the removed one mustn't be known in the database.
		 */
		db.deleteResource(node_complexArray);
		TestCase.assertFalse(db.hasResource0(node_complexArray));
		TestCase.assertFalse(db.hasResource0(propertyUnit));
		TestCase.assertFalse(db.hasResource0(rooms2));
		TestCase.assertFalse(db.hasResource0(rooms));

		/*
		 * After adding of the removed resource its sub resources mustn't exist.
		 */
		try {
			node_complexArray = (TreeElementImpl) building.addChild("allUnits", BuildingPropertyUnit.class, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TestCase.assertTrue(db.hasResource0(node_complexArray));
		TestCase.assertFalse(db.hasResource0(propertyUnit));
		TestCase.assertFalse(db.hasResource0(rooms));
		TestCase.assertFalse(db.hasResource0(rooms2));
		checkDynamicData();
	}

	@Test
	public void testResourceListsReferences() {
		System.out.println("Inside testResourceListsReferences");
		boolean exception = false;
		Class<? extends Resource> buildingCls = null;
		checkDynamicData();
		// first register resource type
		try {
			buildingCls = db.addOrUpdateResourceType(Building.class);
			db.addOrUpdateResourceType(BuildingPropertyUnit.class);
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse("Class Building is null.", buildingCls == null);
		TestCase.assertFalse(exception);

		TreeElementImpl building = null;
		String resourceName = "myHouse3";

		String name_propertyUnit = "propertyUnit3";

		// create a top level resource
		try {
			building = (TreeElementImpl) db.addResource(resourceName, Building.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		TreeElementImpl node_propertyUnitsArr = null, node_propertyUnitsArrRef = null;

		// create another top level resource to use it as a reference in
		// building
		try {
			node_propertyUnitsArr = (TreeElementImpl) db.addResource(name_propertyUnit, ResourceList.class, testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		/*
		 * Add a complexArray Child to the building as reference. This test has to succeed.
		 */
		try {
			node_propertyUnitsArrRef = (TreeElementImpl) building.addReference(node_propertyUnitsArr,
					name_propertyUnit, true);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(db.hasResource0(node_propertyUnitsArr));
		TestCase.assertTrue(db.hasResource0(node_propertyUnitsArrRef));
		TestCase.assertTrue(node_propertyUnitsArrRef == building.getChild(name_propertyUnit));

		// create another top level resource to use it as a reference in
		// building
		TreeElementImpl node_propertyUnit = null;
		try {
			node_propertyUnit = (TreeElementImpl) db.addResource("propertyUnitElem", BuildingPropertyUnit.class,
					testAppID);
			TestCase.assertTrue(db.hasResource(resourceName));
		} catch (Exception e) {
			e.printStackTrace();
			exception = true;
		}
		TestCase.assertFalse(exception);
		exception = false;

		/*
		 * Add a Child to the node_propertyUnitsArr as reference. This test has to success because reference child in a
		 * reference is supported as long as no loop of references is produced.
		 */
		try {
			node_propertyUnit = (TreeElementImpl) node_propertyUnitsArrRef.addReference(node_propertyUnit,
					"propertyUnitElem", false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(db.hasResource0(node_propertyUnitsArrRef));
		TestCase.assertFalse(node_propertyUnit == building.getChild(name_propertyUnit).getChild("propertyUnitElem"));

		/*
		 * Create a resource to add as reference to the reference child of a ComplexArrayResource.
		 */
		TreeElementImpl rooms = null;
		exception = false;
		try {
			rooms = (TreeElementImpl) db.addResource("allRooms", ResourceList.class, testAppID);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(db.hasResource0(rooms));

		/*
		 * Add a child to a ComplexArrayResource from type ComplexArrayResource as reference. Its an unsupported
		 * operation and has to fail because the resource type is incompatible.
		 */
		exception = false;
		try {
			rooms = (TreeElementImpl) node_propertyUnitsArrRef.addReference(rooms, "allRooms", false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertTrue(exception);
		// TestCase.assertFalse(rooms ==
		// building.getChild(name_propertyUnit).getChild("propertyUnitElem").getChild("allRooms"));
		TestCase.assertTrue(rooms != null);
		TestCase.assertTrue(db.hasResource0(rooms));
		// TestCase.assertFalse(rooms.decorator);

		/*
		 * Adding a child of a correct type to the ComplexArrayResource has to succeed
		 */
		TreeElementImpl propertyUnit = null;
		exception = false;
		try {
			propertyUnit = (TreeElementImpl) node_propertyUnitsArrRef.addChild("myUnit", BuildingPropertyUnit.class,
					false);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(db.hasResource0(propertyUnit));
		TestCase.assertTrue(node_propertyUnitsArrRef == (TreeElementImpl) building.getChild(name_propertyUnit));
		TestCase.assertTrue(node_propertyUnitsArrRef.isReference());
		TestCase.assertTrue(propertyUnit == node_propertyUnitsArrRef.getReference().getChild("myUnit"));
		/*
		 * Remove a child of type ComplexArrayResource from top level resource building. Thereafter all sub resources of
		 * the removed one mustn't be known in the database.
		 */
		db.deleteResource(node_propertyUnitsArr);
		TestCase.assertFalse(db.hasResource0(node_propertyUnitsArr));
		TestCase.assertFalse(db.hasResource0(propertyUnit));
		// TestCase.assertFalse(db.hasResource0(rooms));

		/*
		 * After adding of the removed resource its sub resources mustn't exist.
		 */
		try {
			node_propertyUnitsArr = (TreeElementImpl) building.addChild("allUnits", BuildingPropertyUnit.class, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TestCase.assertTrue(db.hasResource0(node_propertyUnitsArr));
		TestCase.assertFalse(db.hasResource0(propertyUnit));
		// TestCase.assertFalse(db.hasResource0(rooms));
		checkDynamicData();
	}

	/**
	 * abstract: test function getResourceName test-id: scope: single tested functions of ResourceDB interface: String
	 * getResourceName(int id)
	 */
	public void getResourceName() {

		TestCase.assertTrue(resource_PhysicalElement_id_0.getName() == "resource_PhysicalElement_name_0");
	}

	/**
	 * abstract: test function getAllResourceTypesInstalled() test-id: scope: single tested functions of ResourceDB
	 * interface: List<String> getAllResourceTypesInstalled()
	 */
	public void getAllTypesInstalled() {

		List<Class<? extends Resource>> list;

		list = db.getAllResourceTypesInstalled();
		// TestCase.assertEquals(list.size(), 2);
		TestCase.assertTrue(list.contains(type_PhysicalElement_id));
		TestCase.assertTrue(list.contains(type_TestPhysicalDevice_id));
	}

	/**
	 * abstract: test the activation/deactivation of a resource test-id: scope: group tested functions of ResourceDB
	 * interface:
	 * 
	 * isActive(int id) activateResource(int id) deactivateResource(int id)
	 */
	// @Test
	// public void testActivity() {
	//
	// boolean exception_thrown = false;
	// boolean active = false;
	//
	// // inexistent id
	// try {
	// db.activateResource(inexistent_id);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// TestCase.assertTrue(e.getClass() == ResourceNotFoundException.class);
	// }
	// TestCase.assertTrue(exception_thrown);
	// exception_thrown = false;
	//
	// try {
	// db.deactivateResource(inexistent_id);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// TestCase.assertTrue(e.getClass() == ResourceNotFoundException.class);
	// }
	// TestCase.assertTrue(exception_thrown);
	// exception_thrown = false;
	//
	// try {
	// db.isActive(inexistent_id);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// TestCase.assertTrue(e.getClass() == ResourceNotFoundException.class);
	// }
	// TestCase.assertTrue(exception_thrown);
	// exception_thrown = false;
	//
	// // existent id
	// try {
	// db.activateResource(resource_PhysicalElement_id_0);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// }
	// TestCase.assertFalse(exception_thrown);
	//
	// try {
	// active = db.isActive(resource_PhysicalElement_id_0);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// }
	// TestCase.assertFalse(exception_thrown);
	// TestCase.assertTrue(active);
	// exception_thrown = false;
	// active = false;
	//
	// try {
	// db.deactivateResource(resource_PhysicalElement_id_0);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// }
	// TestCase.assertFalse(exception_thrown);
	//
	// try {
	// active = db.isActive(resource_PhysicalElement_id_0);
	// } catch (ResourceNotFoundException e) {
	// exception_thrown = true;
	// }
	// TestCase.assertFalse(exception_thrown);
	// TestCase.assertFalse(active);
	//
	// }
	/**
	 * abstract: test if a resource is optional and persistent test-id: scope: group tested functions of ResourceDB
	 * interface:
	 * 
	 * boolean isPersistent(int attrId) boolean isOptional(int id)
	 */
	public void persistentOptional() {

		// Werte kann man nicht setzen, sind von Beginn an fest; aber in
		// ResourceDB tauchen diese Werte nicht auf?
		boolean exception_thrown = false;
		boolean persistent = false;

		// existent id
		try {
			persistent = !resource_PhysicalElement_id_0.isNonpersistent();
		} catch (ResourceNotFoundException e) {
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);
		TestCase.assertTrue(persistent);
		exception_thrown = false;
	}

	/**
	 * abstract: test functions which get a resource id or name test-id: scope: group tested functions of ResourceDB
	 * interface:
	 * 
	 * getToplevelResourceId(String name) getResourceIds(String[] types, boolean includeInactive)
	 * getAllToplevelResourceIds() getAllToplevelResourceNames()
	 */
	public void getResourceIdsAndNames() {
		TreeElement top_resource_id = null;
		Collection<TreeElement> int_list = null;

		top_resource_id = db.getToplevelResource("resource_PhysicalElement_name_0");
		TestCase.assertTrue(top_resource_id == resource_PhysicalElement_id_0);

		int_list = db.getAllToplevelResources();
		TestCase.assertTrue(int_list.contains(resource_PhysicalElement_id_0));
	}

	/**
	 * abstract: test getTypeChildren(String name) test-id: scope: single tested functions of ResourceDB interface:
	 * List<NodeDesc> getTypeChildren(String name)
	 */
	@Test
	public void getTypeChildren() {
		checkDynamicData();
		Collection<Class<?>> list = null;
		boolean exception_thrown = false;
		db.addOrUpdateResourceType(TestPhysicalDevice.class);
		try {
			list = db.getTypeChildren("org.ogema.impl.persistence.TestPhysicalDevice");
		} catch (InvalidResourceTypeException e) {
			exception_thrown = true;
		}

		TestCase.assertFalse(exception_thrown);
		TestCase.assertEquals(list.size(), 2);
		TestCase.assertTrue(list.contains(TestSensor.class));
		TestCase.assertTrue(list.contains(TestActor.class));
		checkDynamicData();
	}

	/**
	 * abstract: test child and parent functions test-id: scope: group tested functions of ResourceDB interface:
	 * 
	 * int getParentResourceId(int subId, boolean recursive) List<Integer> getChildrenOfType(int id, String subType,
	 * boolean recursive) int getChild(int parentId, String childName)
	 */
	@Test
	public void childParent() {

		checkDynamicData();

		Collection<Class<?>> int_list = null;
		boolean exception_thrown = false;

		Class<? extends Resource> tempType = null;
		try {
			tempType = db.addOrUpdateResourceType(PhysicalElement.class);
		} catch (Exception e) {
			exception_thrown = true;
		}
		TestCase.assertFalse("Class PhysicalElement is null.", tempType == null);
		TestCase.assertFalse(exception_thrown);
		try {
			int_list = db.getTypeChildren(org.ogema.model.prototypes.PhysicalElement.class.getCanonicalName());
		} catch (InvalidResourceTypeException e) {
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);
		TestCase.assertNotNull(int_list);

		TestCase.assertTrue(int_list.contains(ThermalEnergyCapacityResource.class));
		TestCase.assertTrue(int_list.contains(org.ogema.model.locations.Location.class));
		TestCase.assertTrue(int_list.contains(org.ogema.model.locations.PhysicalDimensions.class));
		checkDynamicData();

	}

	/**
	 * abstract: test function decorateResource test-id: scope: single tested functions of ResourceDB interface:
	 * 
	 * decorateResource(int id, String additionalType, String name, boolean makeVolatile)
	 */
	// No types but resources can be decorated
	// @Test
	// public void test_decorateResource() {
	//
	// TreeElement decorating_sub_resource_id = null;
	// boolean exception_thrown = false;
	// List<NodeDesc> list = null;
	//
	// try {
	// decorating_sub_resource_id =
	// db.decorateResource("org.ogema.junit_persistance.TestActor", "actor_0",
	// false);
	// } catch (ResourceAlreadyExistsException | TypeNotFoundException e) {
	// exception_thrown = true;
	// }
	// TestCase.assertFalse(exception_thrown);
	//
	// try {
	// list = db.getTypeChildren("org.ogema.core.model.prototypes.PhysicalElement");
	// } catch (TypeNotFoundException e) {
	// exception_thrown = true;
	// }
	// TestCase.assertFalse(exception_thrown);
	//
	// TestCase.assertEquals(list.size(), 4);
	// TestCase.assertFalse(exception_thrown);
	// TestCase.assertTrue(list.get(0).getName() == "RoomInformation");
	// TestCase.assertTrue(list.get(1).getName() == "FirmwareInfo");
	// TestCase.assertTrue(list.get(2).getName() == "PhysicalDimensions");
	// TestCase.assertTrue(list.get(3).getName() ==
	// "org.ogema.junit_persistance.TestActor");
	// exception_thrown = false;
	//
	// // ResourceAlreadyExistsException
	// try {
	// decorating_sub_resource_id = db.decorateResource(type_PhysicalElement_id,
	// "org.ogema.junit_persistance.TestActor", "actor_0", false);
	// } catch (ResourceAlreadyExistsException | TypeNotFoundException e) {
	// exception_thrown = true;
	// TestCase.assertTrue(e.getClass() ==
	// ResourceAlreadyExistsException.class);
	//
	// }
	// TestCase.assertTrue(exception_thrown);
	//
	// // TypeNotFoundException
	//
	// }
	/**
	 * abstract: test functions for setting and getting values test-id: scope: group tested functions of ResourceDB
	 * interface: void setFloatValue(int attrId, float value) void setIntValue(int attrId, int value) void
	 * setLongValue(int attrId, long value) void setStringValue(int attrId, String value) void setBooleanValue(int
	 * attrId, boolean value) void setOpaqueValue(int attrId, byte[] value) float getFloatValue(int attrId) int
	 * getIntValue(int attrId) long getLongValue(int attrId) String getStringValue(int attrId) boolean
	 * getBooleanValue(int attrId byte[] getOpaqueValue(int attrId)
	 */
	public void setGetValue() {

		boolean exception_thrown = false;
		TreeElementImpl sub_resource_bool_id;
		TreeElementImpl sub_resource_float_id;
		TreeElementImpl sub_resource_int_id;
		TreeElementImpl sub_resource_opaque_id;
		TreeElementImpl sub_resource_string_id;
		TreeElementImpl sub_resource_time_id;

		sub_resource_TestActor_id_0 = resource_TestPhysicalDevice_id_0.requireds.get("actor");
		// sensor is yet optional, so its not a member of the requireds
		TestCase.assertTrue(sub_resource_TestActor_id_0 == null);

		sub_resource_TestActor_id_0 = resource_TestPhysicalDevice_id_0.optionals.get("actor");
		// sensor is yet optional, so its a member of the optionals
		TestCase.assertTrue(sub_resource_TestActor_id_0 != null);

		// change the situation by adding sensor as a required member
		// first we try to add a child with the wrong type class
		// it must fail with TypenotfoundExcception
		try {
			resource_TestPhysicalDevice_id_0.addChild("actor", TestSensor.class, false);
		} catch (Exception e2) {
			// e2.printStackTrace();
			exception_thrown = true;
		}
		TestCase.assertTrue(exception_thrown);

		exception_thrown = false;
		// and than we try to add a child with the right type class
		// it must success
		try {
			resource_TestPhysicalDevice_id_0.addChild("actor", TestActor.class, false);
		} catch (Exception e2) {
			// e2.printStackTrace();
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);

		// now sensor is a required member
		sub_resource_TestActor_id_0 = resource_TestPhysicalDevice_id_0.requireds.get("actor");
		TestCase.assertTrue(sub_resource_TestActor_id_0 != null);

		// getChild must deliver a TreeElement object for sensor
		sub_resource_TestActor_id_0 = (TreeElementImpl) resource_TestPhysicalDevice_id_0.getChild("actor");
		TestCase.assertTrue(sub_resource_TestActor_id_0 != null);
		checkNodeAttributes(sub_resource_TestActor_id_0);

		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		sub_resource_float_id = sub_resource_TestActor_id_0.requireds.get("float_res");
		sub_resource_int_id = sub_resource_TestActor_id_0.requireds.get("int_res");
		sub_resource_string_id = sub_resource_TestActor_id_0.requireds.get("string_res");
		sub_resource_bool_id = sub_resource_TestActor_id_0.requireds.get("bool_res");
		sub_resource_time_id = sub_resource_TestActor_id_0.requireds.get("time_res");
		sub_resource_opaque_id = sub_resource_TestActor_id_0.requireds.get("opaque_res");
		TestCase.assertTrue(sub_resource_float_id == null);
		TestCase.assertTrue(sub_resource_int_id == null);
		TestCase.assertTrue(sub_resource_string_id == null);
		TestCase.assertTrue(sub_resource_bool_id == null);
		TestCase.assertTrue(sub_resource_time_id == null);
		TestCase.assertTrue(sub_resource_opaque_id == null);

		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		sub_resource_float_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("float_res");
		sub_resource_int_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("int_res");
		sub_resource_string_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("string_res");
		sub_resource_bool_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("bool_res");
		sub_resource_time_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("time_res");
		sub_resource_opaque_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("opaque_res");
		TestCase.assertTrue(sub_resource_float_id == null);
		TestCase.assertTrue(sub_resource_int_id == null);
		TestCase.assertTrue(sub_resource_string_id == null);
		TestCase.assertTrue(sub_resource_bool_id == null);
		TestCase.assertTrue(sub_resource_time_id == null);
		TestCase.assertTrue(sub_resource_opaque_id == null);

		// as default all sub resources are optional until they are added via
		// addChild
		sub_resource_float_id = sub_resource_TestActor_id_0.optionals.get("float_res");
		sub_resource_int_id = sub_resource_TestActor_id_0.optionals.get("int_res");
		sub_resource_string_id = sub_resource_TestActor_id_0.optionals.get("string_res");
		sub_resource_bool_id = sub_resource_TestActor_id_0.optionals.get("bool_res");
		sub_resource_time_id = sub_resource_TestActor_id_0.optionals.get("time_res");
		sub_resource_opaque_id = sub_resource_TestActor_id_0.optionals.get("opaque_res");
		TestCase.assertTrue(sub_resource_float_id != null);
		TestCase.assertTrue(sub_resource_int_id != null);
		TestCase.assertTrue(sub_resource_string_id != null);
		TestCase.assertTrue(sub_resource_bool_id != null);
		TestCase.assertTrue(sub_resource_time_id != null);
		TestCase.assertTrue(sub_resource_opaque_id != null);

		exception_thrown = false;
		// add sub resources to the TestSensor as childs
		try {
			sub_resource_float_id = (TreeElementImpl) sub_resource_TestActor_id_0.addChild("float_res",
					org.ogema.core.model.simple.FloatResource.class, false);
			sub_resource_int_id = (TreeElementImpl) sub_resource_TestActor_id_0.addChild("int_res",
					org.ogema.core.model.simple.IntegerResource.class, false);
			sub_resource_string_id = (TreeElementImpl) sub_resource_TestActor_id_0.addChild("string_res",
					org.ogema.core.model.simple.StringResource.class, false);
			sub_resource_bool_id = (TreeElementImpl) sub_resource_TestActor_id_0.addChild("bool_res",
					org.ogema.core.model.simple.BooleanResource.class, false);
			sub_resource_time_id = (TreeElementImpl) sub_resource_TestActor_id_0.addChild("time_res",
					org.ogema.core.model.simple.TimeResource.class, false);
			sub_resource_opaque_id = (TreeElementImpl) sub_resource_TestActor_id_0.addChild("opaque_res",
					OpaqueResource.class, false);
		} catch (Exception e2) {
			exception_thrown = true;
			e2.printStackTrace();
		}
		TestCase.assertTrue(!exception_thrown);
		TestCase.assertTrue(sub_resource_float_id != null);
		TestCase.assertTrue(sub_resource_int_id != null);
		TestCase.assertTrue(sub_resource_string_id != null);
		TestCase.assertTrue(sub_resource_bool_id != null);
		TestCase.assertTrue(sub_resource_time_id != null);
		TestCase.assertTrue(sub_resource_opaque_id != null);

		// checks after addchild for sub resources
		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		sub_resource_float_id = sub_resource_TestActor_id_0.requireds.get("float_res");
		sub_resource_int_id = sub_resource_TestActor_id_0.requireds.get("int_res");
		sub_resource_string_id = sub_resource_TestActor_id_0.requireds.get("string_res");
		sub_resource_bool_id = sub_resource_TestActor_id_0.requireds.get("bool_res");
		sub_resource_time_id = sub_resource_TestActor_id_0.requireds.get("time_res");
		sub_resource_opaque_id = sub_resource_TestActor_id_0.requireds.get("opaque_res");
		TestCase.assertTrue(sub_resource_float_id != null);
		TestCase.assertTrue(sub_resource_int_id != null);
		TestCase.assertTrue(sub_resource_string_id != null);
		TestCase.assertTrue(sub_resource_bool_id != null);
		TestCase.assertTrue(sub_resource_time_id != null);
		TestCase.assertTrue(sub_resource_opaque_id != null);

		// as default all sub resources are optional until they are added via
		// addChild
		sub_resource_float_id = sub_resource_TestActor_id_0.optionals.get("float_res");
		sub_resource_int_id = sub_resource_TestActor_id_0.optionals.get("int_res");
		sub_resource_string_id = sub_resource_TestActor_id_0.optionals.get("string_res");
		sub_resource_bool_id = sub_resource_TestActor_id_0.optionals.get("bool_res");
		sub_resource_time_id = sub_resource_TestActor_id_0.optionals.get("time_res");
		sub_resource_opaque_id = sub_resource_TestActor_id_0.optionals.get("opaque_res");
		TestCase.assertTrue(sub_resource_float_id == null);
		TestCase.assertTrue(sub_resource_int_id == null);
		TestCase.assertTrue(sub_resource_string_id == null);
		TestCase.assertTrue(sub_resource_bool_id == null);
		TestCase.assertTrue(sub_resource_time_id == null);
		TestCase.assertTrue(sub_resource_opaque_id == null);

		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		// getter/setter LeafValues
		// first get the node references to the leaf resources
		// float
		sub_resource_float_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("float_res");
		TestCase.assertTrue(sub_resource_float_id != null);
		setGetFloat(sub_resource_float_id);

		// boolean
		sub_resource_bool_id = (TreeElementImpl) sub_resource_TestActor_id_0.getChild("bool_res");
		TestCase.assertTrue(sub_resource_bool_id != null);
		setGetBool(sub_resource_bool_id);

		// int
		// opaque
		// string
		// time
	}

	/**
	 * abstract: test the set and get functions for arrays test-id: scope: group tested functions of ResourceDB
	 * interface:
	 * 
	 * float[] setFloatArray(int attrId, float[] fa float[] getFloatArray(int attrId) int[] setIntArray(int attrId,
	 * int[] ia) int[] getIntArray(int attrId) String[] setStringArray(int attrId, String[] stra) String[]
	 * getStringArray(int attrId) boolean[] setBooleanArray(int attrId, boolean[] boola) boolean[] getBooleanArray(int
	 * attrId) long[] setTimeArray(int attrId, long[] la) long[] getTimeArray(int attrId) int[] setComplexArray(int
	 * attrId, int[] ia) int[] getComplexArray(int attrId)
	 */
	public void setGetArray() {

		boolean exception_thrown = false;
		TreeElementImpl sub_resource_float_array_id = null;
		TreeElementImpl sub_resource_int_array_id = null;
		TreeElementImpl sub_resource_string_array_id = null;
		TreeElementImpl sub_resource_bool_array_id = null;
		TreeElementImpl sub_resource_time_array_id = null;
		TreeElementImpl sub_resource_complex_array_id = null;

		sub_resource_TestSensor_id_0 = resource_TestPhysicalDevice_id_0.requireds.get("sensor");
		// sensor is yet optional, so its not a member of the requireds
		TestCase.assertTrue(sub_resource_TestSensor_id_0 == null);

		sub_resource_TestSensor_id_0 = resource_TestPhysicalDevice_id_0.optionals.get("sensor");
		// sensor is yet optional, so its a member of the optionals
		TestCase.assertTrue(sub_resource_TestSensor_id_0 != null);

		// change the situation by adding sensor as a required member
		try {
			resource_TestPhysicalDevice_id_0.addChild("sensor", TestSensor.class, false);
		} catch (Exception e2) {
			e2.printStackTrace();
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);

		// now sensor is a required member
		sub_resource_TestSensor_id_0 = resource_TestPhysicalDevice_id_0.requireds.get("sensor");
		TestCase.assertTrue(sub_resource_TestSensor_id_0 != null);

		// getChild must deliver a TreeElement object for sensor
		sub_resource_TestSensor_id_0 = (TreeElementImpl) resource_TestPhysicalDevice_id_0.getChild("sensor");
		TestCase.assertTrue(sub_resource_TestSensor_id_0 != null);
		checkNodeAttributes(sub_resource_TestSensor_id_0);

		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		sub_resource_float_array_id = sub_resource_TestSensor_id_0.requireds.get("float_array_res");
		sub_resource_int_array_id = sub_resource_TestSensor_id_0.requireds.get("int_array_res");
		sub_resource_string_array_id = sub_resource_TestSensor_id_0.requireds.get("string_array_res");
		sub_resource_bool_array_id = sub_resource_TestSensor_id_0.requireds.get("bool_array_res");
		sub_resource_time_array_id = sub_resource_TestSensor_id_0.requireds.get("time_array_res");
		sub_resource_complex_array_id = sub_resource_TestSensor_id_0.requireds.get("complex_array_res");
		TestCase.assertTrue(sub_resource_float_array_id == null);
		TestCase.assertTrue(sub_resource_int_array_id == null);
		TestCase.assertTrue(sub_resource_string_array_id == null);
		TestCase.assertTrue(sub_resource_bool_array_id == null);
		TestCase.assertTrue(sub_resource_time_array_id == null);
		TestCase.assertTrue(sub_resource_complex_array_id == null);

		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		sub_resource_float_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("float_array_res");
		sub_resource_int_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("int_array_res");
		sub_resource_string_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("string_array_res");
		sub_resource_bool_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("bool_array_res");
		sub_resource_time_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("time_array_res");
		sub_resource_complex_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("complex_array_res");
		TestCase.assertTrue(sub_resource_float_array_id == null);
		TestCase.assertTrue(sub_resource_int_array_id == null);
		TestCase.assertTrue(sub_resource_string_array_id == null);
		TestCase.assertTrue(sub_resource_bool_array_id == null);
		TestCase.assertTrue(sub_resource_time_array_id == null);
		TestCase.assertTrue(sub_resource_complex_array_id == null);

		// as default all sub resources are optional until they are added via
		// addChild
		sub_resource_float_array_id = sub_resource_TestSensor_id_0.optionals.get("float_array_res");
		sub_resource_int_array_id = sub_resource_TestSensor_id_0.optionals.get("int_array_res");
		sub_resource_string_array_id = sub_resource_TestSensor_id_0.optionals.get("string_array_res");
		sub_resource_bool_array_id = sub_resource_TestSensor_id_0.optionals.get("bool_array_res");
		sub_resource_time_array_id = sub_resource_TestSensor_id_0.optionals.get("time_array_res");
		sub_resource_complex_array_id = sub_resource_TestSensor_id_0.optionals.get("complex_array_res");
		TestCase.assertTrue(sub_resource_float_array_id != null);
		TestCase.assertTrue(sub_resource_int_array_id != null);
		TestCase.assertTrue(sub_resource_string_array_id != null);
		TestCase.assertTrue(sub_resource_bool_array_id != null);
		TestCase.assertTrue(sub_resource_time_array_id != null);
		TestCase.assertTrue(sub_resource_complex_array_id != null);

		exception_thrown = false;
		// add sub resources to the TestSensor as childs
		try {
			sub_resource_float_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.addChild("float_array_res",
					org.ogema.core.model.array.FloatArrayResource.class, false);
			sub_resource_int_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.addChild("int_array_res",
					IntegerArrayResource.class, false);
			sub_resource_string_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.addChild("string_array_res",
					StringArrayResource.class, false);
			sub_resource_bool_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.addChild("bool_array_res",
					BooleanArrayResource.class, false);
			sub_resource_time_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.addChild("time_array_res",
					TimeArrayResource.class, false);
			sub_resource_complex_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.addChild(
					"complex_array_res", ResourceList.class, false);
		} catch (Exception e2) {
			exception_thrown = true;
			e2.printStackTrace();
		}
		TestCase.assertTrue(!exception_thrown);
		TestCase.assertTrue(sub_resource_float_array_id != null);
		TestCase.assertTrue(sub_resource_int_array_id != null);
		TestCase.assertTrue(sub_resource_string_array_id != null);
		TestCase.assertTrue(sub_resource_bool_array_id != null);
		TestCase.assertTrue(sub_resource_time_array_id != null);
		TestCase.assertTrue(sub_resource_complex_array_id != null);

		// checks after addchild for sub resources
		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		sub_resource_float_array_id = sub_resource_TestSensor_id_0.requireds.get("float_array_res");
		sub_resource_int_array_id = sub_resource_TestSensor_id_0.requireds.get("int_array_res");
		sub_resource_string_array_id = sub_resource_TestSensor_id_0.requireds.get("string_array_res");
		sub_resource_bool_array_id = sub_resource_TestSensor_id_0.requireds.get("bool_array_res");
		sub_resource_time_array_id = sub_resource_TestSensor_id_0.requireds.get("time_array_res");
		sub_resource_complex_array_id = sub_resource_TestSensor_id_0.requireds.get("complex_array_res");
		TestCase.assertTrue(sub_resource_float_array_id != null);
		TestCase.assertTrue(sub_resource_int_array_id != null);
		TestCase.assertTrue(sub_resource_string_array_id != null);
		TestCase.assertTrue(sub_resource_bool_array_id != null);
		TestCase.assertTrue(sub_resource_time_array_id != null);
		TestCase.assertTrue(sub_resource_complex_array_id != null);

		// as default all sub resources are optional until they are added via
		// addChild
		sub_resource_float_array_id = sub_resource_TestSensor_id_0.optionals.get("float_array_res");
		sub_resource_int_array_id = sub_resource_TestSensor_id_0.optionals.get("int_array_res");
		sub_resource_string_array_id = sub_resource_TestSensor_id_0.optionals.get("string_array_res");
		sub_resource_bool_array_id = sub_resource_TestSensor_id_0.optionals.get("bool_array_res");
		sub_resource_time_array_id = sub_resource_TestSensor_id_0.optionals.get("time_array_res");
		sub_resource_complex_array_id = sub_resource_TestSensor_id_0.optionals.get("complex_array_res");
		TestCase.assertTrue(sub_resource_float_array_id == null);
		TestCase.assertTrue(sub_resource_int_array_id == null);
		TestCase.assertTrue(sub_resource_string_array_id == null);
		TestCase.assertTrue(sub_resource_bool_array_id == null);
		TestCase.assertTrue(sub_resource_time_array_id == null);
		TestCase.assertTrue(sub_resource_complex_array_id == null);

		// as default all sub resources are optional and not a member of the
		// resource until they are added via addChild
		// getter/setter LeafValues
		// first get the node references to the leaf resources
		// float
		sub_resource_float_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("float_array_res");
		TestCase.assertTrue(sub_resource_float_array_id != null);
		setGetFloatArray(sub_resource_float_array_id);

		sub_resource_int_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("int_array_res");
		sub_resource_string_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("string_array_res");
		sub_resource_bool_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("bool_array_res");
		sub_resource_time_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("time_array_res");
		sub_resource_complex_array_id = (TreeElementImpl) sub_resource_TestSensor_id_0.getChild("complex_array_res");
		TestCase.assertTrue(sub_resource_int_array_id != null);
		TestCase.assertTrue(sub_resource_string_array_id != null);
		TestCase.assertTrue(sub_resource_bool_array_id != null);
		TestCase.assertTrue(sub_resource_time_array_id != null);
		TestCase.assertTrue(sub_resource_complex_array_id != null);

	}

	void setGetFloat(TreeElementImpl e) {
		boolean exception_thrown = false;
		float float_value = 3.234F;
		float float_0 = 0, float_result = 0;

		try {
			float_0 = e.getData().getFloat();
		} catch (ResourceNotFoundException e1) {
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);
		TestCase.assertTrue(float_0 == 0.0f);
		exception_thrown = false;

		try {
			e.getData().setFloat(float_value);
			float_result = e.getData().getFloat();
		} catch (ResourceNotFoundException ex) {
			exception_thrown = true;
		}

		TestCase.assertFalse(exception_thrown);
		TestCase.assertTrue(float_result == float_value);

	}

	void setGetBool(TreeElementImpl e) {
		boolean exception_thrown = false;
		boolean bool_value = true;
		boolean bool_false = true, bool_result = false;

		try {
			bool_false = e.getData().getBoolean();
		} catch (ResourceNotFoundException e1) {
			exception_thrown = true;
		}
		TestCase.assertFalse(exception_thrown);
		TestCase.assertTrue(bool_false != true);
		exception_thrown = false;

		try {
			e.getData().setBoolean(bool_value);
			bool_result = e.getData().getBoolean();
		} catch (ResourceNotFoundException ex) {
			exception_thrown = true;
		}

		TestCase.assertFalse(exception_thrown);
		TestCase.assertTrue(bool_result == bool_value);
		exception_thrown = false;

	}

	void setGetFloatArray(TreeElementImpl e) {
		boolean exception_thrown = false;
		float[] float_array = { 3.234F, 5455.345F, 2354.45F };
		float[] float_array_result = new float[3];
		try {
			e.getData().setFloatArr(float_array);
			float_array_result = e.getData().getFloatArr();
		} catch (ResourceNotFoundException ex) {
			exception_thrown = true;
		}

		TestCase.assertFalse(exception_thrown);
		TestCase.assertTrue(float_array_result[0] == float_array[0]);
		TestCase.assertTrue(float_array_result[1] == float_array[1]);
		TestCase.assertTrue(float_array_result[2] == float_array[2]);
		exception_thrown = false;

	}

	private void checkNodeAttributes(TreeElementImpl e) {
		TestCase.assertTrue(((e.parent == null) && (db.getToplevelResource(e.name) == e))
				|| ((e.parent != null) && (e.parent.getChild(e.name) == e)));
		TestCase.assertTrue(((e.topLevelParent == null) && (e.toplevel))
				|| ((e.topLevelParent != null) && (db.getToplevelResource(e.topLevelParent.name) == e.topLevelParent)));
		TestCase.assertTrue((e.active == e.topLevelParent.active)
				&& (((e.parent != null) && (e.active == e.parent.active)) || (e.parent == null)));
		TestCase.assertTrue(e.appID.equals(testAppID));
		TestCase.assertTrue((e.parent.optionals.get(e.name) == e) || (e.parent.requireds.get(e.name) == e));
		TestCase.assertTrue(db.hasResourceType(e.type.getName()));
		TestCase.assertTrue(e == db.resNodeByID.get(db.resIDByName.get(e.path)));
		TestCase.assertTrue(e.resRef == null);
		// e.nonpersistent = false;
		// e.optional = false;
		TestCase.assertTrue(((e.decorator == false) && (e.parent.typeChildren.get(e.name) == e.type))
				|| ((e.decorator == true) && (e.parent.typeChildren.get(e.name) == null)));

		TestCase.assertTrue(db.resNodeByID.get(e.resID) == e);

	}

	@Test
	public void cyclicReferencesWork() {
		checkDynamicData();
		int counter = 0;
		TreeElement e;
		while (counter < 10) {
			e = db.addResource("elSwitch" + counter++, OnOffSwitch.class, "yetAnotherApp");
			e.addChild("stateControl", BooleanResource.class, false);
			e.getChild("stateControl").addReference(e, "fnord", true);
		}
		checkDynamicData();
	}

	@Test
	public void testModelImplNonResourceInterface() {
		System.out.println("Inside testModelInplNonResourceInterface");
		boolean exception = false;
		Class<? extends Resource> cls = null;
		checkDynamicData();
		try {
			cls = db.addOrUpdateResourceType(ModelImplNonResourceInterface.class);
		} catch (Exception e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertFalse("Class PhysicalRlrmrnt is null.", cls == null);

		exception = false;
		try {
			db.addResource("extendedResource", org.ogema.model.prototypes.PhysicalElement.class, testAppID);
		} catch (Throwable e) {
			exception = true;
		}
		TestCase.assertFalse(exception);
		TestCase.assertTrue(db.hasResource("extendedResource"));
		checkDynamicData();
	}
}
