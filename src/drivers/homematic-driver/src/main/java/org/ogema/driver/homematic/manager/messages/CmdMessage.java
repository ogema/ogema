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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.ogema.driver.homematic.manager.LocalDevice;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.tools.Converter;

public class CmdMessage extends Message {

	private byte flag;
	private byte type;
	private String data;

	private LocalDevice localDevice;

	public CmdMessage(LocalDevice localDevice, RemoteDevice rd, byte flag, byte type, String data) {
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
	public byte[] getFrame(long num) {
		this.num = num;
		ByteArrayOutputStream body = new ByteArrayOutputStream(1);
		body.write((byte) num);
		body.write(flag);
		body.write(type);
		try {
			body.write(Converter.hexStringToByteArray(localDevice.getOwnerid()));
			body.write(Converter.hexStringToByteArray(dest));
			body.write(Converter.hexStringToByteArray(data));
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
