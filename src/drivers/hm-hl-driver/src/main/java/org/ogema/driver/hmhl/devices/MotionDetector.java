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
package org.ogema.driver.hmhl.devices;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.GenericFloatSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.tools.resource.util.ResourceUtils;

public class MotionDetector extends HM_hlDevice {

	private BooleanResource motion;
	private FloatResource brightness;
	private FloatResource batteryStatus;

	public MotionDetector(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public MotionDetector(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator dl) {
		super(driver, appManager, dl);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "ATTRIBUTE:0001":
			motion.setValue(value.getBooleanValue());
			motion.activate(true);
			break;
		case "ATTRIBUTE:0002":
			brightness.setValue(value.getFloatValue());
			brightness.activate(true);
			break;
		case "ATTRIBUTE:0003":
			batteryStatus.setValue(value.getFloatValue());
			batteryStatus.activate(true);
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
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_Motion";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0002";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_Brightness";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0003";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_BatteryStatus";
		attributeConfig.chLocator = addChannel(attributeConfig);

		SensorDevice motionDetector = resourceManager.createResource(hm_hlConfig.resourceName, SensorDevice.class);
		motionDetector.sensors().create();
		;
		// motionDetector.sensors().activate(true);

		MotionSensor motionSensor = motionDetector.sensors().addDecorator("motion", MotionSensor.class);
		// TODO: can this be replaced with LightSensor (brightness in Lux)?
		GenericFloatSensor brightnessSensor = motionDetector.sensors().addDecorator("brightness",
				GenericFloatSensor.class);

		motion = motionSensor.reading().create();
		motion.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		// motion.activate(true);
		// motionSensor.activate(true);

		brightness = (FloatResource) brightnessSensor.reading().create();
		brightness.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		// brightness.setValue(0);
		// brightness.activate(true);
		// brightnessSensor.activate(true);

		ElectricityStorage battery = motionDetector.electricityStorage().create();
		battery.type().<IntegerResource> create().setValue(1); // magic number: generic battery
		battery.type().activate(false);
		// battery.activate(false);
		StateOfChargeSensor eSens = battery.chargeSensor().create();
		batteryStatus = eSens.reading().create();
		// batteryStatus.activate(true);
		//		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		// do not activate value resources, since they do not contain sensible values yet
		ResourceUtils.activateComplexResources(motionDetector, true, appManager.getResourceAccess());
		// eSens.activate(true);
	}

	@Override
	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_MOTION_RES_NAME + config.deviceAddress.replace(':', '_');
	}

	@Override
	protected void terminate() {
		removeChannels();
	}
}
