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

public class Remote extends SubDevice {

	private long btncnt = 0;
	private byte oldflag = 0x00;
	private int numOfSwitches;

	public Remote(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceAttributes.put((short) 0x0300, new DeviceAttribute((short) 0x0300, "batteryStatus", true, true));
		// Get number of button channels
		String[] channels = remoteDevice.getChannels();
		for (String channel : channels) {
			String[] splitChannel = channel.split(":");
			numOfSwitches = Integer.parseInt(splitChannel[2]) - Integer.parseInt(splitChannel[1]) + 1;
			if (splitChannel[0].equals("Sw") || splitChannel[0].equals("Btn")) {
				for (int i = 1; i <= numOfSwitches; i++) {
					deviceAttributes.put((short) i, new DeviceAttribute((short) i, "shortPressedButton_" + i, true,
							true));
					deviceAttributes.put((short) (i + 0x100), new DeviceAttribute((short) (i + 0x100),
							"longPressedButton_" + (i), true, true));
				}
			}
		}
	}

	@Override
	public void parseValue(StatusMessage msg) {

		if ((msg.msg_type & 0xF0) == 0x40) {
			final int btn_no = (msg.msg_data[0] & 0x3F);
			if ((msg.msg_data[0] & 0x40) > 0) {
				if (msg.msg_flag != oldflag) { // long press
					oldflag = msg.msg_flag;
					if ((msg.msg_flag & 0x20) > 0) {
						deviceAttributes.get((short) (btn_no + 0x100)).setValue(new BooleanValue(false)); // Release
						System.out.println("Long Pressed button: " + false);
					}
					else if (msg.msg_data[1] != btncnt) {
						deviceAttributes.get((short) (btn_no + 0x100)).setValue(new BooleanValue(true)); // Press
						System.out.println("Long Pressed button: " + true);
					}
				}
			}
			else if (msg.msg_data[1] != btncnt) { // short press
				BooleanValue oldValue = (BooleanValue) deviceAttributes.get((short) btn_no).getValue();
				if (oldValue == null)
					oldValue = new BooleanValue(false);
				deviceAttributes.get((short) btn_no).setValue(new BooleanValue(!oldValue.getBooleanValue())); // press
				System.out.println("Short Pressed button value: " + !oldValue.getBooleanValue());
				System.out.println("Short Pressed button count: " + btncnt);
			}
			String err_str = ((msg.msg_data[0] & 0x80) > 0) ? "low" : "ok";
			float batt = ((msg.msg_data[0] & 0x80) > 0) ? 5 : 95;
			System.out.println("Battery: " + err_str);
			deviceAttributes.get((short) 0x0300).setValue(new FloatValue(batt));
			btncnt = msg.msg_data[1];
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

		if ((msgType == 0x10 && ((contentType == 0x02) || (contentType == 0x03)))) {
			// Configuration response Message
			parseConfig(msg, cmd);
		}
		else {
			parseValue(msg);
		}
	}
}
