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
	private Map<String, Channel> channels; // channelAddress, Channel
	private Connection con;

	public Device(DeviceLocator deviceLocator, Connection connection) {
		con = connection;
		locator = deviceLocator;
		channels = new HashMap<String, Channel>();
		deviceAddress = deviceLocator.getDeviceAddress();
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
