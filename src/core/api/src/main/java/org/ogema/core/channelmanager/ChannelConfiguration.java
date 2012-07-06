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
package org.ogema.core.channelmanager;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

/**
 * 
 * ChannelCofiguration is the Interface for the Configurations of Channels
 */
public interface ChannelConfiguration {

	/**
	 * Channel manager should use driver in listening mode
	 */
	public static final long LISTEN_FOR_UPDATE = -1;

	/**
	 * ChannelManager should use driver in comand mode, with no periodic reading or listening.
	 */
	public static final long NO_READ_NO_LISTEN = 0;

	/**
	 * Get the ChannelLocator associated with this ChannelConfiguration
	 * 
	 * @return ChannelLocator
	 */
	public ChannelLocator getChannelLocator();

	/**
	 * Get the DeviceLocator associated with this ChannelConfiguration
	 * 
	 * @return DeviceLocator
	 */
	public DeviceLocator getDeviceLocator();

	/**
	 * Direction of the Channel, <br>
	 * INPUT = read the values from channel <br>
	 * OUTPUT = write values at the channel
	 */
	public enum Direction {
		DIRECTION_INPUT, /* from gateways point of view */
		DIRECTION_OUTPUT, DIRECTION_INOUT
	}

	/**
	 * Get the sampling period (in milliseconds)
	 * @return the sampling period in ms
	 */
	public long getSamplingPeriod();

	/**
	 * 
	 * @param samplingPeriodInMs
	 *            sampling period in Ms - can be set to LISTEN_FOR_UPDATE if the channel manager should not actively
	 *            poll the driver but instruct the driver to listen and update the channel.
	 * 
	 *            A Sampling Periode of 0, will be not listen and not reading it's only for sending comands or reading
	 *            on comand. You can use the Vaiable NO_READ_NO_LISTEN
	 */
	public void setSamplingPeriod(long samplingPeriodInMs);

	/**
	 * 
	 * @return the Direction of Channel
	 */
	public Direction getDirection();

	/**
	 * Set Direction of the Channel
	 * 
	 * @param direction
	 */
	public void setDirection(Direction direction);

	/* other properties may be scaling factor, offset, ... */
	// public void setOffset(float offset);
	// public void setFactor(float factor);
}
