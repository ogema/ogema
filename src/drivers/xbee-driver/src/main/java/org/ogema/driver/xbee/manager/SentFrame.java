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

import org.ogema.driver.xbee.manager.InputHandler.ResponseType;

/**
 * Stores the sent frame with the time in seconds the frame was sent to the SerialPortWriter. The timestamp does NOT
 * represent the time when the frame was actually sent out the serial port.
 * 
 * @author puschas
 * 
 */
public final class SentFrame {
	private long timestampInSeconds;
	private final byte[] sentFrame;
	private int numberOfRetries;
	private final ResponseType responseType;
	private final byte frameId;

	public SentFrame(byte[] sentFrame, ResponseType responseType) {
		this.sentFrame = sentFrame;
		numberOfRetries = 0;
		timestampInSeconds = System.currentTimeMillis() / 1000;
		this.responseType = responseType;
		frameId = sentFrame[4];
	}

	/**
	 * 
	 * @return The timestamp when the frame was sent to the SerialWriter. Timestamp format in seconds since midnight,
	 *         January 1, 1970 UTC.
	 */
	public long getTimestampInSeconds() {
		return timestampInSeconds;
	}

	public byte[] getFrame() {
		return sentFrame;
	}

	/**
	 * Sets the timestamp to the current time in seconds since midnight, January 1, 1970 UTC.
	 */
	public void refreshTimestamp() {
		timestampInSeconds = System.currentTimeMillis() / 1000;
	}

	/**
	 * 
	 * @return The difference between the timestamp and now in seconds.
	 */
	public long getTimeDifference() {
		return (System.currentTimeMillis() / 1000) - timestampInSeconds;
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public void incrementNumberOfRetires() {
		++numberOfRetries;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public byte getFrameId() {
		return frameId;
	}
}
