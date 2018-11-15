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
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.tools.resource.util.ResourceUtils;

// very similar to WaterSensor, same underlying low level device (ThreeStateSensor) -> unify?
public class DoorWindowSensor extends HM_hlDevice {

	private BooleanResource status;
	private FloatResource batteryStatus;

	public DoorWindowSensor(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public DoorWindowSensor(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "ATTRIBUTE:0001":
			status.setValue(value.getStringValue().equals("closed"));
			status.activate(true);
			break;
		case "ATTRIBUTE:0002":
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
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_WindowStatus";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0002";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_BatteryStatus";
		attributeConfig.chLocator = addChannel(attributeConfig);

        @SuppressWarnings("deprecation")
		org.ogema.driver.hmhl.models.DoorWindowContact threeStateDevice =
                resourceManager.createResource(hm_hlConfig.resourceName, org.ogema.driver.hmhl.models.DoorWindowContact.class);
		status = threeStateDevice.reading().create();
		// highWater.activate(true);
		// highWater.setValue("dry");
		status.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		StateOfChargeSensor eSens = threeStateDevice.battery().chargeSensor().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		// batteryStatus.activate(true);
		// batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		// do not activate value resources, since they do not contain sensible values yet
		ResourceUtils.activateComplexResources(threeStateDevice, true, appManager.getResourceAccess());
	}

	protected void unifyResourceName(HM_hlConfig config) {
		
		config.resourceName += Constants.HM_DOOR_CONTACT_RES_NAME + config.deviceAddress.replace(':', '_');
	}

	@Override
	protected void terminate() {
		removeChannels();
	}
}
