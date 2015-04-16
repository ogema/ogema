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

import org.ogema.driver.xbee.frames.AbstractXBeeMessage;

/**
 * The Active_EP_req command is generated from a local endpoint wishing to acquire the list of endpoints on a remote
 * endpoint with simple descriptors. Upon receipt of this command, the recipient endpoint shall process the command and
 * generate an Active_EP_rsp command in response.
 * 
 * @author puschas
 * 
 */
public final class ActiveEndpointsRequest extends AbstractXBeeMessage {
	private final byte frameType = 0x11;
	private final byte frameId = 0x00; // Default no transmit status
	private final byte[] broadcastAddress64Bit = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF };
	private final byte[] broadcastAddress16Bit = { (byte) 0xFF, (byte) 0xFE };
	private final byte sourceEndpoint = 0x00;
	private final byte destinationEndpoint = 0x00;
	private final byte[] clusterId = { 0x00, 0x05 };
	private final byte[] profileId = { 0x00, 0x00 };
	private final byte broadcastRadius = 0x00;
	private final byte options = 0x00;
	private final byte command = 0x01;
	private final ByteBuffer message = ByteBuffer.allocate(23);

	public ActiveEndpointsRequest() {
		message.put(frameType);
		message.put(frameId);
		message.put(broadcastAddress64Bit);
		message.put(broadcastAddress16Bit);
		message.put(sourceEndpoint);
		message.put(destinationEndpoint);
		message.put(clusterId);
		message.put(profileId);
		message.put(broadcastRadius);
		message.put(options);
		message.put(command);
		message.put(new byte[] { 0x00, 0x00 }); // Placeholder
	}

	public ActiveEndpointsRequest(short nwkAddrOfInterest) {
		message.put(frameType);
		message.put(frameId);
		message.put(broadcastAddress64Bit);
		message.put(broadcastAddress16Bit);
		message.put(sourceEndpoint);
		message.put(destinationEndpoint);
		message.put(clusterId);
		message.put(profileId);
		message.put(broadcastRadius);
		message.put(options);
		message.put(command);
		message.putShort(Short.reverseBytes(nwkAddrOfInterest));
	}

	public void setNwkAddrOfInterest(short nwkAddrOfInterest) {
		message.position(message.limit() - 2);
		message.putShort(Short.reverseBytes(nwkAddrOfInterest));
	}

	public void setFrameId(byte id) {
		message.position(1);
		message.put(id);
	}

	public byte[] getBroadcastMessage() {
		message.position(2);
		message.put(broadcastAddress64Bit);
		message.put(broadcastAddress16Bit);
		return message.array();
	}

	public byte[] getUnicastMessage(byte[] address64Bit, byte[] address16Bit) {
		message.position(2);
		message.put(address64Bit);
		message.put(address16Bit);
		return message.array();
	}

	public byte[] getUnicastMessage(long sourceAddress64Bit, short sourceAddress16Bit) {
		message.position(2);
		message.putLong(sourceAddress64Bit);
		message.putShort(sourceAddress16Bit);
		return message.array();
	}
}
