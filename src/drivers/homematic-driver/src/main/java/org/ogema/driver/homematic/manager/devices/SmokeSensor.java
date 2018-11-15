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

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.Converter;

public class SmokeSensor extends SubDevice {

	public SmokeSensor(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "Temperature", true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "BatteryStatus", true, true));
	}

	@Override
	public void parseValue(StatusMessage msg) {
		if (msg.msg_type == 0x41) {
			long status = Converter.toLong(msg.msg_data[2]);
			long err = Converter.toLong(msg.msg_data[0]);

			String err_str = ((err & 0x80) > 0) ? "low" : "ok";
			float batt = ((err & 0x80) > 0) ? 5 : 95;
			System.out.println("State of Battery: " + err_str);
			deviceAttributes.get((short) 0x0003).setValue(new FloatValue(batt));

			if (status > 1) {
				System.out.println("Smoke Alert: true");
				deviceAttributes.get((short) 0x0001).setValue(new BooleanValue(true));
			}
			else {
				System.out.println("Smoke Alert: false");
				deviceAttributes.get((short) 0x0001).setValue(new BooleanValue(false));
			}
		}
	}

	@Override
	public void channelChanged(byte identifier, Value value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseMessage(StatusMessage msg, CmdMessage cmd) {
		byte msgType = msg.msg_type;
		byte contentType = msg.msg_data[0];

		if ((msgType == 0x10 && (contentType == 0x02) || (contentType == 0x03))) {
			// Configuration response Message
			parseConfig(msg, cmd);
		}
		else {
			parseValue(msg);
		}
	}
}
