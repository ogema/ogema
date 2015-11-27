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
package org.ogema.core.model.units;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration type for physical units. The IDs are chosen to match those
 * defined in the BACnet data model.
 */
public enum PhysicalUnit {

	SQUARE_METERS("m²"), AMPERES("A"), OHMS("O"), VOLTS("V"), JOULES("J"), HERTZ("Hz"), METERS("m"), DEGREES("°"), KILOGRAMS(
			"kg"), PARTS_PER_MILLION("ppm"), WATTS("W"), LUX("lx"), LUMEN("lm"), KELVIN("K"), JOULE_PER_KELVIN("J/K"), WATT_PER_SQUARE_METER(
			"W/m²"), METERS_PER_SECOND("m/s"), CUBIC_METERS_PER_SECOND("m³/s"), CUBIC_METERS("m³"), UNKNOWN("???");

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
