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
package org.ogema.core.model.units;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration type for physical units. The IDs are chosen to match those defined in the BACnet data model.
 */
public enum PhysicalUnit {

	SQUARE_METERS("m²"), AMPERES("A"), OHMS("O"), VOLTS("V"), JOULES("J"), HERTZ("Hz"), METERS("m"), DEGREES(
			"°"), KILOGRAMS("kg"), PARTS_PER_MILLION("ppm"), WATTS("W"), LUX("lx"), LUMEN("lm"), KELVIN(
					"K"), JOULE_PER_KELVIN("J/K"), WATT_PER_SQUARE_METER("W/m²"), METERS_PER_SECOND(
							"m/s"), CUBIC_METERS_PER_SECOND("m³/s"), CUBIC_METERS("m³"), UNKNOWN("???"), PERCENT("%");

	static final Map<String, PhysicalUnit> stringToEnum;

	static {
		stringToEnum = new HashMap<String, PhysicalUnit>();
		for (PhysicalUnit u : PhysicalUnit.values()) {
			stringToEnum.put(u.toString(), u);
		}
	}

	private final String name;

	PhysicalUnit(String name) {
		this.name = name;
	}

	@Override
	public final String toString() {
		return name;
	}

	public static final PhysicalUnit fromString(String unit) {
		return stringToEnum.get(unit);
	}
}
