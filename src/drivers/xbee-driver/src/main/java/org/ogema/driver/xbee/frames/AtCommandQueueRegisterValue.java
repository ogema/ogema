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

/**
 * This class represents an AT ClusterCommand Queue Register Value message. Use the message and the XBeeFrameFactory to
 * generate the AT ClusterCommand Queue Register Value frame.
 * 
 * @author puschas
 * @version 1.0
 * @see <a href="http://ftp1.digi.com/support/utilities/digi_apiframes.htm">Digi API Packet Generator for ZB (ZigBee)
 *      Networks</a>
 * @see <a href="http://examples.digi.com/wp-content/uploads/2012/07/XBee_ZB_ZigBee_AT_Commands.pdf">
 *      XBee_ZB_ZigBee_AT_Commands</a>
 */
public class AtCommandQueueRegisterValue extends AbstractAtCommand {
	private final byte frameType = 0x09;

	/**
	 * @param All
	 *            parameters without predefined values
	 */
	public AtCommandQueueRegisterValue(byte frameId, short atCommand, byte[] parameterValueArray) {
		this.frameId = frameId;
		this.atCommand = atCommand;
		dataArray = ByteBuffer.wrap(parameterValueArray);
		message = generateMessage();
	}

	/**
	 * @param payload
	 *            The complete content of the message (without frame start delimiter, length and checksum).
	 * @throws WrongFormatException
	 */
	public AtCommandQueueRegisterValue(byte[] payload) throws WrongFormatException {
		ByteBuffer bb = ByteBuffer.wrap(payload);

		if (bb.get() != frameType) {
			throw new WrongFormatException("Wrong FrameType, not an ATCommandQuery!");
		}
		frameId = bb.get();
		atCommand = bb.getShort();
		byte[] temp = new byte[bb.capacity() - 4];
		bb.get(temp, 0, temp.length);
		dataArray = ByteBuffer.wrap(temp);
		message = generateMessage();
	}

	private ByteBuffer generateMessage() {
		ByteBuffer bb = ByteBuffer.allocate(4 + dataArray.limit());
		bb.put(frameType);
		bb.put(frameId);
		bb.putShort(atCommand);
		bb.put(dataArray.array());
		return bb;
	}

	@Override
	void setAtCommand(short atCommand) {
		this.atCommand = atCommand;
		message.position(2);
		message.putShort(atCommand);
	}

	@Override
	void setDataArray(byte[] dataArray) {
		this.dataArray = ByteBuffer.wrap(dataArray);
		message = generateMessage(); // Because the size could have changed
	}
}
