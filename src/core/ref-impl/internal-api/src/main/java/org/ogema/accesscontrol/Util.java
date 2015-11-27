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
/**
 * 
 */
package org.ogema.accesscontrol;

import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author mns
 * 
 */
public class Util {

	/*
	 * Checks if outer contains all the elements of inner.
	 */
	public static boolean containsAll(Object[] outer, Object[] inner) {

		int i = 0;
		// boolean success = false;
		while (true) {
			try {
				Object dpi = inner[i++];
				int j = 0;
				while (true) {
					try {
						Object rpi = outer[j++];
						if (dpi.equals(rpi)) {
							// success = true;
							break;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						// if (!success)
						return false;
						// else
						// break;
					}

				}
				// if (success)
				// break;
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
		return true;
	}

	/*
	 * Checks if the outer array contains an element that equals to inner.
	 */
	public static boolean contains(Object[] outer, Object inner) {

		boolean success = false;
		Object dpi = inner;
		int j = 0;
		while (true) {
			try {
				Object rpi = outer[j++];
				if (dpi.equals(rpi)) {
					success = true;
					break;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}

		}
		return success;
	}

	/*
	 * Gets the first occurrence of an element in outer that starts with inner.
	 */
	public static int startsWithAny(String[] outer, String inner) {

		int success = -1;
		String dpi = inner;
		int j = 0;
		while (true) {
			try {
				String rpi = outer[j];
				if (dpi.startsWith(rpi)) {
					success = j;
					break;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			j++;

		}
		return success;
	}

	public static String startsWithAnyValue(Map<String, String> map, String name) {
		String key = null, value = null;
		Set<Entry<String, String>> entries = map.entrySet();
		for (Entry<String, String> e : entries) {
			value = e.getValue() + "/";
			if (name.startsWith(value)) {
				key = e.getKey();
				break;
			}
		}
		return key;
	}

	public static String startsWithAnyKey(Map<String, String> map, String name) {
		String value = null, key = null;
		Set<Entry<String, String>> entries = map.entrySet();
		for (Entry<String, String> e : entries) {
			key = e.getKey() + "/";
			if (name.startsWith(key)) {
				value = e.getValue();
				break;
			}
		}
		return value;
	}

	public static boolean intersect(String[] fActArr, String[] gActArr) {
		int f = 0, g = 0;
		while (true) {
			try {
				String fstr = fActArr[f++];
				while (true) {
					try {
						String gstr = gActArr[g++];
						if (fstr.equals("*") || gstr.equals("*") || fstr.equals(gstr))
							return true;
					} catch (ArrayIndexOutOfBoundsException e) {
						break;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
		return false;
	}

}
