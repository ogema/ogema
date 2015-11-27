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
package org.ogema.driver.homematic.manager.devices;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.Converter;

public class CO2Detector extends SubDevice {

	public CO2Detector(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "Concentration", true, true));
	}

	@Override
	public void parseMessage(StatusMessage msg, CmdMessage cmd) {
		byte msgType = msg.msg_type;
		byte contentType = msg.msg_data[0];

		if (remoteDevice.getDeviceType().equals("0056") || remoteDevice.getDeviceType().equals("009F")) {
			if ((msg.msg_type == 0x02 && msg.msg_data[0] == 0x01) || (msg.msg_type == 0x10 && msg.msg_data[0] == 0x06)
					|| (msg.msg_type == 0x41)) {
				parseValue(msg);
			}
			else if ((msgType == 0x10 && (contentType == 0x02) || (contentType == 0x03))) {
				// Configuration response Message
				parseConfig(msg, cmd);
			}

		}
		// else if (msg.msg_type == 0x10 && msg.msg_data[0] == 0x06) {
		// // long err = Converter.toLong(msg[3]);
		// state = Converter.toLong(msg.msg_data[2]);
		// String state_str = (state > 2) ? "off" : "smoke-Alarm";
		//
		// System.out.println("Level: " + state);
		// deviceAttributes.get((short) 0x0001).setValue(new FloatValue(state));
		// // String err_str = ((err & 0x80) > 0) ? "low" : "ok";
		// System.out.println("State: " + state_str);
		// }
	}

	@Override
	public void channelChanged(byte identifier, Value value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseValue(StatusMessage msg) {
		long state = 0;
		state = Converter.toLong(msg.msg_data[2]);

		if (remoteDevice.getDeviceType().equals("009F"))
			System.out.println("Level: " + state);
		System.out.println("State: " + state);
		deviceAttributes.get((short) 0x0001).setValue(new FloatValue(state));
		System.out.println("#######################\tCO2\t#############################");
		System.out.println("Concentration: " + state);
	}

}
