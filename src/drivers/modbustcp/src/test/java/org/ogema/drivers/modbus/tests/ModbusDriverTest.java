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
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.driver.modbustcp.ModbusDriver;

/**
 * This is a junit test class for a modbus/tcp connection. The used device is a Janitza UMG 507 and all registers and
 * their default values can be found in the official address list at http://www.janitza.com/downloads/operating-manuals/
 * 
 * @author pweiss
 * 
 */

public class ModbusDriverTest {

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
	DeviceLocator device = new TestDeviceLocator("modbus-tcp", "", "192.168.000.001", null);

	@Test
	public void testReadChannelsListOfSampledValueContainer() {

		/**
		 * specify some channels with different data types to be read
		 */

		// Byte[] 11104:Device Description | 10800:E-Mail-Header
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:11104:bytearray[6]"));
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:10800:BYTEARRAY[17]"));

		// Float 576:Temperature internal | 6004:Voltage transformer primary
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:576:float"));
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:6004:FLOAT"));

		// Integer 3444:Seconds timer | 3183:Comparator lead-times
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:3444:int"));
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:3183:INT"));

		// Short 3561:Contrast setting of display | 3280:Flag Nr.: 2
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:3561:short"));
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:3280:SHORT"));

		// String 11104:Device Description
		locators.add(new TestChannelLocator(device, "HOLDING_REGISTERS:11104:STRING[6]"));

		/**
		 * specify some values to write and the targeting channel
		 */

		String writingValue = "UMG507";
		byte[] bt = writingValue.getBytes();

		ValueContainer value = new ValueContainer(locators.get(0), new ByteArrayValue(bt));

		writingContainer.add(value);

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
			modbus.writeChannels(writingContainer);
			modbus.readChannels(channels);

		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/**
		 * compare the read results with the expected ones
		 */

		// Byte[]
		assertEquals("UMG507", modbus.getStringFromBYTEARRAY(channels.get(0).getSampledValue().getValue()
				.getByteArrayValue()));
		assertEquals("umg507@Janitza.de", modbus.getStringFromBYTEARRAY(channels.get(1).getSampledValue().getValue()
				.getByteArrayValue()));

		// Float
		assertEquals(25.f, channels.get(2).getSampledValue().getValue().getFloatValue(), 4.0);
		assertEquals(400.f, channels.get(3).getSampledValue().getValue().getFloatValue(), 0.0);

		// Integer
		assertEquals(0, channels.get(4).getSampledValue().getValue().getIntegerValue(), 0);
		assertEquals(0, channels.get(5).getSampledValue().getValue().getIntegerValue(), 0);

		// Short
		assertEquals(20, channels.get(6).getSampledValue().getValue().getIntegerValue(), 0);
		assertEquals(1, channels.get(7).getSampledValue().getValue().getIntegerValue(), 0);

		// String
		assertEquals("UMG507", channels.get(8).getSampledValue().getValue().getStringValue());

	}

}
