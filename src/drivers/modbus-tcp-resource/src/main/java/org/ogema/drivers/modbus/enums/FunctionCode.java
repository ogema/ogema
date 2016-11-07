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

/**
 * Supported modbuss function codes
 */
public enum FunctionCode {

	FC_01_READ_COILS(1), FC_02_READ_DISCRETE_INPUTS(2), FC_03_READ_HOLDING_REGISTERS(
			3), FC_04_READ_INPUT_REGISTERS(4), FC_05_WRITE_SINGLE_COIL(5), FC_06_WRITE_SINGLE_REGISTER(
			6), FC_15_WRITE_MULITPLE_COILS(15), FC_16_WRITE_MULTIPLE_REGISTERS(
			18);

	private int value;

	private FunctionCode(int value) {
		this.setValue(value);
	}

	public static FunctionCode fromValue(int value)
			throws EnumConstantNotPresentException {

		if (value == 1) {
			return FC_01_READ_COILS;
		} else if (value == 2) {
			return FC_02_READ_DISCRETE_INPUTS;
		} else if (value == 3) {
			return FC_03_READ_HOLDING_REGISTERS;
		} else if (value == 4) {
			return FC_04_READ_INPUT_REGISTERS;
		} else if (value == 5) {
			return FC_05_WRITE_SINGLE_COIL;
		} else if (value == 6) {
			return FC_06_WRITE_SINGLE_REGISTER;
		} else if (value == 15) {
			return FC_15_WRITE_MULITPLE_COILS;
		} else if (value == 16) {
			return FC_16_WRITE_MULTIPLE_REGISTERS;
		} else {
			throw new EnumConstantNotPresentException(FunctionCode.class,
					"Modbus function not supported");
		}

	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public static FunctionCode getFunctionCodeFromRegisterType(boolean read,
			RegisterType type, DataType dataType) throws RuntimeException {
		return getFunctionCodeFromRegisterType(read, type, dataType, 1);
	}

	/**
	 * 
	 * @param read
	 * @param type
	 * @param dataType
	 * @param count
	 * 		only evaluated if it is required but cannot be derived from the dataType, i.e. for write actions
	 * 		for String and Bytearray types 
	 * @return
	 * @throws RuntimeException
	 */
	public static FunctionCode getFunctionCodeFromRegisterType(boolean read,
			RegisterType type, DataType dataType, int count) throws RuntimeException {

		if (read) {

			if (type.equals(RegisterType.COILS)
					&& dataType.equals(DataType.BOOLEAN)) {
				return FC_01_READ_COILS;
			}

			if (type.equals(RegisterType.DISCRETE_INPUTS)
					&& dataType.equals(DataType.BOOLEAN)) {
				return FC_02_READ_DISCRETE_INPUTS;
			}

			if (type.equals(RegisterType.HOLDING_REGISTERS)) {
				return FC_03_READ_HOLDING_REGISTERS;
			}

			if (type.equals(RegisterType.INPUT_REGISTERS)) {
				return FC_04_READ_INPUT_REGISTERS;
			} else {

				throw new RuntimeException(
						"Invalid channel address parameter combination for reading. \n Datatype: "
								+ dataType.toString().toUpperCase()
								+ " PrimaryTable: "
								+ type.toString().toUpperCase());
			}

		} else {
			if (type.equals(RegisterType.COILS)
					&& dataType.equals(DataType.BOOLEAN)) {
				return FC_05_WRITE_SINGLE_COIL;
			}

			if (type.equals(RegisterType.DISCRETE_INPUTS)) {
				throw new RuntimeException(
						"not allowed to write in discrete inputs");
			}

			if (type.equals(RegisterType.HOLDING_REGISTERS)) {
				if (dataType.equals(DataType.DOUBLE)
							|| dataType.equals(DataType.FLOAT) 
							|| dataType.equals(DataType.LONG)
							|| dataType.equals(DataType.INT) 
							|| count > 1) {  // String & Bytearray
					return FC_16_WRITE_MULTIPLE_REGISTERS;
				} else { // single register String & Bytearray, SHORT
					return FC_06_WRITE_SINGLE_REGISTER; 
				}
				
			}

			if (type.equals(RegisterType.INPUT_REGISTERS)  // FIXME Input registers cannot be written(?)
					&& (dataType.equals(DataType.SHORT) || dataType
							.equals(DataType.BOOLEAN))) {

				return FC_06_WRITE_SINGLE_REGISTER;
			}

			if (type.equals(RegisterType.INPUT_REGISTERS) // FIXME Input registers cannot be written(?)
					&& (dataType.equals(DataType.DOUBLE)
							|| dataType.equals(DataType.FLOAT) || dataType
								.equals(DataType.LONG))
					|| dataType.equals(DataType.INT)) {

				return FC_16_WRITE_MULTIPLE_REGISTERS;
			}

			if (type.equals(RegisterType.COILS) // FIXME Coils should always be booleans(?)
					&& (dataType.equals(DataType.DOUBLE)
							|| dataType.equals(DataType.FLOAT) || dataType
								.equals(DataType.LONG))
					|| dataType.equals(DataType.INT)) {

				return FC_15_WRITE_MULITPLE_COILS;
			}

			else {

				throw new RuntimeException(
						"Invalid channel address parameter combination for writing. \n Datatype: "
								+ dataType.toString().toUpperCase()
								+ " Registertype: "
								+ type.toString().toUpperCase());

			}
		}
	}

}
