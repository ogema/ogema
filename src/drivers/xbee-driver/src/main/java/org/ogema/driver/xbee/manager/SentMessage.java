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

public final class SentMessage {
	private long timestampInSeconds;
	private final byte[] message;
	private final Object channelLock;
	private final byte responseType;
	private final byte frameId;

	public SentMessage(byte[] message, byte responseType, Object channelLock) {
		this.message = message;
		this.channelLock = channelLock;
		this.responseType = responseType;
		timestampInSeconds = System.currentTimeMillis() / 1000;
		frameId = message[1];
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

	public byte getResponseType() {
		return responseType;
	}

	public byte[] getMessage() {
		return message;
	}

	public Object getChannelLock() {
		return channelLock;
	}

	public byte getFrameId() {
		return frameId;
	}
}
