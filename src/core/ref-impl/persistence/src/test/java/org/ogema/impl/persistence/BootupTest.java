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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.persistence.DBConstants;

import junit.framework.TestCase;

@RunWith(Parameterized.class)
public class BootupTest {

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][] { { 6, "scenario21" }, { 5, "compaction" }, { 4, "scenario2" },
				{ 1, "scenario11" }, { 2, "scenario12" }, { 3, "scenario13" } });
	}

	static final String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, "
			+ "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,"
			+ " sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
			+ " Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
			+ " Lorem ipsum dolor sit amet, consetetur sadipscing elitr,"
			+ " sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, "
			+ "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum."
			+ " Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
	private static int executionOrder;
	private static String path;

	public BootupTest(int count, String name) {
		executionOrder = count;
		path = name;
	}

	static ResourceDBImpl db;

	@BeforeClass
	public static void init() {
	}

	// Just an example appID, which has to be unique and probably will have the
	// format of a file path.
	@SuppressWarnings("unused")
	private String testAppID = "/persistence/target/persistence-2.0-SNAPSHOT.jar";

	@Before
	public void before() throws InterruptedException {
		System.setProperty("org.ogema.persistence", "active");
		System.setProperty(DBConstants.DB_PATH_PROP, "./src/test/resources/" + path);
		System.setProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE, "100");
		removeFiles(executionOrder, path);
		copyFiles(executionOrder, path);
		db = new ResourceDBImpl();
		db.setName("BootupTest");
		db.init();

	}

	private void copyFiles(int order, String path) {
		File dir = new File("./src/test/resources/test" + order);
		Path targetDir = FileSystems.getDefault().getPath("./src", "test", "resources", path);
		try {
			Files.createDirectories(targetDir);
			String files[] = dir.list();
			for (String file : files) {
				Path source = FileSystems.getDefault().getPath("./src", "test", "resources", "test" + order, file);
				if (!Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
					Path targetFile = FileSystems.getDefault().getPath("./src", "test", "resources", path, file);
					Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void removeFiles(int order, String path) {
		File dir = new File("./src/test/resources/" + path);
		if (!dir.exists())
			dir.mkdir();
		String files[] = dir.list();
		for (String file : files) {
			new File(dir, file).delete();
		}
	}

	@After
	public void after() throws InterruptedException {

	}

	@Test
	public void checkPersistenceFilesAfterRestart() {
		String testID = "BootupTest";// this.toString();
		System.out.println(testID + executionOrder);
		switch (executionOrder) {
		case 1:
			TestCase.assertEquals("resMap4", db.resourceIO.dirFiles.fileNew.getName());
			TestCase.assertEquals("resMap3", db.resourceIO.dirFiles.fileOld.getName());
			TestCase.assertEquals("resData6", db.resourceIO.resDataFiles.fileNew.getName());
			break;
		case 2:
			TestCase.assertEquals("resMap3", db.resourceIO.dirFiles.fileNew.getName());
			TestCase.assertNull(db.resourceIO.dirFiles.fileOld);
			TestCase.assertEquals("resData6", db.resourceIO.resDataFiles.fileNew.getName());
			break;
		case 3:
			TestCase.assertEquals("resMap4", db.resourceIO.dirFiles.fileNew.getName());
			TestCase.assertEquals("resMap3", db.resourceIO.dirFiles.fileOld.getName());
			TestCase.assertEquals("resData6", db.resourceIO.resDataFiles.fileNew.getName());
			break;
		case 4:
			TestCase.assertNull(db.resourceIO.dirFiles.fileNew);
			TestCase.assertNull(db.resourceIO.dirFiles.fileOld);
			TestCase.assertEquals("resData2", db.resourceIO.dataFile.fileName);
			break;
		case 5:
			try {
				TreeElementImpl stringArrRes = (TreeElementImpl) db.getToplevelResource("TestStringArray1");
				if (stringArrRes == null)
					stringArrRes = (TreeElementImpl) db.addResource("TestStringArray1", StringArrayResource.class,
							this.getClass().getName());
				String[] strArr = { text, text, text, text, text, text, text, text, text, text, text, text, text, text,
						text, text, text, text, text, text, text, text, text, text, text, text, text, text, text, text,
						text, text, text, text, text, text, text, text, text };
				int len = strArr.length;
				// while (true) {
				int count = 20;
				while (count-- > 0) {
					stringArrRes.getData().setStringArr(strArr);
					try {
						Thread.sleep(((TimedPersistence) db.persistence).storePeriod / 5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				db.restart();
				TreeElementImpl stringArrRes2 = (TreeElementImpl) db.getToplevelResource("TestStringArray1");
				TestCase.assertNotNull(stringArrRes2);
				String[] strArr2 = stringArrRes2.getData().getStringArr();
				count = 0;
				while (len > count) {
					TestCase.assertEquals(strArr[count], strArr2[count]);
					count++;
				}
				// }
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		case 6:

			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unused")
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
			}
			TestCase.assertTrue(success);
		}
		// 2. typeClassByName list
		{
			Set<Entry<String, Class<?>>> tlrs = typeClassByName.entrySet();
			for (Map.Entry<String, Class<?>> entry : tlrs) {
				Class<?> clsOld = entry.getValue();
				Class<?> clsNew = db.typeClassByName.get(clsOld.getName());
				if (clsNew == null) {
					success = false;
					break;
				}
				if (clsOld != clsNew) {
					success = false;
					break;
				}
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
					// System.out.println(key);
					// System.out.println(db);
					// System.out.println(db.resIDByName);
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
