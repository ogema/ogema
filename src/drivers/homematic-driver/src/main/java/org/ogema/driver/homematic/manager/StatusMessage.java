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
package org.ogema.driver.homematic.manager;

import java.util.Arrays;

import org.ogema.driver.homematic.tools.Converter;

public class StatusMessage {

	public byte type = 0;
	Integer rtoken;
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
	public int msg_num = 0;
	public byte msg_flag = 0;
	public byte msg_type = 0;
	public byte[] msg_data = null;

	public byte[] msg_all = null;

	public boolean partyMode = false;
	private boolean isEmpty = false;

	StatusMessage() {
		isEmpty = true;
	}

	// E-Message: [0x45 SourceAddr(1.-3.) Cond(4.) Status(5.) Uptime(6.-9.) AES-Key(10.) RSSI(11.-12.) lenght(13.)
	// MsgNumber(14.) Flag(15.) MsgType(16.) SourceAddr(17.-19.) DestinationAddr(20.-22.) Data(23.-(len+13).)

	StatusMessage(byte[] data) {

		type = data[0];
		int i_type = (type == 'E') ? 0 : 1;

		rtoken = (int) Converter.toLong(data, 1, 3 + i_type);
		source = Converter.toHexString(data, 17 + i_type, 3);
		destination = Converter.toHexString(data, 20 + i_type, 3);

		cond = data[4 + i_type];
		status = data[5 + i_type];

		// Here comes the Uptime magic !!
		uptime = Converter.toLong(data, 6 + i_type, 4);
		timestamp = System.currentTimeMillis();
		upTimestamp = timestamp - uptime;

		// Here comes the AES-Key magic !!
		if (cond == 0x01)
			aeskey = "AESKey-" + Converter.toHexString(data, 10 + i_type, 1);

		rssi = Converter.toLong(data, 11 + i_type, 2) - 65536;

		msg_len = Converter.toLong(data[13 + i_type]);
		msg_num = Converter.toInt(data[14 + i_type]);
		msg_flag = data[15 + i_type];
		msg_type = data[16 + i_type];
		if (this.msg_len > 9)
			msg_data = Arrays.copyOfRange(data, 23 + i_type, (int) (14 + i_type + this.msg_len));
		msg_all = Arrays.copyOfRange(data, 13 + i_type, (int) (14 + i_type + this.msg_len));

		if (source.equals(destination))
			partyMode = true;
	}

	// public boolean almostEquals(StatusMessage emsg) {
	// if (isEmpty)
	// return false;
	// else if (rtoken == emsg.rtoken && Arrays.equals(msg_all, emsg.msg_all))
	// return true;
	// else
	// return false;
	// }

	public String parseType() {
		return Converter.toHexString(msg_data, 1, 2);
	}

	public String parseSerial() {
		return new String(Arrays.copyOfRange(msg_data, 3, 13));
	}

}
