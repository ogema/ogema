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

public abstract class AbstractAtCommand extends AbstractXBeeMessage {
	private byte frameType;
	protected byte frameId;
	protected short atCommand;
	protected ByteBuffer dataArray;

	public byte getFrameType() {
		return frameType;
	}

	// This method shall not be inherited
	void setFrameType(byte frameType) {
		this.frameType = frameType;
		message.position(0);
		message.put(frameType);
	}

	public byte getFrameId() {
		return frameId;
	}

	public void setFrameId(byte frameId) {
		this.frameId = frameId;
		message.position(1);
		message.put(frameId);
	}

	public short getAtCommand() {
		return atCommand;
	}

	abstract void setAtCommand(short atCommand);

	/**
	 * The array after the AT command (Parameter value/Command data).
	 * 
	 * @return
	 */
	public byte[] getDataArray() {
		return dataArray.array();
	}

	/**
	 * The array after the AT command (Parameter value/Command data).
	 * 
	 * @return
	 */
	abstract void setDataArray(byte[] dataArray);
}
