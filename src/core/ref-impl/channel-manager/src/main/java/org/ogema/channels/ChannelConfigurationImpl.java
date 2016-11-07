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
package org.ogema.channels;

import org.ogema.core.application.AppID;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

final class ChannelConfigurationImpl implements ChannelConfiguration {

	private final ChannelLocator channelLocator;
	private final long samplingPeriodInMs;
	private final Direction direction;
	private final AppID appID;

	ChannelConfigurationImpl(ChannelLocator channel, long samplingTime, Direction direction, AppID appID) {

		if (channel == null)
			throw new NullPointerException("Channel is null");

		if (direction == null)
			throw new NullPointerException("Direction is null");

		if (samplingTime < 0 && (samplingTime != ChannelConfiguration.LISTEN_FOR_UPDATE))
			throw new IllegalArgumentException("Invalid sampling time " + samplingTime);

		if (appID == null)
			throw new NullPointerException("app id is null");

		this.channelLocator = channel;
		this.samplingPeriodInMs = samplingTime;
		this.direction = direction;
		this.appID = appID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appID == null) ? 0 : appID.hashCode());
		result = prime * result + ((channelLocator == null) ? 0 : channelLocator.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + (int) (samplingPeriodInMs ^ (samplingPeriodInMs >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChannelConfigurationImpl other = (ChannelConfigurationImpl) obj;
		if (appID == null) {
			if (other.appID != null)
				return false;
		} else if (!appID.equals(other.appID))
			return false;
		if (channelLocator == null) {
			if (other.channelLocator != null)
				return false;
		} else if (!channelLocator.equals(other.channelLocator))
			return false;
		if (direction != other.direction)
			return false;
		if (samplingPeriodInMs != other.samplingPeriodInMs)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Channel " + channelLocator.toString() + "; app " + appID.getIDString() + "; direction " + direction.toString() + "; sampling " + samplingPeriodInMs;
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
	public Direction getDirection() {
		return this.direction;
	}

	@Override
	public DeviceLocator getDeviceLocator() {
		return this.channelLocator.getDeviceLocator();
	}

	AppID getAppID() {
		return appID;
	}
}
