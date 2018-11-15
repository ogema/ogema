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

public class ListEntryValue {

	public ListEntry entry;

	public ListEntryValue(ListEntry entry, int value) {
		this.entry = entry;
		this.regValue = value;
		// The initialized value takes as many bits as the value of 'size'
	}

	int regValue;
	private float fValue;
	private int iValue;

	public ListEntry getEntry() {
		return entry;
	}

	public int getRegValue() {
		return regValue;
	}

	public void setRegValue(int regValue) {
		this.regValue = regValue;
	}

	public String getDescription() {
		String setting = null;
		int value = regValue;
		int mask = ((1 << entry.size) - 1) << entry.offsetBits;
		value = value & mask;
		value = value >> entry.offsetBits;
		float fValue = (float) value;
		if (entry.factor > 0) {
			fValue /= entry.factor;
			setting = Float.toString(fValue);
		}
		else {
			setting = Integer.toHexString(value);
		}
		if (entry.canonical == null) {
			entry.canonical = "Name: " + entry.name + "\n\t\tMin: " + entry.min + "\n\t\tMax: " + entry.max
					+ "\n\t\tConversion: " + (entry.conversion.equals("") ? "-" : entry.conversion) + "\n\t\tUnit: "
					+ (entry.unit.equals("") ? "-" : entry.unit) + "\n\t\tDesc: \"" + entry.help;
		}

		if (!entry.conversion.equals(""))
			try {
				setting = HMLookups.getSetting2Value(entry.conversion, value);
			} catch (Exception e) {
				e.printStackTrace();
				setting = e.getClass().getSimpleName();
			}
		return entry.canonical + "\"\n\t\tRegValue: " + regValue + "\n\t\tRegAddr: " + entry.register
				+ "\n\t\tOffset: " + entry.offsetBits + "\n\t\tSize: " + entry.size + "\n\t\tMask: " + mask
				+ "\n\t\tFactor: " + entry.factor + "\n\t\tLookupkey: " + value + "\n\t\tCurrentvalue: " + setting
				+ "\n\t\tlist: " + entry.list + "\n\t\tchannel: " + entry.channel;
	}

	public String getConfigKey(String configName) {
		String setting = null;
		int value = regValue;
		int mask = ((1 << entry.size) - 1) << entry.offsetBits;
		value = value & mask;
		value = value >> entry.offsetBits;
		float fValue = (float) value;
		if (entry.factor > 0) {
			fValue /= entry.factor;
			setting = Float.toString(fValue);
		}
		else {
			setting = Integer.toString(value);
		}
		return setting;
	}

	public String getConfigValue(String configName) {
		String setting = null;
		calcConfigValue();

		if (entry.factor > 0)
			setting = Float.toString(fValue);

		if (!entry.conversion.equals(""))
			try {
				setting = HMLookups.getSetting2Value(entry.conversion, iValue);
			} catch (Exception e) {
				e.printStackTrace();
				setting = e.getClass().getSimpleName();
			}
		else
			setting = Integer.toString(iValue);
		return setting;
	}

	void calcConfigValue() {
		iValue = regValue;
		int mask = ((1 << entry.size) - 1) << entry.offsetBits;
		iValue = iValue & mask;
		iValue = iValue >> entry.offsetBits;
		fValue = (float) iValue;
		if (entry.factor > 0) {
			fValue /= entry.factor;
		}
	}
}
