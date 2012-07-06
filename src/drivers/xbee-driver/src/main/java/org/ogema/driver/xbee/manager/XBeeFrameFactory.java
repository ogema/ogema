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
package org.ogema.driver.xbee.manager;

import java.nio.ByteBuffer;

import org.ogema.driver.xbee.frames.WrongFormatException;

/**
 * This class creates a valid Digi frame from a given message payload. That means that it will calculate the lenght,
 * checksum and also add the start delimiter.
 * 
 * @author puschas
 * 
 */
public class XBeeFrameFactory {
	private final static byte startDelimiter = 0x7E;

	/**
	 * Calculates checksum as defined by Digi
	 * 
	 * @param payload
	 * @return
	 */
	private static byte calcChecksum(byte[] payload) {
		int check = 0;
		for (int i = 0; i < payload.length; i++) {
			check += payload[i];
		}
		return (byte) (0xFF - (check & 0xFF));
	}

	private static short calcLength(byte[] str) throws WrongFormatException {
		if (str.length < 84) {
			return (short) str.length;
		}
		else {
			throw new WrongFormatException("Frame is too long!");
		}
	}

	/**
	 * 
	 * @param message
	 * @return the complete frame ready to be sent out the serial port
	 * @throws WrongFormatException
	 */
	public static byte[] composeMessageToFrame(byte[] message) throws WrongFormatException {
		ByteBuffer bb = ByteBuffer.allocate(message.length + 4);
		bb.put(startDelimiter);
		bb.putShort(calcLength(message));
		bb.put(message);
		bb.put(calcChecksum(message));
		return bb.array();
	}
}
