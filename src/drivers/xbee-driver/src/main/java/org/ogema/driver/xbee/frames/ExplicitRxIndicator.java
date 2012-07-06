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
 * This immutable class represents an Explicit Rx Indicator message. Use the message and the XBeeFrameFactory to
 * generate the Explicit Rx Indicator frame.
 * 
 * @author puschas
 * @see <a href="http://ftp1.digi.com/support/utilities/digi_apiframes.htm">Digi API Packet Generator for ZB (ZigBee)
 *      Networks</a>
 */
public final class ExplicitRxIndicator extends AbstractXBeeMessage {
	private final byte frameType = (byte) 0x91;
	private final long address64Bit;
	private final short address16Bit;
	private final byte sourceEndpoint;
	private final byte destinationEndpoint;
	private final short clusterId;
	private final short profileId;
	private final byte receiveOptions;
	private final byte[] receivedData;
	private final byte[] message;

	/**
	 * @param All
	 *            parameters without predefined values.
	 */
	public ExplicitRxIndicator(long address64Bit, short address16Bit, byte sourceEndpoint, byte destinationEndpoint,
			short clusterId, short profileId, byte receiveOptions, byte[] receivedData) {
		this.address64Bit = address64Bit;
		this.address16Bit = address16Bit;
		this.sourceEndpoint = sourceEndpoint;
		this.destinationEndpoint = destinationEndpoint;
		this.clusterId = clusterId;
		this.profileId = profileId;
		this.receiveOptions = receiveOptions;
		this.receivedData = receivedData;
		message = generateMessage();
	}

	/**
	 * @param payload
	 *            The complete content of the message (without frame start delimiter, length and checksum).
	 * @throws WrongFormatException
	 */
	public ExplicitRxIndicator(byte[] payload) throws WrongFormatException {
		ByteBuffer bb = ByteBuffer.wrap(payload);
		if (bb.get() != frameType) {
			throw new WrongFormatException();
		}
		address64Bit = bb.getLong();
		address16Bit = bb.getShort();
		sourceEndpoint = bb.get();
		destinationEndpoint = bb.get();
		clusterId = bb.getShort();
		profileId = bb.getShort();
		receiveOptions = bb.get();

		receivedData = new byte[bb.capacity() - 18];
		bb.get(receivedData, 0, receivedData.length);
		message = generateMessage();
	}

	@Override
	public byte getFrameType() {
		return frameType;
	}

	public long getAddress64Bit() {
		return address64Bit;
	}

	public short getAddress16Bit() {
		return address16Bit;
	}

	public byte getSourceEndpoint() {
		return sourceEndpoint;
	}

	public byte getDestinationEndpoint() {
		return destinationEndpoint;
	}

	public short getClusterId() {
		return clusterId;
	}

	public short getProfileId() {
		return profileId;
	}

	public byte getReceiveOptions() {
		return receiveOptions;
	}

	public byte[] getReceivedData() {
		return receivedData;
	}

	/**
	 * Use the returned message and the XBeeFrameFactory to generate the Explicit Rx Indicator frame.
	 * 
	 * @return The Explicit Rx Indicator message.
	 * @see org.ogema.driver.xbee.manager.XBeeFrameFactory
	 */
	@Override
	public byte[] getMessage() {
		return message;
	}

	private byte[] generateMessage() {
		ByteBuffer bb = ByteBuffer.allocate(18 + receivedData.length);
		bb.put(frameType);
		bb.putLong(address64Bit);
		bb.putShort(address16Bit);
		bb.put(sourceEndpoint);
		bb.put(destinationEndpoint);
		bb.putShort(clusterId);
		bb.putShort(profileId);
		bb.put(receiveOptions);
		bb.put(receivedData);
		return bb.array();
	}
}
