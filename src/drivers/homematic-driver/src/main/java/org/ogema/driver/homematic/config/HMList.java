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
package org.ogema.driver.homematic.config;

import java.util.ArrayList;
import java.util.HashMap;

public class HMList {

	public static HMList[] allLists;

	public static HashMap<String, ListEntry> entriesByName;

	static {
		entriesByName = new HashMap<String, ListEntry>();
		allLists = new HMList[8];
		allLists[0] = new HMList0();
		allLists[1] = new HMList1();
		allLists[2] = null;
		allLists[3] = new HMList3();
		allLists[4] = new HMList4();
		allLists[5] = new HMList5();
		allLists[6] = new HMList6();
		allLists[7] = new HMList7();
	}

	public HashMap<Integer, Object> entriesByRegs;

	HMList() {
		entriesByRegs = new HashMap<Integer, Object>();
	}

	ListEntry nl(int list, int register, int offsetBits, String name, int sizeB, float min, float max,
			String conversion, float factor, String unit, boolean inReading, String help) {
		ListEntry e = new ListEntry(list, name, register, offsetBits, sizeB, min, max, conversion, factor, unit,
				inReading, help);
		// Check if an entry exists for this register
		Object o = entriesByRegs.get(register);
		if (o == null) // The register was not yet present in any entry, put it as single entry object
			entriesByRegs.put(register, e);
		else if (o instanceof ArrayList<?>) // The register was already present in many entries, add the new entry to
			// the list
			((ArrayList<ListEntry>) o).add(e);
		else if (o instanceof ListEntry) { // The register was present as single entry, replace by a list containing the
			// new and the old entry
			ArrayList<ListEntry> lst = new ArrayList<ListEntry>();
			lst.add((ListEntry) o);
			lst.add(e);
			entriesByRegs.put(register, lst);
		}

		o = entriesByName.get(name);
		if (o == null)
			entriesByName.put(name, e);
		return e;
	}

	public static ListEntry getEntryByName(String name) {
		ListEntry entry = entriesByName.get(name);
		return entry;
	}

}
