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

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.driver.hmhl.models.WaterDetector;
import org.ogema.model.sensors.StateOfChargeSensor;

public class WaterSensor extends HM_hlDevice {

	private StringResource highWater;
	private FloatResource batteryStatus;

	public WaterSensor(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public WaterSensor(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "ATTRIBUTE:0001":
			highWater.setValue(value.getStringValue());
			break;
		case "ATTRIBUTE:0002":
			batteryStatus.setValue(value.getFloatValue());
			break;
		}
	}

	private void addMandatoryChannels() {
		HM_hlConfig attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0001";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_HighWater";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0002";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_BatteryStatus";
		attributeConfig.chLocator = addChannel(attributeConfig);

		WaterDetector threeStateDevice = resourceManager.createResource(hm_hlConfig.resourceName, WaterDetector.class);

		highWater = (StringResource) threeStateDevice.highWater().create();
		highWater.activate(true);
		//		highWater.setValue("dry");
		highWater.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		StateOfChargeSensor eSens = (StateOfChargeSensor) threeStateDevice.battery().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		batteryStatus.activate(true);
		//		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
	}

	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_THREE_STATE_NAME + config.deviceAddress.replace(':', '_');
	}
}
