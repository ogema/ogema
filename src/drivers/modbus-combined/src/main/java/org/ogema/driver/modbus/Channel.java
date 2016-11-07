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
 * @author pau
 * 
 */
public abstract class Channel {

	public static final String TYPE_INPUT = "INPUT_REGISTERS";
	public static final String TYPE_HOLDING = "HOLDING_REGISTERS";
	public static final String TYPE_DISCRETE = "DISCRETE_INPUTS";
	public static final String TYPE_COIL = "COILS";

	protected ChannelLocator locator;

	/**
	 * Channel Factory
	 * 
	 * @param locator
	 * @return
	 */
	public static Channel createChannel(ChannelLocator locator)
			throws IllegalArgumentException {
		String[] splitAddress = locator.getChannelAddress().split(":");

		switch (splitAddress[1]) {
		case TYPE_INPUT:
			return new InputChannel(locator, splitAddress);
		case TYPE_HOLDING:
			return new HoldingChannel(locator, splitAddress);
		case TYPE_DISCRETE:
			return new DiscreteChannel(locator, splitAddress);
		case TYPE_COIL:
			return new CoilChannel(locator, splitAddress);
		default:
			throw new IllegalArgumentException(
					"could not create Channel with Address "
							+ locator.getChannelAddress());
		}
	}

	protected Channel(ChannelLocator locator) {
		this.locator = locator;
	}

	public ChannelLocator getChannelLocator() {
		return locator;
	}

	abstract public SampledValue readValue(Connection connection)
			throws IOException;

	abstract public void writeValue(Connection connection, Value value)
			throws IOException;

}
