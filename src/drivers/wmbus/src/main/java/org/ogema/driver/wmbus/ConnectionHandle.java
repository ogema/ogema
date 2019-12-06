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
package org.ogema.driver.wmbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelUpdateListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.openmuc.jmbus.wireless.WMBusConnection;

/**
 * Class representing an MBus Connection.<br>
 * This class will bind to the local com-interface.<br>
 * 
 */
public class ConnectionHandle {
	private ChannelUpdateListener listener;
	private WMBusConnection mBusConnection;
	private final Map<DeviceLocator, List<ChannelLocator>> deviceList = new HashMap<>();
	private String keys;
	private DeviceLocator dl;

	String getParams() {
		return keys;
	}

	String getIfaceId() {
		return dl.getInterfaceName();
	}

	public ConnectionHandle(WMBusConnection mBusCon, DeviceLocator dl) {
		this.mBusConnection = mBusCon;
		this.dl = dl;
		this.keys = dl.getParameters();
	}

	public ChannelUpdateListener getListener() {
		return listener;
	}

	public void setListener(ChannelUpdateListener listener) {
		this.listener = listener;
	}

	public WMBusConnection getMBusConnection() {
		return mBusConnection;
	}

	public int getDeviceCount() {
		return deviceList.size();
	}

	void addDevice(DeviceLocator dl) {
		if (!deviceList.containsKey(dl))
			deviceList.put(dl, new ArrayList<>());
	}

	public List<ChannelLocator> getChannels(DeviceLocator dl) {
		return deviceList.get(dl);
	}

	public void addChannel(DeviceLocator dl, ChannelLocator channel) {
		if (!deviceList.containsKey(dl))
			addDevice(dl);
		deviceList.get(dl).add(channel);
	}

	public void removeDevice(DeviceLocator dl) {
		deviceList.remove(dl);
	}

	public Set<DeviceLocator> getDevices() {
		return deviceList.keySet();
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof ConnectionHandle))
			return false;
		ConnectionHandle ch = (ConnectionHandle) o;
		if (ch.dl.equals(this.dl))
			return true;
		return false;
	}

	public int hashCode() {
		int result = 0;
		result = dl.hashCode();
		return result;
	}
}
