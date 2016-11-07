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
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;

public class DiscreteChannel extends Channel {

	public static final int MAX_READ = 2000;

	private final ReadInputDiscretesRequest readRequest;

	public DiscreteChannel(ChannelLocator locator, String[] splitAddress) {
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

		readRequest = new ReadInputDiscretesRequest();

		readRequest.setUnitID(device);
		readRequest.setReference(reg);
		readRequest.setBitCount(count);
	}

	@Override
	public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;
		ReadInputDiscretesResponse response;
		int[] array;

		try {
			response = (ReadInputDiscretesResponse) connection
					.executeTransaction(readRequest);

			array = new int[response.getBitCount()];

			for (int i = 0; i < array.length; i++) {
				array[i] = response.getDiscreteStatus(i) ? 1 : 0;
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
		throw new IOException("MODBUS discrete input registers are read-only.");
	}

}
