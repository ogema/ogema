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
import org.ogema.driver.homematic.HMDriver;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.DeviceCommand;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.Converter;

public class Thermostat extends SubDevice {

	private static final String THERMOSTAT_TYPE = "0095";

	public Thermostat(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceCommands.put((byte) 0x01, new DeviceCommand(this, (byte) 0x01, "desiredTemp", true));
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "desiredTemp", true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "currentTemp", true, true));
		deviceAttributes.put((short) 0x0003, new DeviceAttribute((short) 0x0003, "ValvePosition", true, true));
		deviceAttributes.put((short) 0x0004, new DeviceAttribute((short) 0x0004, "batteryStatus", true, true));
	}

	public void parseMessage(StatusMessage msg, CmdMessage cmd) {
		byte msgType = msg.msg_type;
		byte contentType = msg.msg_data[0];
		if ((msgType == 0x10 && contentType == 0x0A) || (msgType == 0x02 && contentType == 0x01)) {
			parseValue(msg);
		}
		else if ((msgType == 0x10 && (contentType == 0x02) || (contentType == 0x03))) {
			// Configuration response Message
			parseConfig(msg, cmd);
		}
		else if (msgType == 0x59) { // inform about new value
			// TODO: team msg
		}
		else if (msgType == 0x3F) { // Timestamp request important!
			// TODO: push @ack,$shash,"${mNo}803F$ioId${src}0204$s2000";
		}
	}

	public void parseValue(StatusMessage msg) {
		if (!remoteDevice.getDeviceType().equals(THERMOSTAT_TYPE))
			return;
		byte msgType = msg.msg_type;
		float bat = 0;
		float remoteCurrentTemp = 0;
		long desTemp = 0;
		long valvePos = 0;
		long err = 0;
		String err_str = "";
		long ctrlMode = 0;
		String ctrlMode_str = "";

		if (msgType == 0x10) {
			bat = ((float) (Converter.toLong(msg.msg_data[3] & 0x1F))) / 10 + 1.5F;
			remoteCurrentTemp = ((float) (Converter.toLong(msg.msg_data, 1, 2) & 0x3FF)) / 10;
			desTemp = (Converter.toLong(msg.msg_data, 1, 2) >> 10);
			valvePos = Converter.toLong(msg.msg_data[4] & 0x7F);
			err = Converter.toLong(msg.msg_data[3] >> 5);
		}
		else {
			desTemp = Converter.toLong(msg.msg_data, 1, 2);
			err = Converter.toLong(msg.msg_data[3] >> 1);
		}
		float remoteDesiredTemp = (desTemp & 0x3f) / 2;
		deviceAttributes.get((short) 0x0001).setValue(new FloatValue(remoteDesiredTemp));
		err = err & 0x7;
		ctrlMode = Converter.toLong((msg.msg_data[5] >> 6) & 0x3);

		if (msg.msg_len >= 7) { // Messages with Party Mode
			// TODO: Implement Party features
		}

		if ((msg.msg_len >= 6) && (ctrlMode == 3)) { // Msg with Boost
			// TODO: Calculation with Boost Time
		}
		switch (Converter.toInt(err)) {
		case 0:
			err_str = "OK";
			break;
		case 1:
			err_str = "ralve tight";
			break;
		case 2:
			err_str = "adjust range too large";
			break;
		case 3:
			err_str = "adjust range too small";
			break;
		case 4:
			err_str = "communication error";
			break;
		case 5:
			err_str = "unknown";
			break;
		case 6:
			err_str = "low Battery";
			break;
		case 7:
			err_str = "valve error position";
			break;
		}

		switch (Converter.toInt(ctrlMode)) {
		case 0:
			ctrlMode_str = "auto";
			break;
		case 1:
			ctrlMode_str = "manual";
			break;
		case 2:
			ctrlMode_str = "party(urlaub)";
			break;
		case 3:
			ctrlMode_str = "boost";
			break;
		default:
			ctrlMode_str = Long.toHexString(ctrlMode);
			break;
		}

		HMDriver.logger.debug("Measured Temperature: " + remoteCurrentTemp + " C");
		deviceAttributes.get((short) 0x0002).setValue(new FloatValue(remoteCurrentTemp));
		HMDriver.logger.debug("Desired Temperature: " + remoteDesiredTemp + " C");
		HMDriver.logger.debug("Battery Voltage: " + bat + " V");
		deviceAttributes.get((short) 0x0004).setValue(new FloatValue(bat));
		HMDriver.logger.debug("Valve Position: " + valvePos + " %");
		deviceAttributes.get((short) 0x0003).setValue(new FloatValue(valvePos / 100));
		HMDriver.logger.debug("Error: " + err_str);
		HMDriver.logger.debug("Control Mode: " + ctrlMode_str);
	}

	@Override
	public void channelChanged(byte identifier, Value value) {
		if (identifier == 0x01) { // desiredTemp
			float localDesiredTemp = value.getFloatValue();
			localDesiredTemp = (float) (Math.ceil(localDesiredTemp * 2) / 2);
			if (localDesiredTemp > 31.5)
				localDesiredTemp = 31.5f;
			if (localDesiredTemp < 0)
				localDesiredTemp = 0;
			float f = (localDesiredTemp * 2.0f);
			int i = (int) f;
			byte b = (byte) (i & 0x000000FF);
			String bs = Converter.toHexString(b);
			// Syntax: Commando + Desiredtemp * 2 + Flag + Type
			this.remoteDevice.pushCommand((byte) 0xB0, (byte) 0x11, "8104" + bs);
		}
	}
}
