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
package org.ogema.driver.modbustcp;

import java.io.IOException;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.modbustcp.ModbusChannel.EAccess;
import org.ogema.driver.modbustcp.enums.EDatatype;

/**
 * This class manages the driver data for each channel
 * 
 * Supported channel access modes:
 * 
 * 
 * @author pau
 * 
 */
public abstract class Channel {

	private static String channelAddress;

	/**
	 * Channel Factory
	 * 
	 * ChannelLocator is set in the subclass ModbusChannel
	 * 
	 * @param locator
	 * @return
	 */
	public static ModbusChannel createChannel(ChannelLocator locator) {

		channelAddress = locator.getChannelAddress().toUpperCase();

		ModbusChannel modbusChannel = new ModbusChannel(channelAddress);
		modbusChannel.setChannelLocator(locator);

		return modbusChannel;

	}

	protected Channel() {

	}

	abstract public SampledValue readValue(Connection connection) throws IOException;

	abstract public void writeValue(Connection connection, Value value) throws IOException;

	abstract public void update(EAccess accessFlag);

	abstract public ChannelLocator getChannelLocator();

	abstract public EDatatype getDatatype();

}
