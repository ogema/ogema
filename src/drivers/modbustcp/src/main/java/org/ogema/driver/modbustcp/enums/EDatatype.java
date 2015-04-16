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
package org.ogema.driver.modbustcp.enums;

public enum EDatatype {

	BOOLEAN("boolean", 1), // 1 Bit
	SHORT("short", 1), // 1 Register
	INT("int", 2), // 2 Registers
	FLOAT("float", 2), // 2 Registers
	DOUBLE("double", 4), // 4 Registers
	LONG("long", 4), // 4 Registers
	BYTEARRAY("bytearray", 0), // registerCount is calculated dynamically, the 0 will be overwritten
	STRING("string", 0); // registerCount is calculated dynamically, the 0 will be overwritten

	private final String datatype;
	private final int registerCount;

	EDatatype(String datatypeAsString, int registerSize) {
		datatype = datatypeAsString;
		registerCount = registerSize;
	}

	public int getRegisterCount() {
		return registerCount;
	}

	@Override
	public String toString() {
		return datatype;
	}

	/**
	 * @param enumAsString
	 * @return the EDatatype
	 * @throws Exception
	 */
	public static EDatatype getEnumFromString(String enumAsString) {
		EDatatype returnValue = null;

		if (enumAsString != null) {

			for (EDatatype type : EDatatype.values()) {
				if (enumAsString.equals(type.datatype)) {
					returnValue = EDatatype.valueOf(enumAsString.toUpperCase());
					break;
				}
				else if (enumAsString.toUpperCase().matches("BYTEARRAY\\[\\d+\\]")) {
					// Special check for BYTEARRAY[n] datatyp
					returnValue = EDatatype.BYTEARRAY;
					break;
				}
				else if (enumAsString.toUpperCase().matches("STRING\\[\\d+\\]")) {
					// Special check for STRING[n] datatyp
					returnValue = EDatatype.STRING;
					break;
				}
			}
		}

		if (returnValue == null) {
			throw new RuntimeException(enumAsString
					+ " is not supported. Use one of the following supported datatypes: " + getSupportedDatatypes());
		}

		return returnValue;

	}

	/**
	 * @return all supported datatypes
	 */
	public static String getSupportedDatatypes() {

		String supported = "";

		for (EDatatype type : EDatatype.values()) {
			supported += type.toString() + ", ";
		}

		return supported;
	}

	/**
	 * Checks if the datatype is valid
	 * 
	 * @param enumAsString
	 * @return true if valid, otherwise false
	 */
	public static boolean isValidDatatype(String enumAsString) {
		boolean returnValue = false;

		for (EDatatype type : EDatatype.values()) {

			if (type.toString().toLowerCase().equals(enumAsString.toLowerCase())) {
				returnValue = true;
				break;
			}
			else if (enumAsString.toUpperCase().matches("BYTEARRAY\\[\\d+\\]")) {
				// Special check for BYTEARRAY[n] datatyp
				returnValue = true;
				break;
			}
			else if (enumAsString.toUpperCase().matches("STRING\\[\\d+\\]")) {
				// Special check for STRING[n] datatyp
				returnValue = true;
				break;
			}
		}

		return returnValue;
	}

}
