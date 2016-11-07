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
