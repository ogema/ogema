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
 * The Simple_Desc_req command is generated from a local endpoint wishing to inquire as to the simple descriptor of a
 * remote endpoint on a specified endpoint. Upon receipt of this command, the recipient endpoint shall process the
 * command and generate a Simple_Desc_rsp command in response.
 * 
 * @author puschas
 * 
 */
public class SimpleDescriptorRequest {
	private final byte frameType = 0x11;
	private final byte frameId = 0x00; // Default no transmit status
	private final byte[] broadcastAddress64Bit = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF };
	private final byte[] broadcastAddress16Bit = { (byte) 0xFF, (byte) 0xFE };
	private final byte sourceEndpoint = 0x00;
	private final byte destinationEndpoint = 0x00;
	private final byte[] clusterId = { 0x00, 0x04 };
	private final byte[] profileId = { 0x00, 0x00 };
	private final byte broadcastRadius = 0x00;
	private final byte options = 0x00;
	private final byte command = 0x01;
	private final ByteBuffer message = ByteBuffer.allocate(24);

	public SimpleDescriptorRequest() {
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
		message.put((byte) 0x00); // Placeholder
	}

	public SimpleDescriptorRequest(byte[] nwkAddrOfInterest, byte endpoint) {
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
		message.put(nwkAddrOfInterest);
		message.put(endpoint);
	}

	public void setNwkAddrOfInterest(byte[] nwkAddrOfInterest) {
		message.position(message.limit() - 3);
		message.put(nwkAddrOfInterest);
	}

	public void setNwkAddrOfInterest(short nwkAddrOfInterest) {
		message.position(message.limit() - 3);
		message.putShort(Short.reverseBytes(nwkAddrOfInterest));
	}

	public void setEndpoint(byte endpoint) {
		message.position(message.limit() - 1);
		message.put(endpoint);
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

	public byte[] getUnicastMessage(long address64Bit, short address16Bit) {
		message.position(2);
		message.putLong(address64Bit);
		message.putShort(address16Bit);
		return message.array();
	}
}
