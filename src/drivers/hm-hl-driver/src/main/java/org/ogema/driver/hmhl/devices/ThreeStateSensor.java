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
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.Converter;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.driver.hmhl.models.ThreeStateSensorDataModel;
import org.ogema.model.sensors.StateOfChargeSensor;

public class ThreeStateSensor extends HM_hlDevice {

	private StringResource highWater;
	private FloatResource batteryStatus;

	public ThreeStateSensor(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public ThreeStateSensor(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		byte[] array = null;
		long state = 0;
		long err = 0;
		String state_str = "";
		float batt;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		byte msgtype = array[array.length - 1];
		// byte msgflag = array[array.length - 2];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		if ((msgtype == 0x10 && msg[0] == 0x06) || (msgtype == 0x02 && msg[0] == 0x01)) {
			state = Converter.toLong(msg[1]);
			err = Converter.toLong(msg[2]);

		}
		else if (msgtype == 0x41) {
			state = Converter.toLong(msg[2]);
			err = Converter.toLong(msg[0]);
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
		highWater.setValue(state_str);
		batteryStatus.setValue(batt);
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

		ThreeStateSensorDataModel threeStateDevice = resourceManager.createResource(hm_hlConfig.resourceName,
				ThreeStateSensorDataModel.class);

		highWater = (StringResource) threeStateDevice.highWater().create();
		highWater.activate(true);
		highWater.setValue("dry");
		highWater.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		StateOfChargeSensor eSens = (StateOfChargeSensor) threeStateDevice.battery().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		batteryStatus.activate(true);
		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
	}

	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_THREE_STATE_NAME + config.deviceAddress.replace(':', '_');
	}
}
