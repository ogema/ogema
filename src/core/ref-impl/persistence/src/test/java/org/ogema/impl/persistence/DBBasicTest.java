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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.ogema.core.model.ResourceList;

public class DBBasicTest {

	static ResourceDBImpl db;

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
}
