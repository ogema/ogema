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
/**
 * 
 */
package org.ogema.util;

import java.util.Set;

import java.util.Map;
import java.util.Map.Entry;

import org.ogema.core.application.AppID;

/**
 * @author mns
 * 
 */
public class Util {

	public static final ThreadLocal<AppID> currentAppThreadLocale = new ThreadLocal<>();

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
			value = e.getValue();
			if (value.charAt(0) != '/')
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

	public static String getCurrentAppStoragePath() {
		String result;
		AppID app = currentAppThreadLocale.get();
		result = app.getBundle().getDataFile("").getAbsolutePath();
		return result;
	}

	public static String bytes2decString(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i != data.length; i++) {
			int v = data[i] & 0xff;
			buf.append(v);
			buf.append(' ');
		}
		return buf.toString();
	}

	private static String digits = "0123456789abcdef".intern();

	public static String bytes2hexString(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i != data.length; i++) {
			int v = data[i] & 0xff;
			buf.append(digits.charAt(v >> 4));
			buf.append(digits.charAt(v & 0xf));
			buf.append(' ');
		}
		return buf.toString();
	}
}
