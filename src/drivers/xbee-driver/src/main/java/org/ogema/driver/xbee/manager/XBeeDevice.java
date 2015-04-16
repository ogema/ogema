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
package org.ogema.driver.xbee.manager;

import java.nio.ByteBuffer;

import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.xbee.XBeeChannel;

public class XBeeDevice extends RemoteDevice {

	private Value value;
	private XBeeChannel xBeeChannel;
	private boolean hasListener = false;

	public XBeeDevice(long address64Bit, short address16Bit) {
		super(address64Bit, address16Bit);
		endpoints.put((byte) 0xE8, new Endpoint(this, (byte) 0xE8));
		// The remote XBee in AT-transparent mode will only forward data out the serial port which is targetted at 0xE8
		// @see http://www.digi.com/wiki/developer/index.php/Understanding_XBee_EndPoints
		deviceType = "XBee";
	}

	public String getNodeIdentifier() {
		return nodeIdentifier;
	}

	public void setNodeIdentifier(String nodeIdentifier) {
		this.nodeIdentifier = nodeIdentifier;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
		if (hasListener) {
			xBeeChannel.updateListener();
		}
	}

	public void setChannel(XBeeChannel xBeeChannel) {
		this.xBeeChannel = xBeeChannel;
	}

	public void setListener(boolean b) {
		hasListener = b;
	}

	public void parseNodeIdentifier(ByteBuffer byteBuffer) {
		int niPart = byteBuffer.getInt();
		switch (niPart) {
		case 0x31313056: // 110V => ZBS-110V
			byte niPart2 = byteBuffer.get();
			if (niPart2 == 0x32) { // 2 => ZBS-110V2
				setNodeIdentifier("ZBS-110V2");
			}
			break;
		case 0x5a425331: // ZBS1
			if (0x3232 == byteBuffer.getShort()) { // 22 => ZBS122
				setNodeIdentifier("ZBS-122");
			}
			break;
		case 0x48412053: // HA S
			if (0x454E534F524B4E4FL == byteBuffer.getLong()) { // ENSORKNO -> HA
				// SENSORKNO
				if (0x5445 == byteBuffer.getShort()) { // TE -> HA SENSORKNOTE
					if (0x4E == byteBuffer.get()) { // N -> HA SENSORKNOTEN
						setNodeIdentifier("HA Sensorknoten");
					}
				}
			}
		}
	}
}
