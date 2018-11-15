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
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleCoilsRequest;
import com.ghgande.j2mod.modbus.util.BitVector;

public class CoilChannel extends Channel {

	public static final int MAX_READ = 2000;
	public static final int MAX_WRITE = 1968;
	
	private final ReadCoilsRequest readRequest;
	private final WriteMultipleCoilsRequest writeRequest;

	public CoilChannel(ChannelLocator locator, String[] splitAddress) {
		super(locator);

		int device;
		int reg;
		int count;

		// decode the argument string
		// channelAddressString format:
		// "<DEVICE_ID>:COILS:<REGISTERNUMBER>:<COUNT>:<DATATYPE>"
		try {
			device = Integer.decode(splitAddress[0]).intValue();
			
			if (device < 0 || device > 254)
				throw new IllegalArgumentException();
			
			reg = Integer.decode(splitAddress[2]).intValue();
			
			if (reg < 0 || reg > 0xFFFF)
				throw new IllegalArgumentException();
			
			count = Integer.decode(splitAddress[3]).intValue();

//			if (count < 0 || count > MAX_READ)
//				;
			
		} catch (NullPointerException | IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"could not create Channel with Address "
							+ locator.getChannelAddress(), e);
		}

		readRequest = new ReadCoilsRequest();

		readRequest.setUnitID(device);
		readRequest.setReference(reg);
		readRequest.setBitCount(count);

		writeRequest = new WriteMultipleCoilsRequest();

		writeRequest.setUnitID(device);
		writeRequest.setReference(reg);
		writeRequest.setCoils(new BitVector(count));
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;
		ReadCoilsResponse response;
		int[] array;

		try {
			response = (ReadCoilsResponse) connection
					.executeTransaction(readRequest);

			array = new int[response.getBitCount()];

			for (int i = 0; i < array.length; i++) {
				array[i] = response.getCoilStatus(i) ? 1 : 0;
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

		//WriteMultipleCoilsResponse response;
		int[] array;

		try {
			array = (int[]) value.getObjectValue();

			for (int i = 0; i < array.length; i++) {
				writeRequest.setCoilStatus(i, array[i] != 0);
			}

			/*response = (WriteMultipleCoilsResponse) */connection
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
