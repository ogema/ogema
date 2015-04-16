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
package org.ogema.driver.homematic.manager;

/**
 * This class represents a command of a HM Device.
 * 
 * @author puschas/baerthbn
 * 
 */
public class DeviceCommand {
	protected final byte identifier;
	private final String channelAddress;
	private final String description;
	private final boolean mandatory;
	protected RemoteDevice remoteDevice;

	public DeviceCommand(RemoteDevice remoteDevice, byte commandIdentifier, String description, boolean mandatory) {
		this.identifier = commandIdentifier;
		this.description = description;
		this.mandatory = mandatory;
		this.remoteDevice = remoteDevice;
		channelAddress = generateChannelAddress();
	}

	/**
	 * unused as of now. Creates the channel address of the attribute.
	 * 
	 * @return
	 */
	private String generateChannelAddress() {
		StringBuilder tempString = new StringBuilder();
		tempString.append("Command:");
		tempString.append(Integer.toHexString(identifier & 0xff));
		switch (tempString.length()) {
		case 8:
			tempString.append("00");
			break;
		case 9:
			tempString.insert(tempString.length() - 1, "0");
			break;
		}
		return tempString.toString();
	}

	public byte getIdentifier() {
		return identifier;
	}

	public String getDescription() {
		return description;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public void performCommand(byte[] data) {
		this.remoteDevice.performCommand(identifier, data);
	}

}
