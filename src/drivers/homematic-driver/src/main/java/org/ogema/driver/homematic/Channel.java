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
package org.ogema.driver.homematic;

import java.io.IOException;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * Each channel represents a Homematic Command/Attribute
 * 
 * @author puschas,baerthbn
 * 
 */
abstract class Channel {
	protected ChannelLocator locator;

	protected Channel(ChannelLocator locator) {
		this.locator = locator;
	}

	public static Channel createChannel(ChannelLocator locator, Device dev) {
		String[] splitAddress = locator.getChannelAddress().split(":");
		switch (splitAddress[0]) {
		case "COMMAND":
			return new CommandChannel(locator, splitAddress, dev);
		case "ATTRIBUTE":
			return new AttributeChannel(locator, splitAddress, dev);
		default:
			break;
		}
		throw new NullPointerException("could not create Channel with Address " + locator.getChannelAddress());
	}

	public ChannelLocator getChannelLocator() {
		return locator;
	}

	abstract public SampledValue readValue(Connection connection) throws IOException, UnsupportedOperationException;

	abstract public void writeValue(Connection connection, Value value) throws IOException,
			UnsupportedOperationException;

	abstract public void setEventListener(SampledValueContainer container, ChannelUpdateListener listener)
			throws IOException, UnsupportedOperationException;

	abstract public void removeUpdateListener() throws IOException, UnsupportedOperationException;
}
