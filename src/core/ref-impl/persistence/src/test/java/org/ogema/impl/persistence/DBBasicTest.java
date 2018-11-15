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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.ogema.core.model.ResourceList;
import org.ogema.persistence.DBConstants;

public class DBBasicTest {

	ResourceDBImpl db;

	void checkDynamicData() {
		// get current maps of the resources
		ConcurrentHashMap<String, TreeElementImpl> root = db.root;
		ConcurrentHashMap<String, Class<?>> typeClassByName = db.typeClassByName;
		ConcurrentHashMap<String, Integer> resIDByName = db.resIDByName;
		ConcurrentHashMap<Integer, TreeElementImpl> resNodeByID = db.resNodeByID;
		ConcurrentHashMap<String, Vector<Integer>> resIDsByType = db.resIDsByType;
		// check the consistency of the maps
		boolean success = true;
		// 1. root list
		{
			Set<Entry<String, TreeElementImpl>> tlrs = root.entrySet();
			for (Map.Entry<String, TreeElementImpl> entry : tlrs) {
				TreeElementImpl res = entry.getValue();
				if (!res.toplevel || res.topLevelParent != res || res.parent != null) {
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
				Class<?> cls = entry.getValue();
				String name = entry.getKey();
				if (!name.equals(cls.getName())) {
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
				int val = entry.getValue();
				String path = entry.getKey();
				TreeElementImpl res = resNodeByID.get(val);
				if (!path.equals(res.path)) {
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
				String cls = entry.getKey();
				Vector<Integer> values = entry.getValue();

				for (int ID : values) {
					TreeElementImpl res = resNodeByID.get(ID);
					if (res.resID != ID) {
						success = false;
						break;
					}
					if (!cls.equals(ResourceList.class.getName()) && !res.type.getName().equals(cls)) {
						success = false;
						break;
					}
				}
				if (!success)
					break;
			}
			TestCase.assertTrue(success);
		}
	}

	void restartAndCompareDynamicData() {
		db.doStorage();
		// try {
		// Thread.sleep(2 * TimedPersistence.DEFAULT_STOREPERIOD);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// get current maps of the resources
		db.stopStorage();
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
					System.err.println("oldValue/newValue -> " + oldVal + "/" + newVal);
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
				if (!node.isReference() && !resNew.isReference()) {
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
				}
				else if (node.isReference() && resNew.isReference()) {
					if (!node.equals(resNew))
						success = false;
				}
				else
					success = false;
				if (success == false) {
					break;
				}
			}
			TestCase.assertTrue(success);
		}
	}

}
