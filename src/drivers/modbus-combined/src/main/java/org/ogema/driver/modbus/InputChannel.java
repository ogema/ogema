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
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;

public class InputChannel extends Channel {

	public static final int MAX_READ = 125;
	
	private final ReadInputRegistersRequest readRequest;

	public InputChannel(ChannelLocator locator, String[] splitAddress) {
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

		readRequest = new ReadInputRegistersRequest();

		readRequest.setUnitID(device);
		readRequest.setReference(reg);
		readRequest.setWordCount(count);
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;
		ReadInputRegistersResponse response;
		int[] array;

		try {
			response = (ReadInputRegistersResponse) connection
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
		throw new IOException("MODBUS holding input registers are read-only.");
	}

}
