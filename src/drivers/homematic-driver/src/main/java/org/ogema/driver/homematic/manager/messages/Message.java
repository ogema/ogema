/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.driver.homematic.manager.messages;

import java.util.Random;

import org.ogema.driver.homematic.manager.RemoteDevice;

public abstract class Message {

	protected int num = 1;
	protected String dest;
	protected int token;
	protected long timestampInSeconds;
	protected byte[] frame;
	protected RemoteDevice rdevice;
	Random random;

	protected Message(RemoteDevice rd) {
		this.random=new Random();
		this.rdevice = rd;
		// this.setToken(calcToken());
		this.token = calcToken();
		timestampInSeconds = System.currentTimeMillis() / 1000;
	}

	public int calcToken() {
		return random.nextInt();
//		return System.nanoTime();
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

	public int getToken() {
		return token;
	}

	// private void setToken(long token) {
	// this.token = token;
	// }

	public String getDest() {
		return dest;
	}

	public byte[] getFrame() {
		return frame;
	}

	public byte[] getFrame(int num) {
		return frame;
	}

	public RemoteDevice getDevice() {
		return rdevice;
	}

	public int refreshMsg_num() {
		return this.num = rdevice.getMsgNum();
	}
}
