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
package org.ogema.driver.xbee.manager.zdo;

import java.nio.ByteBuffer;

public final class MatchDescriptorResponse {
	private final byte frameType = 0x11;
	private final byte frameId = 0x00; // Default no transmit status
	private final byte sourceEndpoint = 0x00;
	private final byte destinationEndpoint = 0x00;
	private final byte[] clusterId = { (byte) 0x80, 0x06 };
	private final byte[] profileId = { 0x00, 0x00 };
	private final byte broadcastRadius = 0x00;
	private final byte options = 0x00;
	private final byte command = 0x01;
	private final ByteBuffer message;

	public MatchDescriptorResponse(long address64Bit, short address16Bit, byte[] payload) {
		message = ByteBuffer.allocate(21 + payload.length);
		message.put(frameType);
		message.put(frameId);
		message.putLong(address64Bit);
		message.putShort(address16Bit);
		message.put(sourceEndpoint);
		message.put(destinationEndpoint);
		message.put(clusterId);
		message.put(profileId);
		message.put(broadcastRadius);
		message.put(options);
		message.put(command);
		message.put(payload);
	}

	public byte[] getUnicastMessage() {
		return message.array();
	}
}
