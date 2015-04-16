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
package org.ogema.driver.modbus;

import java.io.IOException;

import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * @author pau
 * 
 */
public class MultiChannel extends Channel {

	private final int regNum;
	private final int count;
	private ReadMultipleRegistersRequest readRequest;
	private final WriteMultipleRegistersRequest writeRequest;
	private final Register[] writeRegisters;

	public MultiChannel(ChannelLocator locator, String[] splitAddress) {
		super(locator);

		// now parse the rest of the address string
		// "multi:<no of 16bit register>:<first register address in hex>"

		count = Integer.decode(splitAddress[1]).intValue();
		regNum = Integer.decode(splitAddress[2]).intValue();

		readRequest = new ReadMultipleRegistersRequest(regNum, count);

		writeRegisters = new Register[count];

		for (int i = 0; i < count; i++)
			writeRegisters[i] = new SimpleRegister();

		writeRequest = new WriteMultipleRegistersRequest(regNum, writeRegisters);

		readRequest = new ReadMultipleRegistersRequest(regNum, count);
		readRequest.setUnitID(Integer.decode(locator.getDeviceLocator().getDeviceAddress()));
	}

	@Override
	synchronized public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;
		ReadMultipleRegistersResponse response;
		int[] array;
		//int tmp;

		try {
			response = (ReadMultipleRegistersResponse) connection.executeTransaction(readRequest);

			//array = new byte[response.getByteCount()];
			array = new int[response.getWordCount()];

			for (int i = 0; i < array.length; i++) {
				array[i] = response.getRegisterValue(i);
				//				array[i * 2] = (byte) (tmp & 0xFF);
				//				array[i * 2 + 1] = (byte) ((tmp >> 8) & 0xFF);
			}

			//			value = new ByteArrayValue(array);
			value = new ObjectValue(array);
			quality = Quality.GOOD;
		} catch (ModbusSlaveException mse) {
			System.out.println("Slave Exception: " + mse.getType());
			mse.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new SampledValue(value, System.currentTimeMillis(), quality);
	}

	@Override
	synchronized public void writeValue(Connection connection, Value value) throws IOException {

		//byte[] array;
		int[] array;
		WriteMultipleRegistersResponse response;

		try {
			//array = value.getByteArrayValue();
			array = (int[]) value.getObjectValue();

			// now load the values into the registers
			for (int i = 0; i < writeRegisters.length; i++) {
				//writeRegisters[i].setValue((array[i * 2] & 0xFF) | (array[i * 2 + 1] << 8));
				writeRegisters[i].setValue(array[i]);
			}

			// cast response to OK response, an error response fails the cast and throws an exception
			response = (WriteMultipleRegistersResponse) connection.executeTransaction(writeRequest);

		} catch (IllegalConversionException e) {
			throw new IOException("value cannot be accessed as int[]", e);
		} catch (Exception e) {
			throw new IOException("error writing registers", e);
		}
	}

}
