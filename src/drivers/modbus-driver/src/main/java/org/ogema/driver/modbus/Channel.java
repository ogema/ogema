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
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * This class manages the driver data for each channel
 * 
 * Supported channel access modes:
 * 
 * - access 16bit register "reg:<hex address> returns an IntegerValue
 * 
 * - access multiple 16bit registers "multi:<no of 16bit register>:<first register address in hex>" returns an
 * ObjectValue that contains an int[] that has the modbus 16bit unsigned values (Java has no unsigned short data type)
 * 
 * @author pau
 * 
 */
public abstract class Channel {

	public static final String TYPE_REG = "reg";
	public static final String TYPE_MULTI = "multi";

	protected ChannelLocator locator;

	/**
	 * Channel Factory
	 * 
	 * @param locator
	 * @return
	 */
	public static Channel createChannel(ChannelLocator locator) {
		String[] splitAddress = locator.getChannelAddress().split(":"/* , 2 */);

		switch (splitAddress[0]) {
		case TYPE_REG:
			return new SingleChannel(locator, splitAddress);
		case TYPE_MULTI:
			return new MultiChannel(locator, splitAddress);
		default:
			break;
		}
		throw new NullPointerException("could not create Channel with Address " + locator.getChannelAddress());
	}

	protected Channel(ChannelLocator locator) {
		this.locator = locator;
	}

	public ChannelLocator getChannelLocator() {
		return locator;
	}

	abstract public SampledValue readValue(Connection connection) throws IOException;

	abstract public void writeValue(Connection connection, Value value) throws IOException;

}
