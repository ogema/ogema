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

import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.xbee.AttributeChannel;
import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.ManufacturerChannel;
import org.ogema.driver.xbee.frames.ExplicitAddressingCommandFrame;

/**
 * This class represents an attribute of a ZigBee cluster.
 * 
 * @author puschas
 * 
 */
public class ClusterAttribute {
	private final short identifier;
	private final String attributeName;
	private final String channelAddress;
	// type;
	// range;
	private final boolean readOnly;
	// default
	// private final boolean mandatory;
	private Value value;
	private long valueTimestamp;
	private final Cluster cluster;
	private AttributeChannel attributeChannel;
	@SuppressWarnings("unused")
	// reserved for future use
	private ManufacturerChannel manufacturerChannel;
	private boolean haslistener = false;

	public ClusterAttribute(Cluster cluster, short identifier, String attributeName, boolean readOnly, boolean mandatory) {
		this.identifier = identifier;
		this.attributeName = attributeName;
		this.cluster = cluster;
		this.readOnly = readOnly;
		// this.mandatory = mandatory;
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
		tempString.append(":Attribute:");
		tempString.append(Integer.toHexString(identifier & 0xffff));
		switch (tempString.length()) {
		case 15:
			tempString.append("0000");
			break;
		case 16:
			tempString.insert(tempString.length() - 1, "000");
			break;
		case 17:
			tempString.insert(tempString.length() - 2, "00");
			break;
		case 18:
			tempString.insert(tempString.length() - 3, "0");
			break;
		}
		return tempString.toString();
	}

	public short getIdentifier() {
		return identifier;
	}

	public String getChannelAddress() {
		return channelAddress;
	}

	public boolean readOnly() {
		return readOnly;
	}

	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * This method creates the message for a read attribute command. It is usually called from the readValue function
	 * inside the AttributeChannel and AttributeMultiChannel.
	 * 
	 * @param messagePayload
	 *            can by an empty byte array or contain further attribute IDs from attributes in the same Cluster.
	 * @return The message for a read attribute command which has to be built into a complete frame by the FrameFactory
	 *         before it can be sent.
	 */
	public byte[] getMessage(byte command, byte[] messagePayload) {
		ByteBuffer dataPayload = ByteBuffer.allocate(5 + messagePayload.length);
		dataPayload.put((byte) 0x00); // Frame control
		dataPayload.put((byte) 0x00); // Placeholder sequence number
		dataPayload.put(command); // attribute command
		dataPayload.putShort(Short.reverseBytes(identifier));
		dataPayload.put(messagePayload);
		ExplicitAddressingCommandFrame frame = new ExplicitAddressingCommandFrame((byte) 0x01, cluster.endpoint
				.getDevice().getAddress64Bit(), cluster.endpoint.getDevice().getAddress16Bit(), (byte) 0x00,
				cluster.endpoint.getEndpointId(), cluster.getClusterId(), cluster.endpoint.getProfileId(), (byte) 0x00,
				(byte) 0x00, dataPayload.array());
		return frame.getMessage();
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
		valueTimestamp = Constants.calendar.getTimeInMillis();
		if (haslistener) {
			attributeChannel.updateListener();
		}
	}

	public long getValueTimestamp() {
		return valueTimestamp;
	}

	public void setChannel(AttributeChannel attributeChannel) {
		this.attributeChannel = attributeChannel;
	}

	public void setChannel(ManufacturerChannel manufacturerChannel) {
		this.manufacturerChannel = manufacturerChannel;
	}

	public void setListener(boolean b) {
		haslistener = b;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	public void unsupportedAttribute() {
		throw new UnsupportedOperationException();
	}
}
