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
