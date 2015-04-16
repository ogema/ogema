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

/**
 * The NWK_addr_req is generated from a Local Device wishing to inquire as to the 16-bit address of the Remote Device
 * based on its known IEEE address. Upon receipt, a Remote Device shall compare the IEEEAddr to its local IEEE address
 * or any IEEE address held in its local discovery cache.
 * 
 * @author puschas
 * 
 */
public final class NetworkAddressRequest {
	private final byte frameType = 0x11;
	private final byte frameId = 0x00; // Default no transmit status
	private final byte[] broadcastAddress64Bit = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF };
	private final byte[] broadcastAddress16Bit = { (byte) 0xFF, (byte) 0xFE };
	private final byte sourceEndpoint = 0x00;
	private final byte destinationEndpoint = 0x00;
	private final byte[] clusterId = { 0x00, 0x00 };
	private final byte[] profileId = { 0x00, 0x00 };
	private final byte broadcastRadius = 0x00;
	private final byte options = 0x00;
	private final byte command = 0x01;
	private final ByteBuffer singleResponseMessage = ByteBuffer.allocate(31);
	private final ByteBuffer extendedResponseMessage = ByteBuffer.allocate(31);

	public NetworkAddressRequest(long ieeeAddress) {
		singleResponseMessage.put(frameType);
		singleResponseMessage.put(frameId);
		singleResponseMessage.put(broadcastAddress64Bit);
		singleResponseMessage.put(broadcastAddress16Bit);
		singleResponseMessage.put(sourceEndpoint);
		singleResponseMessage.put(destinationEndpoint);
		singleResponseMessage.put(clusterId);
		singleResponseMessage.put(profileId);
		singleResponseMessage.put(broadcastRadius);
		singleResponseMessage.put(options);
		singleResponseMessage.put(command);
		singleResponseMessage.putLong(Long.reverseBytes(ieeeAddress));
		singleResponseMessage.put((byte) 0x00); // Single endpoint response
		singleResponseMessage.put((byte) 0x00); // Placeholder

		extendedResponseMessage.put(frameType);
		extendedResponseMessage.put(frameId);
		extendedResponseMessage.put(broadcastAddress64Bit);
		extendedResponseMessage.put(broadcastAddress16Bit);
		extendedResponseMessage.put(sourceEndpoint);
		extendedResponseMessage.put(destinationEndpoint);
		extendedResponseMessage.put(clusterId);
		extendedResponseMessage.put(profileId);
		extendedResponseMessage.put(broadcastRadius);
		extendedResponseMessage.put(options);
		extendedResponseMessage.put(command);
		extendedResponseMessage.putLong(Long.reverseBytes(ieeeAddress));
		extendedResponseMessage.put((byte) 0x01); // Extended endpoint response
		extendedResponseMessage.put((byte) 0x00); // Placeholder
	}

	public void setIEEEAddress(long ieeeAddress) {
		singleResponseMessage.position(singleResponseMessage.limit() - 3);
		singleResponseMessage.putLong(Long.reverseBytes(ieeeAddress));
		extendedResponseMessage.position(extendedResponseMessage.limit() - 4);
		extendedResponseMessage.putLong(Long.reverseBytes(ieeeAddress));
	}

	public void setStartIndex(byte startIndex) {
		singleResponseMessage.position(singleResponseMessage.limit() - 1);
		singleResponseMessage.put(startIndex);
		extendedResponseMessage.position(extendedResponseMessage.limit() - 1);
		extendedResponseMessage.put(startIndex);
	}

	public void setFrameId(byte id) {
		singleResponseMessage.position(1);
		singleResponseMessage.put(id);

		extendedResponseMessage.position(1);
		extendedResponseMessage.put(id);
	}

	public byte[] getBroadcastMessageSingleResponse() {
		singleResponseMessage.position(2);
		singleResponseMessage.put(broadcastAddress64Bit);
		singleResponseMessage.put(broadcastAddress16Bit);

		return singleResponseMessage.array();
	}

	public byte[] getUnicastMessageSingleResponse(byte[] address64Bit) {
		singleResponseMessage.position(2);
		singleResponseMessage.put(address64Bit);
		singleResponseMessage.putShort((short) 0xFFFE);

		return singleResponseMessage.array();
	}

	public byte[] getUnicastMessageSingleResponse(long address64Bit) {
		singleResponseMessage.position(2);
		singleResponseMessage.putLong(address64Bit);
		singleResponseMessage.putShort((short) 0xFFFE);

		return singleResponseMessage.array();
	}

	public byte[] getBroadcastMessageExtendedResponse() {
		extendedResponseMessage.position(2);
		extendedResponseMessage.put(broadcastAddress64Bit);
		extendedResponseMessage.put(broadcastAddress16Bit);

		return extendedResponseMessage.array();
	}

	public byte[] getUnicastMessageExtendedResponse(byte[] address64Bit, byte[] address16Bit) {
		extendedResponseMessage.position(2);
		extendedResponseMessage.put(address64Bit);
		extendedResponseMessage.put(address16Bit);

		return extendedResponseMessage.array();
	}
}
