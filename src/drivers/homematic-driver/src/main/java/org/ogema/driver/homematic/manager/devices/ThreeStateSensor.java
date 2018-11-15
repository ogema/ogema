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
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.Converter;

public class ThreeStateSensor extends SubDevice {
	
	// otherwise we assume it is a water sensor
	private final boolean isDoorWindowSensor;

	public ThreeStateSensor(RemoteDevice rd, boolean isDoorWindowSensor) {
		super(rd);
		this.isDoorWindowSensor = isDoorWindowSensor;
	}

	@Override
	protected void addMandatoryChannels() {
		String statusName = isDoorWindowSensor ? "WindowStatus" : "HighWater";
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, statusName, true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "BatteryStatus", true, true));
	}

	@Override
	public void parseValue(StatusMessage msg) {

		long state = 0;
		long err = 0;
		String state_str = "";
		float batt;

		if ((msg.msg_type == 0x10 && msg.msg_data[0] == 0x06) || (msg.msg_type == 0x02 && msg.msg_data[0] == 0x01)) {
			state = Converter.toLong(msg.msg_data[1]);
			err = Converter.toLong(msg.msg_data[2]);

		}
		else if (msg.msg_type == 0x41) {
			state = Converter.toLong(msg.msg_data[2]);
			err = Converter.toLong(msg.msg_data[0]);
		}

		String err_str = ((err & 0x80) > 0) ? "low" : "ok";
		batt = ((err & 0x80) > 0) ? 5 : 95;

		
		if (state == 0x00)
			state_str = isDoorWindowSensor ? "closed" : "dry";
		else if (state == 0x64)
			state_str =  isDoorWindowSensor ? "unknown" : "damp";  // FIXME 
		else if (state == 0xC8)
			state_str = isDoorWindowSensor ? "open" :  "wet";

		System.out.println("State of " + (isDoorWindowSensor ? "WindowStatus" : "HighWater") + ":" + state_str);
		System.out.println("State of Battery: " + err_str);
		deviceAttributes.get((short) 0x0001).setValue(new StringValue(state_str));
		deviceAttributes.get((short) 0x0002).setValue(new FloatValue(batt));
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
