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

public class ListEntry {

	public ListEntry(int list, String name, int register, int offsetBits, int size, float min, float max,
			String conversion, float factor, String unit, boolean inReading, String description) {
		this.list = list;
		this.name = name;
		this.register = register;
		this.offsetBits = offsetBits;
		this.size = size;
		this.min = min;
		this.max = max;
		this.conversion = conversion;
		this.factor = factor;
		this.unit = unit;
		this.inReading = inReading;
		this.help = description;
	}

	public String name;
	public int register;
	public int offsetBits;
	public int size;
	public int channel;
	public int list;
	public float min;
	public float max;
	public String conversion;
	public float factor;
	public String unit;
	boolean inReading;
	public String help;
	String canonical;

	public int getBytesCnt() {
		int bits = offsetBits + size;
		int oddBits = bits % 8;
		int byteCount = bits >> 3;
		if (oddBits != 0)
			byteCount++;

		return byteCount;
	}
}
