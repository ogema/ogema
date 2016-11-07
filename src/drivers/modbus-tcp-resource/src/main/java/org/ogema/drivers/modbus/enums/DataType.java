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

public enum DataType {

	BOOLEAN(2), SHORT(2), INT(4), FLOAT(4), DOUBLE(8), LONG(8), BYTEARRAY(0), STRING(0);

	private int size;

	private DataType(int size) {
		this.setSize(size);
	}

	public static DataType getEnumFromString(String enumAsString) {

		DataType returnValue = valueOf(enumAsString);

		if (returnValue == null) {
			throw new RuntimeException(
					enumAsString
							+ " is not supported. Use one of the following supported datatypes: "
							+ getSupportedDatatypes());
		}

		return returnValue;

	}

	private static String getSupportedDatatypes() {

		String supported = "";

		for (DataType type : DataType.values()) {
			supported += type.toString() + ", ";
		}

		return supported;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
