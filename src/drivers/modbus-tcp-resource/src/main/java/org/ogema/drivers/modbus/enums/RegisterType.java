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
