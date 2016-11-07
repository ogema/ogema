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

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

public class HoldingChannel extends Channel {

	public static final int MAX_READ = 125;
	public static final int MAX_WRITE = 123;

	private final ReadMultipleRegistersRequest readRequest;
	private final WriteMultipleRegistersRequest writeRequest;

	public HoldingChannel(ChannelLocator locator, String[] splitAddress) {
		super(locator);

		int device;
		int reg;
		int count;

		// decode the argument string
		// channelAddressString format:
		// "<DEVICE_ID>:COILS:<REGISTERNUMBER>:<COUNT>"
		try {
			device = Integer.decode(splitAddress[0]).intValue();
			reg = Integer.decode(splitAddress[2]).intValue();
			count = Integer.decode(splitAddress[3]).intValue();

		} catch (NullPointerException | IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"could not create Channel with Address "
							+ locator.getChannelAddress(), e);
		}

		readRequest = new ReadMultipleRegistersRequest();

		readRequest.setUnitID(device);
		readRequest.setReference(reg);
		readRequest.setWordCount(count);

		writeRequest = new WriteMultipleRegistersRequest();

		writeRequest.setUnitID(device);
		writeRequest.setReference(reg);

		Register[] registers = new Register[count];
		for (int i = 0; i < registers.length; i++) {
			registers[i] = new SimpleRegister(0);
		}

		writeRequest.setRegisters(registers);
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;
		ReadMultipleRegistersResponse response;
		int[] array;

		try {
			response = (ReadMultipleRegistersResponse) connection
					.executeTransaction(readRequest);

			array = new int[response.getWordCount()];

			for (int i = 0; i < array.length; i++) {
				array[i] = response.getRegisterValue(i);
			}

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
	public void writeValue(Connection connection, Value value)
			throws IOException {

		//WriteMultipleRegistersResponse response;
		int[] array;
		Register[] registers;

		try {
			array = (int[]) value.getObjectValue();
			registers = writeRequest.getRegisters();
			for (int i = 0; i < array.length; i++) {
				registers[i].setValue(array[i]);
			}

			/*response = (WriteMultipleRegistersResponse)*/ connection
					.executeTransaction(writeRequest);

		} catch (ModbusSlaveException mse) {
			//System.out.println("Slave Exception: " + mse.getType());
			//mse.printStackTrace();
			throw new IOException("Slave Exception: " + mse.getType(), mse);
		} catch (Exception e) {
			//e.printStackTrace();
			throw new IOException(e);
		}
	}

}
