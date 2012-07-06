/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.drivers.modbus.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.driverspi.ValueContainer;
import org.ogema.driver.modbustcp.ModbusDriver;

/**
 * This is a junit test class for a modbus/tcp connection. The used device is a Wago 750-842 and all registers and their
 * default values can be found in the official manuals
 * 
 * @author pweiss
 * 
 */

public class ModbusDriverTestWago {

	/**
	 * required list to add channel objects
	 */
	List<ChannelLocator> locators = new ArrayList<ChannelLocator>();

	/**
	 * required list to invoke the readChannels function
	 */
	List<SampledValueContainer> channels = new ArrayList<SampledValueContainer>();

	/**
	 * required list to write values
	 */
	List<ValueContainer> writingContainer = new ArrayList<ValueContainer>();

	/**
	 * required device object to establish the connection
	 */
	DeviceLocator device = new TestDeviceLocator("modbus-tcp", "", "192.168.2.129", null);

	@Test
	public void testReadChannelsListOfSampledValueContainer() {

		/**
		 * specify some channels with different data types to be read
		 */

		// Short 8209:Series Code | 8194:Byte Order
		locators.add(new TestChannelLocator(device, "INPUT_REGISTERS:8209:short"));
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:8194:short"));

		ModbusDriver modbus = new ModbusDriver();

		/**
		 * create a SampledValueContainer for each channel and add each channel to the modbus object
		 */

		for (ChannelLocator channel : locators) {
			channels.add(new SampledValueContainer(channel));
			modbus.channelAdded(channel);
		}

		/**
		 * read and write the channels specified above
		 */

		try {

			modbus.readChannels(channels);

		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/**
		 * compare the read results with the expected ones
		 */

		// Short
		assertEquals(750, channels.get(0).getSampledValue().getValue().getIntegerValue(), 0);
		assertEquals(0x1234, channels.get(1).getSampledValue().getValue().getIntegerValue(), 0);

	}

}
