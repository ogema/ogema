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
package org.ogema.channelmanager.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.slf4j.Logger;

class CommDevice {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	public CommDevice(DeviceLocator device, ChannelDriver driver) {
		this.channels = new LinkedList<Channel>();
		this.deviceLocator = device;

		this.readerThread = new DeviceReaderThread(this, driver);
		this.readerThread.updateConfiguration();
		this.readerThread.start();
	}

	private final DeviceReaderThread readerThread;
	private final DeviceLocator deviceLocator;
	private final List<Channel> channels;
	private List<SampledValueContainer> valueContainers = null;

	public DeviceLocator getDeviceLocator() {
		return deviceLocator;
	}

	public List<Channel> getChannels() {

		List<Channel> channellistcopy = new LinkedList<Channel>();

		for (int i = 0; i < channels.size(); i++) {
			channellistcopy.add(channels.get(i));
		}

		return channellistcopy;
	}

	public void addChannel(Channel channel) {

		new SampledValueContainer(channel.getConfiguration().getChannelLocator());

		logger.debug("Add channel with interval " + channel.getConfiguration().getSamplingPeriod());
		this.channels.add(channel);

		if (channel.getConfiguration().getSamplingPeriod() > 0) {

			this.readerThread.updateConfiguration();

			logger.debug("Add channel with interval " + channel.getConfiguration().getSamplingPeriod());

		}

		valueContainers = null;
	}

	public void removeChannel(Channel channel) {
		if (channels.contains(channel)) {
			channels.remove(channel);
			this.readerThread.updateConfiguration();
			readerThread.removeChannel(channel);
		}
	}

	public List<SampledValueContainer> getValueContainers() {
		if (valueContainers == null) {
			valueContainers = new LinkedList<SampledValueContainer>();
			for (Channel channel : channels) {
				valueContainers.add(channel.getSampledValueContainer());
			}
		}
		return valueContainers;
	}

	public boolean doesChannelMatch(Channel channel) {
		if (channel.getConfiguration().getChannelLocator().getDeviceLocator().equals(deviceLocator)) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CommDevice) {
			CommDevice otherDevice = (CommDevice) other;

			if (otherDevice.deviceLocator.equals(this.deviceLocator)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + Objects.hashCode(this.deviceLocator);
		return hash;
	}

	public void addChannelUpdateListener(Channel channel, ChannelEventListener listener) {
		readerThread.addChannelUpdateListener(channel, listener);
	}

	public void addChannelChangedListener(Channel channel, ChannelEventListener listener) {
		readerThread.addChannelChangedListener(channel, listener);
	}
}
