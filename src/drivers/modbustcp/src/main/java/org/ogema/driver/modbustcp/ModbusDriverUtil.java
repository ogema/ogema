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
package org.ogema.driver.modbustcp;

import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.BitVector;
import net.wimpi.modbus.util.ModbusUtil;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.modbustcp.enums.EDatatype;

public class ModbusDriverUtil {

	public Value getBitVectorsValue(BitVector bitVector) {

		Value readValue;
		if (bitVector.size() == 1) {

			readValue = new BooleanValue(bitVector.getBit(0)); // read single bit
		}
		else {
			readValue = new ByteArrayValue(bitVector.getBytes()); // read multiple bits
		}
		return readValue;
	}

	public BitVector getBitVectorFromByteArray(Value value) {
		BitVector bv = new BitVector(value.getByteArrayValue().length * 8);
		bv.setBytes(value.getByteArrayValue());
		return bv;
	}

	/**
	 * Converts the registers into the datatyp of the channel
	 * 
	 * @param registers
	 * @param datatype
	 * @return the corresponding Value Object
	 * @throws Exception
	 */
	public Value getRegistersValue(InputRegister[] registers, ModbusChannel channel) {
		Value registerValue = null;
		byte[] registerAsByteArray = inputRegisterToByteArray(registers, channel);

		switch (channel.getDatatype()) {
		case SHORT:
			registerValue = new IntegerValue(ModbusUtil.registerToShort(registerAsByteArray));
			break;
		case INT:
			registerValue = new IntegerValue(ModbusUtil.registersToInt(registerAsByteArray));
			break;
		case FLOAT:
			registerValue = new FloatValue(ModbusUtil.registersToFloat(registerAsByteArray));
			break;
		case DOUBLE:
			registerValue = new DoubleValue(ModbusUtil.registersToDouble(registerAsByteArray));
			break;
		case LONG:
			registerValue = new LongValue(ModbusUtil.registersToLong(registerAsByteArray));
			break;
		case BYTEARRAY:
			registerValue = new ByteArrayValue(registerAsByteArray);
			break;
		case STRING:
			registerValue = new StringValue(new String(registerAsByteArray));
			break;
		default:
			throw new RuntimeException("Datatype " + channel.getDatatype().toString() + " not supported yet");
		}
		return registerValue;
	}

	public Register[] valueToRegisters(Value value, EDatatype datatype) {

		Register[] registers;

		switch (datatype) {

		/*
		 * case SHORT: registers = byteArrayToRegister(ModbusUtil.shortToRegister(value.asShort())); break;
		 */
		case INT:
			registers = byteArrayToRegister(ModbusUtil.intToRegisters(value.getIntegerValue()));
			break;
		case DOUBLE:
			registers = byteArrayToRegister(ModbusUtil.doubleToRegisters(value.getDoubleValue()));
			break;
		case FLOAT:
			registers = byteArrayToRegister(ModbusUtil.floatToRegisters(value.getFloatValue()));
			break;
		case LONG:
			registers = byteArrayToRegister(ModbusUtil.longToRegisters(value.getLongValue()));
			break;
		case BYTEARRAY:
			registers = byteArrayToRegister(value.getByteArrayValue());
			break;
		default:
			throw new RuntimeException("Datatype " + datatype.toString() + " not supported yet");
		}

		return registers;
	}

	/**
	 * Converts an array of input registers into a byte array
	 * 
	 * @param inputRegister
	 * @return the InputRegister[] as byte[]
	 */
	private byte[] inputRegisterToByteArray(InputRegister[] inputRegister, ModbusChannel channel) {

		byte[] registerAsBytes = new byte[(inputRegister.length * 2)]; // one register = 2 bytes
		for (int i = 0; i < inputRegister.length; i++) {
			System.arraycopy(inputRegister[i].toBytes(), 0, registerAsBytes, i * inputRegister[0].toBytes().length,
					inputRegister[i].toBytes().length);
		}

		if (channel.getDatatype().equals(EDatatype.BYTEARRAY) || channel.getDatatype().equals(EDatatype.STRING)) {

			if (!channel.isEvenNumber()) {

				byte[] reg = new byte[registerAsBytes.length - 1];

				for (int i = 0; i < registerAsBytes.length - 1; i++) {
					reg[i] = registerAsBytes[i];
				}

				return reg;

			}

		}

		return registerAsBytes;

	}

	// TODO check byte order e.g. is an Integer!
	// TODO only works for even byteArray.length!
	private Register[] byteArrayToRegister(byte[] byteArray) throws RuntimeException {

		// TODO byteArray might has a odd number of bytes...
		SimpleRegister[] register;

		if (byteArray.length % 2 == 0) {
			register = new SimpleRegister[byteArray.length / 2];
			int j = 0;
			for (int i = 0; i < register.length; i++) {
				register[i] = new SimpleRegister(byteArray[j], byteArray[j + 1]);
				j = j + 2;
			}
		}
		else {
			throw new RuntimeException("conversion vom byteArray to Register is not working for odd number of bytes");
		}
		return register;
	}

}
