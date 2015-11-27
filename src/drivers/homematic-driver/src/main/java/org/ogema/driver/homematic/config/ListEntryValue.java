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
