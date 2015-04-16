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
package org.ogema.driver.homematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.homematic.manager.RemoteDevice;

/**
 * 
 * @author baerthbn
 * 
 */
public class Device {
	private final DeviceLocator locator;
	private String deviceAddress;
	private Map<String, Channel> Channels; // channelAddress, Channel
	private Connection con;

	public Device(DeviceLocator deviceLocator, Connection connection) {
		con = connection;
		locator = deviceLocator;
		Channels = new HashMap<String, Channel>();
		deviceAddress = deviceLocator.getDeviceAddress();
	}

	public List<ChannelLocator> getChannelLocators() {
		List<ChannelLocator> tempList = new ArrayList<ChannelLocator>();
		for (Map.Entry<String, Channel> channel : Channels.entrySet()) {
			tempList.add(channel.getValue().getChannelLocator());
		}
		return tempList;
	}

	public Channel findChannel(ChannelLocator channelLocator) {
		return Channels.get(channelLocator.getChannelAddress());
	}

	public void addChannel(Channel chan) {
		Channels.put(chan.getChannelLocator().getChannelAddress(), chan);
	}

	public void removeChannel(Channel chan) {
		Channels.remove(chan.getChannelLocator().getChannelAddress());
	}

	public Map<String, Channel> getChannels() {
		return Channels;
	}

	public RemoteDevice getRemoteDevice() {
		return con.localDevice.getDevices().get(deviceAddress);
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public DeviceLocator getDeviceLocator() {
		return locator;
	}
}
