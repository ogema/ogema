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
import org.ogema.driver.hmhl.models.SmokeDetector;
import org.ogema.model.sensors.StateOfChargeSensor;

public class SmokeSensor extends HM_hlDevice {

	private BooleanResource smokeAlert;
	private FloatResource batteryStatus;

	public SmokeSensor(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public SmokeSensor(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator dl) {
		super(driver, appManager, dl);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		byte[] array = null;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		byte msgtype = array[array.length - 1];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		if (msgtype == 0x41) {
			long status = Converter.toLong(msg[2]);
			long err = Converter.toLong(msg[0]);

			String err_str = ((err & 0x80) > 0) ? "low" : "ok";
			float batt = ((err & 0x80) > 0) ? 5 : 95;
			System.out.println("State of Battery: " + err_str);
			batteryStatus.setValue(batt);

			if (status > 1) {
				System.out.println("Smoke Alert: true");
				smokeAlert.setValue(true);
			}
			else {
				System.out.println("Smoke Alert: false");
				smokeAlert.setValue(false);
			}
		}
	}

	private void addMandatoryChannels() {
		HM_hlConfig attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0001";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_HomeMatic_SmokeDetector";
		attributeConfig.chLocator = addChannel(attributeConfig);

		SmokeDetector smokeDetector = resourceManager.createResource(hm_hlConfig.resourceName, SmokeDetector.class);

		smokeAlert = (BooleanResource) smokeDetector.smokeAlert().create();
		smokeAlert.activate(true);
		//		smokeAlert.setValue(false);
		smokeAlert.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		StateOfChargeSensor eSens = (StateOfChargeSensor) smokeDetector.battery().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		batteryStatus.activate(true);
		//		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
	}

	@Override
	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_SMOKE_RES_NAME + config.deviceAddress.replace(':', '_');
	}

}
