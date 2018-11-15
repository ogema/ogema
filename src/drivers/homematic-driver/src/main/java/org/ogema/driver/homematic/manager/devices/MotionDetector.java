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
import org.ogema.driver.homematic.HMDriver;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driver.homematic.manager.StatusMessage;
import org.ogema.driver.homematic.manager.SubDevice;
import org.ogema.driver.homematic.manager.messages.CmdMessage;
import org.ogema.driver.homematic.tools.Converter;

public class MotionDetector extends SubDevice {

	private long old_cnt = 0;
	private boolean motionInRun = false;
	private Thread timer = new Thread();
	private int nextTr = 0;

	public MotionDetector(RemoteDevice rd) {
		super(rd);
	}

	@Override
	protected void addMandatoryChannels() {
		deviceAttributes.put((short) 0x0001, new DeviceAttribute((short) 0x0001, "motion", true, true));
		deviceAttributes.put((short) 0x0002, new DeviceAttribute((short) 0x0002, "brightness", true, true));
		deviceAttributes.put((short) 0x0003, new DeviceAttribute((short) 0x0003, "batteryStatus", true, true));
	}

	@Override
	public void parseValue(StatusMessage msg) {

		// long state = Converter.toLong(msg[2]); // Is also brightness
		if ((msg.msg_type == 0x10 || msg.msg_type == 0x02) && msg.msg_data[0] == 0x06 && msg.msg_data[1] == 0x01) {
			long err = Converter.toLong(msg.msg_data[3]);
			String err_str;
			// long brightness = Converter.toLong(msg[2]);

			if (remoteDevice.getDeviceType().equals("004A"))
				HMDriver.logger.debug("SabotageError: " + (((err & 0x0E) > 0) ? "on" : "off"));
			else
				HMDriver.logger.debug("Cover: " + (((err & 0x0E) > 0) ? "open" : "closed"));

			err_str = ((err & 0x80) > 0) ? "low" : "ok";
			float batt = ((err & 0x80) > 0) ? 5 : 95;
			HMDriver.logger.debug("Battery: " + err_str);
			deviceAttributes.get((short) 0x0003).setValue(new FloatValue(batt));
		}
		else if (msg.msg_type == 0x41) {
			long cnt = Converter.toLong(msg.msg_data[1]);
			long brightn = Converter.toLong(msg.msg_data[2]);
			switch (msg.msg_data[3]) {
			case (byte) 0x40:
				nextTr = 15;
				break;
			case (byte) 0x50:
				nextTr = 30;
				break;
			case (byte) 0x60:
				nextTr = 60;
				break;
			case (byte) 0x70:
				nextTr = 120;
				break;
			case (byte) 0x80:
				nextTr = 240;
				break;
			}

			if (cnt != old_cnt) {
				old_cnt = cnt;
				HMDriver.logger.info("State: motion");
				deviceAttributes.get((short) 0x0001).setValue(new BooleanValue(true));
				HMDriver.logger.info("MotionCount: " + cnt + " next Trigger: " + nextTr + "s");
				HMDriver.logger.info("Brightness: " + brightn);
				deviceAttributes.get((short) 0x0002).setValue(new FloatValue(brightn));
				if (timer.isAlive()) {
					motionInRun = true;
				}
				else {
					timer = new Thread() {
						@Override
						public void run() {
							boolean repeat = true;
							while (repeat) {
								try {
									Thread.sleep((nextTr + 1) * 1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if (motionInRun) {
									motionInRun = false;
								}
								else {
									repeat = false;
									deviceAttributes.get((short) 0x0001).setValue(new BooleanValue(false));
									HMDriver.logger.info("reset State: no motion");
								}
							}
						}
					};
					timer.setName("homematic-ll-timer");
					timer.start();

				}
			}
		}
		else if (msg.msg_type == 0x70 && msg.msg_data[0] == 0x7F) {
			// TODO: NYI
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
		else
			parseValue(msg);

	}
}
