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
package org.ogema.driver.homematic.config;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the super class for all device specific configuration classes. It holds the raw data of all supported
 * registers in all lists. They are initialized at the first full configuration reading and updated after each change
 * trough a configuration write operation.
 * 
 * @author mns
 *
 */
public abstract class HMDevConfI {

	public abstract HashMap<String, ListEntryValue> getDevConfigs();

	/**
	 * Raw data provided by the device consisting of &lt;register, value&gt; pairs.
	 */
	public HashMap<Integer, Integer> rawRegValues0;
	public HashMap<Integer, Integer> rawRegValues1;
	public HashMap<Integer, Integer> rawRegValues3;
	public HashMap<Integer, Integer> rawRegValues4;
	public HashMap<Integer, Integer> rawRegValues5;
	public HashMap<Integer, Integer> rawRegValues6;
	public HashMap<Integer, Integer> rawRegValues7;

	HashMap<String, ListEntryValue> deviceConfigs;

	static final ListEntryValue unInitedEntry = new ListEntryValue(null, 0);

	public Map<Integer, Integer> getRegValues(int list) {
		Map<Integer, Integer> result = null;
		switch (list) {
		case 0:
			if (rawRegValues0 == null)
				rawRegValues0 = new HashMap<Integer, Integer>();
			result = rawRegValues0;
			break;
		case 1:
			if (rawRegValues1 == null)
				rawRegValues1 = new HashMap<Integer, Integer>();
			result = rawRegValues1;
			break;
		case 3:
			if (rawRegValues3 == null)
				rawRegValues3 = new HashMap<Integer, Integer>();
			result = rawRegValues3;
			break;
		case 4:
			if (rawRegValues4 == null)
				rawRegValues4 = new HashMap<Integer, Integer>();
			result = rawRegValues4;
			break;
		case 5:
			if (rawRegValues5 == null)
				rawRegValues5 = new HashMap<Integer, Integer>();
			result = rawRegValues5;
			break;
		case 6:
			if (rawRegValues6 == null)
				rawRegValues6 = new HashMap<Integer, Integer>();
			result = rawRegValues6;
			break;
		case 7:
			if (rawRegValues7 == null)
				rawRegValues7 = new HashMap<Integer, Integer>();
			result = rawRegValues7;
			break;
		}
		return result;
	}

	public ListEntryValue getEntryValue(ListEntry e) {
		ListEntryValue lev = deviceConfigs.get(e.name);
		if (lev == unInitedEntry) {
			lev = new ListEntryValue(null, -1);
			deviceConfigs.put(e.name, lev);
			lev.entry = e;
		}
		return lev;
	}
}
