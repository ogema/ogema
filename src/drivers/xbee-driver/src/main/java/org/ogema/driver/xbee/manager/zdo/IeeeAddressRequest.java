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
package org.ogema.driver.xbee.manager.zdo;

import java.nio.ByteBuffer;

/**
 * The IEEE_addr_req is generated from a Local Device wishing to inquire as to the 64-bit IEEE address of the Remote
 * Device based on their known 16-bit address. Upon receipt, a Remote Device shall compare the NWKAddrOfInterest to its
 * local NWK address or any NWK address held in its local discovery cache.
 * 
 * @author puschas
 * 
 */
public final class IeeeAddressRequest {
	private final byte frameType = 0x11;
	private final byte frameId = 0x00; // Default no transmit status
	private final byte[] broadcastAddress64Bit = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF };
	private final byte[] broadcastAddress16Bit = { (byte) 0xFF, (byte) 0xFE };
	private final byte sourceEndpoint = 0x00;
	private final byte destinationEndpoint = 0x00;
	private final byte[] clusterId = { 0x00, 0x01 };
	private final byte[] profileId = { 0x00, 0x00 };
	private final byte broadcastRadius = 0x00;
	private final byte options = 0x00;
	private final byte command = 0x01;
	private final ByteBuffer singleResponseMessage = ByteBuffer.allocate(24);
	private final ByteBuffer extendedResponseMessage = ByteBuffer.allocate(25);

	public IeeeAddressRequest() {
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
		singleResponseMessage.put(new byte[] { 0x00, 0x00 }); // Placeholder
		singleResponseMessage.put((byte) 0x00); // Single endpoint response

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
		extendedResponseMessage.put(new byte[] { 0x00, 0x00 }); // Placeholder
		extendedResponseMessage.put((byte) 0x01); // Extended endpoint response
		extendedResponseMessage.put((byte) 0x00); // Placeholder
	}

	public IeeeAddressRequest(short nwkAddrOfInterest) {
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
		singleResponseMessage.putShort(Short.reverseBytes(nwkAddrOfInterest));
		extendedResponseMessage.put((byte) 0x01); // Single endpoint response

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
		extendedResponseMessage.putShort(Short.reverseBytes(nwkAddrOfInterest));
		extendedResponseMessage.put((byte) 0x01); // Extended endpoint response
		extendedResponseMessage.put((byte) 0x00); // Placeholder
	}

	public void setNwkAddrOfInterest(short nwkAddrOfInterest) {
		singleResponseMessage.position(singleResponseMessage.limit() - 3);
		singleResponseMessage.putShort(Short.reverseBytes(nwkAddrOfInterest));
		extendedResponseMessage.position(extendedResponseMessage.limit() - 4);
		extendedResponseMessage.putShort(Short.reverseBytes(nwkAddrOfInterest));
	}

	public void setStartIndex(byte startIndex) {
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

	public byte[] getUnicastMessageSingleResponse(byte[] address64Bit, byte[] address16Bit) {
		singleResponseMessage.position(2);
		singleResponseMessage.put(address64Bit);
		singleResponseMessage.put(address16Bit);

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
