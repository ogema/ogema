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
