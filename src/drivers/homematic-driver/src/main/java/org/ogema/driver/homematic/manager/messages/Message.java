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
package org.ogema.driver.homematic.manager.messages;

import org.ogema.driver.homematic.manager.RemoteDevice;

public abstract class Message {

	protected long num = 1;
	protected String dest;
	protected long token;
	protected long timestampInSeconds;
	protected byte[] frame;
	protected RemoteDevice rdevice;

	protected Message(RemoteDevice rd) {
		this.rdevice = rd;
		this.setToken(calcToken());
		timestampInSeconds = System.currentTimeMillis() / 1000;
	}

	public long calcToken() {
		return ((System.nanoTime() / 1000) % 0xffffffffL);
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

	public long getToken() {
		return token;
	}

	private void setToken(long token) {
		this.token = token;
	}

	public String getDest() {
		return dest;
	}

	public byte[] getFrame() {
		return frame;
	}

	public byte[] getFrame(long num) {
		return frame;
	}

	public RemoteDevice getDevice() {
		return rdevice;
	}

	public void refreshMsg_num() {
		this.num = rdevice.getMsg_num();
	}
}
