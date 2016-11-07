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
package org.ogema.drivers.modbus.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.drivers.modbus.enums.DataType;
import org.ogema.drivers.modbus.tasks.ModbusTask;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * 
 * Util class for handling modbus register (example: get registers values).
 * 
 * @author brequardt
 */
public class ModbusDriverUtil {

	public Value getBitVectorsValue(BitVector bitVector) {

		Value readValue;
		if (bitVector.size() == 1) {

			readValue = new BooleanValue(bitVector.getBit(0));
		} else {
			readValue = new ByteArrayValue(bitVector.getBytes());
		}
		return readValue;
	}

	public BitVector getBitVectorFromByteArray(Value value) {
		BitVector bv = new BitVector(value.getByteArrayValue().length * 8);
		bv.setBytes(value.getByteArrayValue());
		return bv;
	}
	
	public Value getRegistersValue(InputRegister[] registers, ModbusTask channel) {
		return getRegistersValue(registers, channel, false);
	}

	public Value getRegistersValue(InputRegister[] registers, ModbusTask channel, boolean useLittleEndian) {
		Value registerValue = null;

		ByteBuffer bb = inputRegisterToByteBuffer(registers, channel, useLittleEndian);

		switch (channel.getDataType()) {

		case BOOLEAN:
			registerValue = new BooleanValue(bb.getShort(0) > 0 ? true : false);
			break;
		case SHORT:
			registerValue = new IntegerValue(bb.getShort(0));
			break;
		case INT:
			registerValue = new IntegerValue(bb.getInt(0));
			break;
		case FLOAT:
			registerValue = new FloatValue(bb.getFloat(0));
			break;
		case DOUBLE:
			registerValue = new DoubleValue(bb.getDouble(0));
			break;
		case LONG:
			registerValue = new LongValue(bb.getLong(0));
			break;
		case BYTEARRAY:
			bb.rewind();
			byte[] arr =  new byte[bb.remaining()];
			bb.get(arr);
			registerValue = new ByteArrayValue(arr);
			break;
		case STRING:
			bb.rewind(); 
			byte[] arr2 =  new byte[bb.remaining()];
			bb.get(arr2);
			registerValue = new StringValue(new String(arr2,StandardCharsets.US_ASCII));
			break;
		default:
			throw new RuntimeException("Datatype "
					+ channel.getDataType().toString() + " not supported yet");
		}

		return registerValue;
	}
	
	public Register[] valueToRegisters(Value value, DataType datatype, int count) {
		return valueToRegisters(value, datatype, count, false);
	}

	public Register[] valueToRegisters(Value value, DataType datatype, int count, boolean useLittelEndian) {

		Register[] registers;

//		ByteBuffer bb = ByteBuffer.allocate(datatype.getSize()); // does not account for ByteArray and String values
		ByteBuffer bb = ByteBuffer.allocate(count * 2);
		bb.order(ByteOrder.BIG_ENDIAN);

		switch (datatype) {

		case BOOLEAN:
			bb.putShort((short) (value.getBooleanValue() ? 1 : 0));
			registers = byteBufferToRegister(bb, count, useLittelEndian);
		case INT:
			bb.putInt(value.getIntegerValue());
			registers = byteBufferToRegister(bb, count, useLittelEndian);
			break;
		case DOUBLE:
			bb.putDouble(value.getDoubleValue());
			registers = byteBufferToRegister(bb, count, useLittelEndian);
			break;
		case FLOAT:
			bb.putFloat(value.getFloatValue());
			registers = byteBufferToRegister(bb, count, useLittelEndian);
			break;
		case LONG:
			bb.putLong(value.getLongValue());
			registers = byteBufferToRegister(bb, count, useLittelEndian);
			break;
		case BYTEARRAY:
			bb.put(value.getByteArrayValue());
			registers = byteBufferToRegister(bb, count, useLittelEndian);
			break;
		case STRING:
			bb.put(value.getStringValue().getBytes(StandardCharsets.US_ASCII));
			registers = byteBufferToRegister(bb, count, useLittelEndian);
			break;
		default:
			throw new RuntimeException("Datatype " + datatype.toString()
					+ " not supported yet");
		}

		return registers;
	}
	
	private ByteBuffer inputRegisterToByteBuffer(InputRegister[] inputRegister,
			ModbusTask modbusTask) {
		return inputRegisterToByteBuffer(inputRegister, modbusTask, false);
	}

	private ByteBuffer inputRegisterToByteBuffer(InputRegister[] inputRegister,
			ModbusTask modbusTask, boolean useLittleEndian) {

//		int size = modbusTask.getDataType().getSize();
		DataType type = modbusTask.getDataType();
		int size;
		if (type == DataType.BYTEARRAY || type == DataType.STRING) {
			size = modbusTask.getCount() * 2; // nr of registers x 2 = nr of bytes
		} else {
			size = type.getSize();
		}
		ByteBuffer b = ByteBuffer.allocate(size);

		b.order(ByteOrder.BIG_ENDIAN);

		for (int i = 0; i < inputRegister.length; i++) {

			// speicher nicht überläuft
			if (b.capacity() >= (i + 1) * 2) {

				int j = i;
				if (useLittleEndian)
					j = inputRegister.length - i - 1;
				b.put(inputRegister[j].toBytes());
			}
		}

		return b;

	}
	
	private Register[] byteBufferToRegister(ByteBuffer bb, int count)
			throws RuntimeException {
		return byteBufferToRegister(bb, count, false);
	}

	private Register[] byteBufferToRegister(ByteBuffer bb, int count, boolean useLittleEndian)
			throws RuntimeException {

		byte[] barray = bb.array();

		SimpleRegister[] register = new SimpleRegister[count];
		int j = 0;
		for (int i = 0; i < barray.length; i = i + 2) {

			byte b1 = barray[i];
			byte b2 = barray[i + 1];
			int k = j;
			if (useLittleEndian)
				k = count - 1 - j;
			if (register.length > k) {
				register[k] = new SimpleRegister(b1, b2);
			}
			j++;
		}

		return register;
	}

}
