/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.xbee.frames;

import java.nio.ByteBuffer;

/**
 * This class represents a Explicit Addressing ClusterCommand Frame message. Use the message and the XBeeFrameFactory to
 * generate the Explicit Addressing ClusterCommand Frame frame.
 * 
 * @author puschas
 * @see <a href="http://ftp1.digi.com/support/utilities/digi_apiframes.htm">Digi API Packet Generator for ZB (ZigBee)
 *      Networks</a>
 */
public final class ExplicitAddressingCommandFrame extends AbstractXBeeMessage {
	private final byte frameType = 0x11;
	private final byte frameId;
	private final long address64Bit;
	private final short address16Bit;
	private final byte sourceEndpoint;
	private final byte destinationEndpoint;
	private final short clusterId;
	private final short profileId;
	private final byte broadcastRadius;
	private final byte options;
	private final byte[] dataPayload;
	private final byte[] message;

	/**
	 * @param All
	 *            parameters without predefined values
	 */
	public ExplicitAddressingCommandFrame(byte frameId, long address64Bit, short address16Bit, byte sourceEndpoint,
			byte destinationEndpoint, short clusterId, short profileId, byte broadcastRadius, byte options,
			byte[] dataPayload) {
		this.frameId = frameId;
		this.address64Bit = address64Bit;
		this.address16Bit = address16Bit;
		this.sourceEndpoint = sourceEndpoint;
		this.destinationEndpoint = destinationEndpoint;
		this.clusterId = clusterId;
		this.profileId = profileId;
		this.broadcastRadius = broadcastRadius;
		this.options = options;
		this.dataPayload = dataPayload;
		message = generateMessage();
	}

	/**
	 * @param payload
	 *            The complete content of the message (without frame start delimiter, length and checksum).
	 * @throws WrongFormatException
	 */
	public ExplicitAddressingCommandFrame(byte[] payload) throws WrongFormatException {
		ByteBuffer bb = ByteBuffer.wrap(payload);
		if (bb.get() != frameType) {
			throw new WrongFormatException();
		}
		frameId = bb.get();
		address64Bit = bb.getLong();
		address16Bit = bb.getShort();
		sourceEndpoint = bb.get();
		destinationEndpoint = bb.get();
		clusterId = bb.getShort();
		profileId = bb.getShort();
		broadcastRadius = bb.get();
		options = bb.get();

		dataPayload = new byte[bb.capacity() - 20];
		bb.get(dataPayload, 0, dataPayload.length);
		message = generateMessage();
	}

	@Override
	public byte getFrameType() {
		return frameType;
	}

	public byte getFrameId() {
		return this.frameId;
	}

	public byte[] getData() {
		return dataPayload;
	}

	public long getAddress64Bit() {
		return address64Bit;
	}

	public short getAddress16Bit() {
		return address16Bit;
	}

	public byte getBroadcastRadius() {
		return broadcastRadius;
	}

	public byte getOptions() {
		return options;
	}

	public short getClusterId() {
		return clusterId;
	}

	public short getProfileId() {
		return profileId;
	}

	public byte getSourceEndpoint() {
		return sourceEndpoint;
	}

	public byte getDestinationEndpoint() {
		return destinationEndpoint;
	}

	/**
	 * Use the returned message and the XBeeFrameFactory to generate the Explicit Addressing ClusterCommand Frame frame.
	 * 
	 * @return The Explicit Addressing ClusterCommand Frame message.
	 * @see org.ogema.driver.xbee.manager.XBeeFrameFactory
	 */
	@Override
	public byte[] getMessage() {
		return message;
	}

	private byte[] generateMessage() {
		ByteBuffer bb = ByteBuffer.allocate(dataPayload.length + 20);
		bb.put(frameType);
		bb.put(frameId);
		bb.putLong(address64Bit);
		bb.putShort(address16Bit);
		bb.put(sourceEndpoint);
		bb.put(destinationEndpoint);
		bb.putShort(clusterId);
		bb.putShort(profileId);
		bb.put(broadcastRadius);
		bb.put(options);
		bb.put(dataPayload);
		return bb.array();
	}
}
