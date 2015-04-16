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
package org.ogema.driver.xbee.frames;

import java.nio.ByteBuffer;

public class TransmitRequest extends AbstractXBeeMessage {
	private final byte frameType = 0x10;
	private byte frameId = 0x00;
	private final long address64Bit;
	private final short address16Bit;
	private final byte broadcastRadius = 0x00;
	private final byte options = 0x00;

	public TransmitRequest(long address64Bit, short address16Bit, byte[] payload) {
		this.address64Bit = address64Bit;
		this.address16Bit = address16Bit;
		message = ByteBuffer.allocate(14 + payload.length);
		buildMessage(payload);
	}

	private void buildMessage(byte[] payload) {
		message.put(frameType);
		message.put(frameId);
		message.putLong(address64Bit);
		message.putShort(address16Bit);
		message.put(broadcastRadius);
		message.put(options);
		message.put(payload);
	}

	public byte getFrameId() {
		return frameId;
	}

	public void setFrameId(byte frameId) {
		this.frameId = frameId;
	}
}
