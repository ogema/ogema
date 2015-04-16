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
package org.ogema.driver.xbee;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.xbee.manager.Endpoint;
import org.ogema.driver.xbee.manager.RemoteDevice;
import org.slf4j.Logger;

/**
 * Each endpoint represents a ZigBee Endpoint
 * 
 * @author puschas
 * 
 */
public class Device {
	private final DeviceLocator locator;
	private String deviceAddress;
	private Map<String, Channel> channels; // channelAddress, Channel
	private Endpoint endpoint;
	private String[] splitAddress;
	private final long address64Bit;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public Device(DeviceLocator deviceLocator, Connection connection) {
		locator = deviceLocator;
		splitAddress = locator.getDeviceAddress().split(":");

		byte[] addressArray = DatatypeConverter.parseHexBinary(splitAddress[0]);
		byte[] endpointId = DatatypeConverter.parseHexBinary(splitAddress[1]);

		RemoteDevice remoteDevice = null;
		ByteBuffer bb = ByteBuffer.wrap(addressArray);

		switch (addressArray.length) {
		case 2:
			/*
			 * short address16Bit = (short) ((addressArray[0] << 8) | (addressArray[1] & 0xff)); address16Bit &= 0xffff;
			 */
			short address16Bit = bb.getShort();
			remoteDevice = connection.localDevice.getRemoteDevice(address16Bit);
			address64Bit = remoteDevice.getAddress64Bit();
			break;
		case 8:
			if (Configuration.DEBUG) {
				logger.debug("to parse:");
				for (byte b : addressArray) {
					logger.debug(Constants.bytesToHex(b));
				}
			}
			address64Bit = bb.getLong();
			if (Configuration.DEBUG) {
				logger.debug("\nremoteDevice: " + Long.toHexString(address64Bit));
			}
			remoteDevice = connection.localDevice.getRemoteDevice(address64Bit);
			break;
		default:
			address64Bit = -1L;
			break;
		}
		if (remoteDevice == null)
			throw new RuntimeException("Connection lost to the ZigBee device " + Long.toHexString(address64Bit));
		Endpoint ep = remoteDevice.getEndpoints().get(endpointId[0]);
		if (ep == null) // the device is not yet initialized properly. channel creation is not allowed in this case.
			throw new RuntimeException("Device is not yet initialized properly!");
		setEndpoint(ep);
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

	public Map<String, Channel> getChannels() {
		return channels;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public DeviceLocator getDeviceLocator() {
		return locator;
	}

	public long getAddress64Bit() {
		return address64Bit;
	}

	public void removeChannel(String channelAddress) {
		Channel channel = channels.get(channelAddress);
		try {
			channel.removeUpdateListener();
		} catch (UnsupportedOperationException | IOException e) {
			e.printStackTrace();
		}
		channels.remove(channelAddress);
	}

	public void removeChannels() {
		for (Map.Entry<String, Channel> channelEntry : channels.entrySet()) {
			try {
				channelEntry.getValue().removeUpdateListener();
			} catch (UnsupportedOperationException | IOException e) {
				e.printStackTrace();
			}
		}
		channels.clear();
	}
}
