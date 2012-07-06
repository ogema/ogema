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

public abstract class AbstractXBeeMessage {
	protected ByteBuffer message;

	/**
	 * Use the returned message and the XBeeFrameFactory to generate the frame.
	 * 
	 * @return The message
	 * @see org.ogema.driver.xbee.manager.XBeeFrameFactory
	 */
	public byte[] getMessage() {
		return message.array();
	}

	void setMessage(byte[] message) {
		this.message = ByteBuffer.wrap(message);
	}

	byte getFrameType() {
		return message.array()[0];
	}

	void setFrameType(byte frameType) {
		message.position(0);
		message.put(frameType);
	}
}
