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
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.tools.Converter;

public class ThreeStateSensor extends SubDevice {

	public ThreeStateSensor(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "HighWater", true, true));
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
			state_str = "dry";
		else if (state == 0x64)
			state_str = "damp";
		else if (state == 0xC8)
			state_str = "wet";

		System.out.println("State of HighWater: " + state_str);
		System.out.println("State of Battery: " + err_str);
		deviceAttributes.get((short) 0x0001).setValue(new StringValue(state_str));
		deviceAttributes.get((short) 0x0002).setValue(new FloatValue(batt));
	}

	@Override
	public void channelChanged(byte identifier, Value value) {
		// TODO Auto-generated method stub

	}

}
