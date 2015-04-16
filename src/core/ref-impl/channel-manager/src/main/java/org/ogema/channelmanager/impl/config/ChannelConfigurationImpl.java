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
package org.ogema.channelmanager.impl.config;

import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class ChannelConfigurationImpl implements ChannelConfiguration {

	private final ChannelLocator channelLocator;
	private long samplingPeriodInMs = 1000;
	private Direction direction = Direction.DIRECTION_INOUT;

	public ChannelConfigurationImpl(ChannelLocator channel) {
		this.channelLocator = channel;
	}

	@Override
	public ChannelLocator getChannelLocator() {
		return channelLocator;
	}

	@Override
	public long getSamplingPeriod() {
		return this.samplingPeriodInMs;
	}

	@Override
	public void setSamplingPeriod(long samplingPeriodInMs) {
		this.samplingPeriodInMs = samplingPeriodInMs;
	}

	@Override
	public Direction getDirection() {
		return this.direction;
	}

	@Override
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	@Override
	public DeviceLocator getDeviceLocator() {
		return this.channelLocator.getDeviceLocator();
	}

}
