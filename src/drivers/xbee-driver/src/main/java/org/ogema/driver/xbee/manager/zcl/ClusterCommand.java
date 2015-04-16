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
package org.ogema.driver.xbee.manager.zcl;

import java.nio.ByteBuffer;

import org.ogema.driver.xbee.frames.ExplicitAddressingCommandFrame;

/**
 * This class represents a command of a ZigBee cluster.
 * 
 * @author puschas
 * 
 */
public class ClusterCommand {
	protected final byte identifier;
	private final String channelAddress;
	private final String description;
	private final boolean mandatory;
	protected Cluster cluster;

	public ClusterCommand(Cluster cluster, byte commandIdentifier, String description, boolean mandatory) {
		this.identifier = commandIdentifier;
		this.description = description;
		this.mandatory = mandatory;
		this.cluster = cluster;
		channelAddress = generateChannelAddress();
	}

	/**
	 * unused as of now. Creates the channel address of the attribute.
	 * 
	 * @return
	 */
	private String generateChannelAddress() {
		StringBuilder tempString = new StringBuilder();
		tempString.append(Integer.toHexString(cluster.getClusterId() & 0xffff));
		switch (tempString.length()) {
		case 0:
			tempString.append("0000");
			break;
		case 1:
			tempString.insert(0, "000");
			break;
		case 2:
			tempString.insert(0, "00");
			break;
		case 3:
			tempString.insert(0, "0");
			break;
		}
		tempString.append(":Command:");
		tempString.append(Integer.toHexString(identifier & 0xff));
		switch (tempString.length()) {
		case 13:
			tempString.append("00");
			break;
		case 14:
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

	/**
	 * This method creates the message for the cluster command. It is usually called from the writeValue function inside
	 * the CommandChannel.
	 * 
	 * @param messagePayload
	 *            contains the command id and further values if necessary.
	 * @return The message for a cluster command which has to be built into a complete frame by the FrameFactory before
	 *         it can be sent.
	 */
	public byte[] getMessage(byte[] messagePayload) {
		ByteBuffer dataPayload = ByteBuffer.allocate(3 + messagePayload.length);
		dataPayload.put((byte) 0x01); // frame control
		dataPayload.put((byte) 0x00); // placeholder for sequence number
		dataPayload.put(identifier);
		dataPayload.put(messagePayload);
		ExplicitAddressingCommandFrame frame = new ExplicitAddressingCommandFrame((byte) 0x01, cluster.endpoint
				.getDevice().getAddress64Bit(), cluster.endpoint.getDevice().getAddress16Bit(), (byte) 0x00,
				cluster.endpoint.getEndpointId(), cluster.getClusterId(), cluster.endpoint.getProfileId(), (byte) 0x00,
				(byte) 0x00, dataPayload.array());
		return frame.getMessage();
	}
}
