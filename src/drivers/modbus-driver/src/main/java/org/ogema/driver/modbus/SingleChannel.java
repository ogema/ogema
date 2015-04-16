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
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * @author pau
 * 
 */
public class SingleChannel extends Channel {

	private final int regNum;
	private final ReadInputRegistersRequest readRequest;
	private final WriteSingleRegisterRequest writeRequest;
	private final Register writeRegister;

	// private ModbusSerialTransaction channel;

	// protected SingleChannel(ChannelLocator locator) {
	// super(locator);
	// // TODO Auto-generated constructor stub
	// }

	public SingleChannel(ChannelLocator locator, String[] splitAddress) {
		super(locator);

		// now parse the rest of the address string
		// "reg:<hex address>"
		regNum = Integer.decode(splitAddress[1]).intValue();
		readRequest = new ReadInputRegistersRequest(regNum, 1);

		writeRegister = new SimpleRegister();
		writeRequest = new WriteSingleRegisterRequest(regNum, writeRegister);
	}

	@Override
	synchronized public SampledValue readValue(Connection connection) throws IOException {
		Value value = null;
		Quality quality = Quality.BAD;

		try {
			ReadInputRegistersResponse response = (ReadInputRegistersResponse) connection
					.executeTransaction(readRequest);
			value = new IntegerValue(response.getRegisterValue(0));
			quality = Quality.GOOD;
		} catch (ModbusSlaveException mse) {
			throw new IOException("Slave Exception: " + mse.getType(), mse);
		} catch (Exception e) {
			throw new IOException("error reading register", e);
		}

		return new SampledValue(value, System.currentTimeMillis(), quality);
	}

	@Override
	synchronized public void writeValue(Connection connection, Value value) throws IOException {
		WriteSingleRegisterResponse response;
		try {
			writeRegister.setValue(value.getIntegerValue());
			response = (WriteSingleRegisterResponse) connection.executeTransaction(writeRequest);
		} catch (IllegalConversionException e) {
			throw new IOException("Value cannot be accessed as integer", e);
		} catch (Exception e) {
			throw new IOException("error writing register", e);
		}

	}

}
