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

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.DeviceCommand;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.tools.Converter;

public class PowerMeter extends SubDevice {

	private byte random = (byte) 0x01;

	private BooleanValue isOn;

	public PowerMeter(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceCommands.put((byte) 0x01, new DeviceCommand(this, (byte) 0x01, "onOff", true));
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "isOn", true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "iRes", true, true));
		deviceAttributes.put((short) 0x0003, new DeviceAttribute((short) 0x0003, "vRes", true, true));
		deviceAttributes.put((short) 0x0004, new DeviceAttribute((short) 0x0004, "pRes", true, true));
		deviceAttributes.put((short) 0x0005, new DeviceAttribute((short) 0x0005, "fRes", true, true));
		deviceAttributes.put((short) 0x0006, new DeviceAttribute((short) 0x0006, "eRes", true, true));

		// Get state
		this.remoteDevice.pushCommand((byte) 0xA0, (byte) 0x01, "010E");
	}

	@Override
	public void parseValue(StatusMessage msg) {

		String state_str = "";
		String timedon;

		if ((msg.msg_type == 0x10 && msg.msg_data[0] == 0x06) || (msg.msg_type == 0x02 && msg.msg_data[0] == 0x01)) {
			// The whole button story
			long state = Converter.toLong(msg.msg_data[2]);
			if (state == 0x00) {
				state_str = "off";
				isOn = new BooleanValue(false);
				deviceAttributes.get((short) 0x0001).setValue(isOn);
			}
			else if (state == 0xC8) {
				state_str = "on";
				isOn = new BooleanValue(true);
				deviceAttributes.get((short) 0x0001).setValue(isOn);
			}

			long err = Converter.toLong(msg.msg_data[3]);
			timedon = ((err & 0x40) > 0) ? "running" : "off";

			System.out.println("State: " + state_str);
			System.out.println("Timed-on: " + timedon);
		}
		else if (msg.msg_type == 0x5E || msg.msg_type == 0x5F) {
			// The Metering Story
			float eCnt = ((float) Converter.toLong(msg.msg_data, 0, 3)) / 10;
			float power = ((float) Converter.toLong(msg.msg_data, 3, 3)) / 100;
			float current = ((float) Converter.toLong(msg.msg_data, 6, 2)) / 1;
			float voltage = ((float) Converter.toLong(msg.msg_data, 8, 2)) / 10;
			float frequence = ((float) Converter.toLong(msg.msg_data[10])) / 100 + 50;
			boolean boot = (Converter.toLong(msg.msg_data, 0, 3) & 0x800000) > 0;

			System.out.println("Energy Counter: " + eCnt + " Wh");
			deviceAttributes.get((short) 0x0006).setValue(new FloatValue(eCnt));
			System.out.println("Power: " + power + " W");
			deviceAttributes.get((short) 0x0004).setValue(new FloatValue(power));
			System.out.println("Current: " + current + " mA");
			deviceAttributes.get((short) 0x0002).setValue(new FloatValue(current));
			System.out.println("Voltage: " + voltage + " V");
			deviceAttributes.get((short) 0x0003).setValue(new FloatValue(voltage));
			System.out.println("Frequence: " + frequence + " Hz");
			deviceAttributes.get((short) 0x0005).setValue(new FloatValue(frequence));
			System.out.println("Boot: " + boot);
		}
	}

	@Override
	public void channelChanged(byte identifier, Value value) {
		if (identifier == 0x01) { // onOff
			// Toggle
			this.remoteDevice.pushCommand((byte) 0xA0, (byte) 0x3E, remoteDevice.getAddress() + "4001"
					+ Converter.toHexString(random++));
			// Get state
			this.remoteDevice.pushCommand((byte) 0xA0, (byte) 0x01, "010E");
		}
	}
}
