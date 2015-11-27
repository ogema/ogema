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
package org.ogema.driver.zwave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.zwave.manager.Node;

/**
 * 
 * @author baerthbn
 * 
 */
public class Device {
	private final DeviceLocator locator;
	private short deviceId;
	private Map<String, Channel> channels; // channelAddress, Channel
	private Connection con;

	public Device(DeviceLocator deviceLocator, Connection connection) {
		con = connection;
		locator = deviceLocator;
		channels = new HashMap<String, Channel>();
		String params = deviceLocator.getParameters();
		String id = params.substring(params.lastIndexOf(':') + 1, params.length());
		deviceId = Short.valueOf(id);
	}

	public List<ChannelLocator> getChannelLocators() {
		List<ChannelLocator> tempList = new ArrayList<ChannelLocator>();
		for (Map.Entry<String, Channel> channel : channels.entrySet()) {
			tempList.add(channel.getValue().getChannelLocator());
		}
		return tempList;
	}

	public Channel findChannel(ChannelLocator channelLocator) {
		return channels.get(channelLocator.getChannelAddress());
	}

	public void addChannel(Channel chan) {
		channels.put(chan.getChannelLocator().getChannelAddress(), chan);
	}

	public void removeChannel(Channel chan) {
		channels.remove(chan.getChannelLocator().getChannelAddress());
	}

	public Map<String, Channel> getChannels() {
		return channels;
	}

	public Node getNode() {
		return con.getLocalDevice().getNodes().get(deviceId);
	}

	public DeviceLocator getDeviceLocator() {
		return locator;
	}

	public Short getDeviceId() {
		return deviceId;
	}
}
