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
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

public class HoldingChannel extends Channel {

	public static final int MAX_READ = 125;
	public static final int MAX_WRITE = 123;

	private final ReadMultipleRegistersRequest readRequest;
	private WriteMultipleRegistersRequest writeMultipleRequest;
	private WriteSingleRegisterRequest writeSingleRequest;
	private int registerCount;

	public HoldingChannel(ChannelLocator locator, String[] splitAddress) {
		super(locator);

		int device;
		int reg;
		// decode the argument string
		// channelAddressString format:
		// "<DEVICE_ID>:COILS:<REGISTERNUMBER>:<COUNT>"
		try {
			device = Integer.decode(splitAddress[0]).intValue();
			reg = Integer.decode(splitAddress[2]).intValue();
			registerCount = Integer.decode(splitAddress[3]).intValue();

		} catch (NullPointerException | IllegalArgumentException e) {
			throw new IllegalArgumentException("could not create Channel with Address " + locator.getChannelAddress(),
					e);
		}

		readRequest = new ReadMultipleRegistersRequest();

		readRequest.setUnitID(device);
		readRequest.setReference(reg);
		readRequest.setWordCount(registerCount);
		if (registerCount > 1) {
			writeMultipleRequest = new WriteMultipleRegistersRequest();

			writeMultipleRequest.setUnitID(device);
			writeMultipleRequest.setReference(reg);

			Register[] registers = new Register[registerCount];
			for (int i = 0; i < registers.length; i++) {
				registers[i] = new SimpleRegister(0);
			}

			writeMultipleRequest.setRegisters(registers);
		}
		else if (registerCount == 1) {
			writeSingleRequest = new WriteSingleRegisterRequest();

			writeSingleRequest.setUnitID(device);
			writeSingleRequest.setReference(reg);

			Register register = new SimpleRegister(0);
			writeSingleRequest.setRegister(register);
		}
		else {
			// Should never happen, that a negative register count is encoded in the channel descriptor
			throw new IllegalArgumentException(
					"could not create Channel with this address due to negative register count: "
							+ locator.getChannelAddress());
		}
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;
		ReadMultipleRegistersResponse response;
		int[] array;

		try {
			response = (ReadMultipleRegistersResponse) connection.executeTransaction(readRequest);

			array = new int[response.getWordCount()];

			for (int i = 0; i < array.length; i++) {
				array[i] = response.getRegisterValue(i);
				ModbusDriver.logger.trace("Register_Value: " + array[i]);
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
	public void writeValue(Connection connection, Value value) throws IOException {
		Object ov = value.getObjectValue();

		try {
			if (registerCount == 1) { // is the value a single integer or an array of it?
				int singleInt = (Integer) ov;
				Register register = writeSingleRequest.getRegister();
				register.setValue(singleInt);
				connection.executeTransaction(writeSingleRequest);
			}
			else {
				int[] array;
				Register[] registers;
				array = (int[]) value.getObjectValue();
				registers = writeMultipleRequest.getRegisters();
				for (int i = 0; i < array.length; i++) {
					registers[i].setValue(array[i]);
				}
				connection.executeTransaction(writeMultipleRequest);
			}

		} catch (ModbusSlaveException mse) {
			throw new IOException("Slave Exception: " + mse.getType(), mse);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
