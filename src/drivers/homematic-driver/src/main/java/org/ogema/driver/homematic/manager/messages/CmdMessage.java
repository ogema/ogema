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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.ogema.driver.homematic.manager.LocalDevice;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.tools.Converter;

public class CmdMessage extends Message {

	private byte flag;
	private byte type;
	public byte[] data;
	public int sentNum;

	private LocalDevice localDevice;

	public CmdMessage(LocalDevice localDevice, RemoteDevice rd, byte flag, byte type, String data) {
		super(rd);
		this.localDevice = localDevice;
		this.flag = flag;
		this.type = type;
		this.dest = rd.getAddress();
		// this.sentNum = rd.sentMsgNum;
		this.data = Converter.hexStringToByteArray(data);
	}

	public CmdMessage(LocalDevice localDevice, RemoteDevice rd, byte flag, byte type, byte[] data) {
		super(rd);
		this.localDevice = localDevice;
		this.flag = flag;
		this.type = type;
		this.dest = rd.getAddress();
		this.data = data;
	}

	@Override
	public byte[] getFrame() {
		return getFrame(this.num);
	}

	@Override
	public byte[] getFrame(int num) {
		this.num = num;
		ByteArrayOutputStream body = new ByteArrayOutputStream(1);
		body.write((byte) num);
		body.write(flag);
		body.write(type);
		try {
			body.write(Converter.hexStringToByteArray(localDevice.getOwnerid()));
			body.write(Converter.hexStringToByteArray(dest));
			body.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteBuffer message = ByteBuffer.allocate(64);
		message.put(Converter.hexStringToByteArray("53"));
		message.putInt(Converter.toInt(token));
		message.put(Converter.hexStringToByteArray("000000000001"));
		message.putInt(localDevice.getUptime());
		message.put((byte) (body.size()));
		message.put(body.toByteArray());

		byte[] frame = message.array();
		return frame;
	}
}
