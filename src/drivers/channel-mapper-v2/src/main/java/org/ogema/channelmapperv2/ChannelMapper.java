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
package org.ogema.channelmapperv2;

import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.simple.SingleValueResource;

/**
 * A generic high-level driver for OGEMA. It is responsible for mapping OGEMA resource elements to channels of the
 * ChannelAPI.
 */
public interface ChannelMapper {

	/**
	 * Map a channel to a resource element. Connects a measurement or control channel to a resource. The connection
	 * (mapping) is persistently stored until the unmapChannel method is called for the channel.
	 * @param channel
	 * @param target
	 * @param channelAccess
	 * @param direction
	 * @param samplingPeriod
	 * @param scalingFactor
	 * @param valueOffset
	 */
	public void mapChannelToResource(ChannelLocator channel, SingleValueResource target,
			Direction direction, long samplingPeriod, float scalingFactor, float valueOffset);

	/**
	 * Unmap a channel. Remove all connections from the specified channel to OGEMA resources.
	 * 
	 * @param channel
	 *            the channel to be unmapped.
	 */
	void unmapChannel(ChannelLocator channel);

	/**
	 * Unmap a channel. Remove a single connection from the specified channel to an OGEMA resource.
	 * 
	 * @param channel
	 *            the channel to be unmapped
	 * @param target
	 */
	void unmapChannel(ChannelLocator channel, SingleValueResource target);
}
