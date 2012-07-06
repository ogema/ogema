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

/**
 * This class represents an AT ClusterCommand Response message. Use the message and the XBeeFrameFactory to generate the
 * AT ClusterCommand Response frame.
 * 
 * @author puschas
 * @see <a href="http://ftp1.digi.com/support/utilities/digi_apiframes.htm">Digi API Packet Generator for ZB (ZigBee)
 *      Networks</a>
 * @see <a href="http://examples.digi.com/wp-content/uploads/2012/07/XBee_ZB_ZigBee_AT_Commands.pdf">
 *      XBee_ZB_ZigBee_AT_Commands</a>
 */
public class AtCommandResponse extends AbstractAtCommand {
	public enum StatusEnum {
		OK, ERROR, INVALID_COMMAND, INVALID_PARAM, TX_FAILURE
	}

	private final byte frameType = (byte) 0x88;
	private byte commandStatus;

	/**
	 * @param All
	 *            parameters without predefined values
	 */
	public AtCommandResponse(byte frameId, short atCommand, StatusEnum commandStatus, byte[] commandDataArray) {
		this.frameId = frameId;
		this.atCommand = atCommand;

		switch (commandStatus) {
		case OK:
			this.commandStatus = 0x00;
			break;
		case ERROR:
			this.commandStatus = 0x01;
			break;
		case INVALID_COMMAND:
			this.commandStatus = 0x02;
			break;
		case INVALID_PARAM:
			this.commandStatus = 0x03;
			break;
		case TX_FAILURE:
			this.commandStatus = 0x04;
			break;
		default:
			this.commandStatus = 0x01;
			break;
		}

		this.dataArray = ByteBuffer.wrap(commandDataArray);
		message = generateMessage();
	}

	/**
	 * @param payload
	 *            The complete content of the message (without frame start delimiter, length and checksum).
	 * @throws WrongFormatException
	 */
	public AtCommandResponse(byte[] payload) throws WrongFormatException {
		ByteBuffer bb = ByteBuffer.wrap(payload);

		if (bb.get() != frameType) {
			throw new WrongFormatException();
		}
		frameId = bb.get();
		atCommand = bb.getShort();
		commandStatus = bb.get();
		byte[] temp = new byte[bb.capacity() - 5];
		bb.get(temp, 0, temp.length);
		dataArray = ByteBuffer.wrap(temp);
		message = generateMessage();
	}

	public StatusEnum getCommandStatus() {
		switch (commandStatus) {
		case 0:
			return StatusEnum.OK;
		case 1:
			return StatusEnum.ERROR;
		case 2:
			return StatusEnum.INVALID_COMMAND;
		case 3:
			return StatusEnum.INVALID_PARAM;
		case 4:
			return StatusEnum.TX_FAILURE;
		default:
			return StatusEnum.ERROR;
		}
	}

	public void setCommandStatus(byte commandStatus) {
		// TODO: Check commandStatus for valid value?
		this.commandStatus = commandStatus;
		message.position(4);
		message.put(commandStatus);
	}

	private ByteBuffer generateMessage() {
		ByteBuffer bb = ByteBuffer.allocate(5 + dataArray.limit());
		bb.put(frameType);
		bb.put(frameId);
		bb.putShort(atCommand);
		bb.put(commandStatus);
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
