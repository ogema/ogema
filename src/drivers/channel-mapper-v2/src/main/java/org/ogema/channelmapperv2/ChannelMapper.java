/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
