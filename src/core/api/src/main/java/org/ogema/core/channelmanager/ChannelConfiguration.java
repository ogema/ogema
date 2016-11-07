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
	 * @return the Direction of Channel
	 */
	public Direction getDirection();
}
