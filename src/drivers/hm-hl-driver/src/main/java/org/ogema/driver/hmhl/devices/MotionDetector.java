/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.driver.hmhl.devices;

import org.apache.commons.lang3.ArrayUtils;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.Converter;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.GenericFloatSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.StateOfChargeSensor;

public class MotionDetector extends HM_hlDevice {

	private BooleanResource motion;
	private FloatResource batteryStatus;
	private FloatResource brightness;

	private long old_cnt = 0;
	private boolean motionInRun = false;
	private Thread timer = new Thread();
	private int nextTr = 0;

	public MotionDetector(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public MotionDetector(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator dl) {
		super(driver, appManager, dl);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		byte[] array = null;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		else {
			throw new IllegalArgumentException("unsupported value type: " + value);
		}
		byte msgtype = array[array.length - 1];
		// byte msgflag = array[array.length - 2];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		//		long state = Converter.toLong(msg[2]); // Is also brightness

		if ((msgtype == 0x10 || msgtype == 0x02) && msg[0] == 0x06 && msg[1] == 0x01) {
			long err = Converter.toLong(msg[3]);
			String err_str;
			// long brightness = Converter.toLong(msg[2]);

			if (type.equals("004A"))
				System.out.println("SabotageError: " + (((err & 0x0E) > 0) ? "on" : "off"));
			else
				System.out.println("Cover: " + (((err & 0x0E) > 0) ? "open" : "closed"));

			err_str = ((err & 0x80) > 0) ? "low" : "ok";
			float batt = ((err & 0x80) > 0) ? 5 : 95;
			System.out.println("Battery: " + err_str);
			batteryStatus.setValue(batt);
		}
		else if (msgtype == 0x41) {
			long cnt = Converter.toLong(msg[1]);
			long brightn = Converter.toLong(msg[2]);
			switch (msg[3]) {
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
				System.out.println("State: motion");
				motion.setValue(true);
				System.out.println("MotionCount: " + cnt + " next Trigger: " + nextTr + "s");
				System.out.println("Brightness: " + brightn);
				brightness.setValue(brightn);
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
									motion.setValue(false); // release
									System.out.println("reset State: no motion");
								}
							}
						}
					};
					timer.start();

				}
			}
		}
		else if (msgtype == 0x70 && msg[0] == 0x7F) {
			// TODO: NYI
		}
	}

	private void addMandatoryChannels() {
		HM_hlConfig attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0001";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_State";
		attributeConfig.chLocator = addChannel(attributeConfig);

		HM_hlConfig commandConfig = new HM_hlConfig();
		commandConfig.driverId = hm_hlConfig.driverId;
		commandConfig.interfaceId = hm_hlConfig.interfaceId;
		commandConfig.channelAddress = "COMMAND:01";
		commandConfig.resourceName = hm_hlConfig.resourceName + "_Command";
		commandConfig.chLocator = addChannel(commandConfig);

		SensorDevice motionDetector = resourceManager.createResource(hm_hlConfig.resourceName, SensorDevice.class);

		motionDetector.sensors().create();
		motionDetector.sensors().activate(false);

		MotionSensor motionSensor = motionDetector.sensors().addDecorator("motion", MotionSensor.class);
		//TODO: can this be replaced with LightSensor (brightness in Lux)?
		GenericFloatSensor brightnessSensor = motionDetector.sensors().addDecorator("brightness",
				GenericFloatSensor.class);

		motion = motionSensor.reading().create();
		motion.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		motion.activate(true);
		motionSensor.activate(true);

		brightness = (FloatResource) brightnessSensor.reading().create();
		brightness.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		brightness.setValue(0);
		brightness.activate(true);
		brightnessSensor.activate(true);

		ElectricityStorage battery = motionDetector.electricityStorage().create();
		battery.type().setValue(1); //magic number: generic battery
		battery.type().activate(false);
		battery.activate(false);
		StateOfChargeSensor eSens = battery.chargeSensor().create();
		batteryStatus = eSens.reading().create();
		batteryStatus.activate(true);
		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		eSens.activate(false);
	}

	@Override
	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_MOTION_RES_NAME + config.deviceAddress.replace(':', '_');
	}
}
