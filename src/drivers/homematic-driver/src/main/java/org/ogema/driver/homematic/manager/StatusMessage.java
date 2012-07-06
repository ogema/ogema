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
package org.ogema.driver.homematic.manager;

import java.util.Arrays;

import org.ogema.driver.homematic.tools.Converter;

public class StatusMessage {

	public enum States {
		INVALID, VALID
	}

	public byte type = 0;
	public long rtoken;
	public String source;
	public String destination;
	public byte status = 0;
	public byte cond = 0;

	public long uptime = 0; // hmTime uptime in ms
	public long timestamp = 0; // SystemTime current in ms
	public long upTimestamp = 0; // Up since-date

	public String aeskey;

	public long rssi = 0;

	public long msg_len = 0;
	public long msg_num = 0;
	public byte msg_flag = 0;
	public byte msg_type = 0;

	public byte[] msg_data = null;

	StatusMessage() {
		// DKtor
	}

	StatusMessage(byte[] data) {

		type = data[0];
		int i_type = (type == 'E') ? 0 : 1;

		rtoken = Converter.toLong(data, 1, 3 + i_type);
		source = new String(Converter.toHexString(data, 17 + i_type, 3));
		destination = new String(Converter.toHexString(data, 20 + i_type, 3));

		cond = data[4 + i_type];
		status = data[5 + i_type];

		// Here comes the Uptime magic !!
		uptime = Converter.toLong(data, 6 + i_type, 4);
		timestamp = System.currentTimeMillis();
		upTimestamp = timestamp - uptime;

		// Here comes the AES-Key magic !!
		if (cond == 0x01)
			aeskey = new String("AESKey-" + Converter.toHexString(data, 10 + i_type, 1));

		rssi = Converter.toLong(data, 11 + i_type, 2) - 65536;

		msg_len = Converter.toLong(data[13 + i_type]);
		msg_num = Converter.toLong(data[14 + i_type]);
		msg_flag = data[15 + i_type];
		msg_type = data[16 + i_type];
		if (this.msg_len > 9)
			msg_data = Arrays.copyOfRange(data, 23 + i_type, (int) (14 + i_type + this.msg_len));
	}

}
