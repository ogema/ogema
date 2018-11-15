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
