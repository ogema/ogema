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
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.Converter;

public class THSensor extends SubDevice {

	private FloatValue temperature;
	private FloatValue humidity;
	private FloatValue batteryStatus;

	public THSensor(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "Temperature", true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "Humidity", true, true));
		deviceAttributes.put((short) 0x0003, new DeviceAttribute((short) 0x0003, "BatteryStatus", true, true));
	}

	@Override
	public void parseValue(StatusMessage msg) {
		long temp = 0;
		long hum = 0;
		long err = 0;
		String err_str = "";
		float batt = 95;

		if (msg.msg_type == 0x70) {
			temp = Converter.toLong(msg.msg_data, 0, 2);
			err_str = ((temp & 0x8000) > 0) ? "low" : "ok";
			batt = ((temp & 0x8000) > 0) ? 5 : 95;
			if ((temp & 0x4000) > 0)
				temp -= 0x8000;

			if (msg.msg_data.length > 2)
				hum = Converter.toLong(msg.msg_data, 2, 1);
		}
		else if (msg.msg_type == 0x53) {
			temp = Converter.toLong(msg.msg_data, 2, 2);
			if ((temp & 0xC00) > 0)
				temp -= 0x10000;
			err = Converter.toLong(msg.msg_data[0]);
			err_str = ((err & 0x80) > 0) ? "low" : "ok";
			batt = ((temp & 0x8000) > 0) ? 5 : 95;
		}

		HMDriver.logger.debug("Temperatur: " + ((float) temp) / 10 + " C");
		HMDriver.logger.debug("State of Battery: " + err_str);
		temperature = new FloatValue(temp / 10f);
		if (hum < 100) {
			HMDriver.logger.debug("Humidity: " + hum + "%");
			humidity = new FloatValue(hum);
		}
		batteryStatus = new FloatValue(batt);
		deviceAttributes.get((short) 0x0001).setValue(temperature);
		deviceAttributes.get((short) 0x0002).setValue(humidity);
		deviceAttributes.get((short) 0x0003).setValue(batteryStatus);
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
