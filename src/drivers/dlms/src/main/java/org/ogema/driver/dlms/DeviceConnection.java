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
package org.ogema.driver.dlms;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;

public class DeviceConnection {

	private final ConnectionHandle connectionHandle;
	private final String deviceAddress;

	private final List<ChannelLocator> channels;
	private final List<ChannelAddress> addresses;

	public DeviceConnection(ConnectionHandle handle, String address) {

		this.connectionHandle = handle;
		this.deviceAddress = address;
		this.channels = new ArrayList<ChannelLocator>();
		this.addresses = new ArrayList<ChannelAddress>();

	}

	public ConnectionHandle getConnectionHandle() {
		return connectionHandle;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public List<ChannelLocator> getChannels() {
		return channels;
	}

	public void addChannel(ChannelLocator channel) {
		channels.add(channel);

	}

	public void removeChannel(ChannelLocator channel) {
		channels.remove(channel);
	}

	public boolean hasChannels() {
		return !channels.isEmpty();
	}

	public ChannelLocator findChannel(ChannelLocator channel) {
		for (ChannelLocator chan : channels) {
			if (chan.getChannelAddress().equals(channel.getChannelAddress())) {
				return chan;
			}
		}

		return null;
	}

	public void addChannelAddress(ChannelAddress channel) {
		addresses.add(channel);

	}

	public void removeChannelAddress(ChannelAddress channel) {
		addresses.remove(channel);
	}

	public List<ChannelAddress> getChannelAddress() {
		return addresses;
	}

	public ChannelAddress getChannelAddress(ChannelLocator channel) {

		String channelAdress = channel.getChannelAddress();

		String result[] = channelAdress.split(":");

		int classID = Integer.parseInt(result[0]);
		int attributeID = Integer.parseInt(result[2]);
		String instanceID = result[1];

		for (ChannelAddress address : addresses) {

			if (address.getAttributeId() == attributeID && address.getClassId() == classID
					&& address.getPrintableInstanceID().equals(instanceID)) {
				return address;
			}
		}

		return null;

	}

}
