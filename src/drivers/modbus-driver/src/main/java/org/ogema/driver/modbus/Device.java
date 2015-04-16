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
package org.ogema.driver.modbus;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

/**
 * This class manages the driver data for each connected device
 * 
 * @author pau
 * 
 */
public class Device {

	private final DeviceLocator locator;
	private final List<Channel> channels;

	public Device(DeviceLocator device) {
		locator = device;
		channels = new ArrayList<Channel>();
	}

	public DeviceLocator getDeviceLocator() {
		return locator;
	}

	public List<ChannelLocator> getChannelLocators() {
		List<ChannelLocator> result = new ArrayList<ChannelLocator>(channels.size());

		for (Channel chan : channels) {
			result.add(chan.getChannelLocator());
		}

		return result;
	}

	public Channel findChannel(ChannelLocator channel) {
		for (Channel chan : channels) {
			if (chan.getChannelLocator().equals(channel))
				return chan;
		}

		return null;
	}

	public void addChannel(Channel channel) {
		channels.add(channel);

	}

	public void removeChannel(Channel channel) {
		channels.remove(channel);
	}

	public boolean hasChannels() {
		return !channels.isEmpty();
	}
}
