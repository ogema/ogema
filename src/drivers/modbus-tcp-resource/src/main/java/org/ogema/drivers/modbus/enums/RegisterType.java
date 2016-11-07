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
package org.ogema.drivers.modbus.enums;

import java.util.Arrays;

/**
 * Modbus defines four different address areas called primary tables.
 */
public enum RegisterType {

	COILS, DISCRETE_INPUTS, INPUT_REGISTERS, HOLDING_REGISTERS;

	public static RegisterType getEnumfromString(String enumAsString) {
		RegisterType returnValue = null;
		if (enumAsString != null) {
			for (RegisterType value : RegisterType.values()) {
				if (enumAsString.toUpperCase().equals(value.toString())) {
					returnValue = RegisterType.valueOf(enumAsString
							.toUpperCase());
					break;
				}
			}
		}
		if (returnValue == null) {
			throw new RuntimeException(
					enumAsString
							+ " is not supported. Use one of the following supported primary tables: "
							+ Arrays.toString(RegisterType.values()));
		}
		return returnValue;
	}

}
