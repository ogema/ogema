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
