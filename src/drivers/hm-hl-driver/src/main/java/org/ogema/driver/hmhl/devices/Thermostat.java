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
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.driver.hmhl.pattern.ThermostatPattern;
import org.ogema.tools.resource.util.ResourceUtils;

public class Thermostat extends HM_hlDevice implements ResourceValueListener<TemperatureResource> {

	private final ResourcePatternAccess patAcc;
	private ThermostatPattern device;

	public Thermostat(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
		patAcc = appManager.getResourcePatternAccess();
		device = patAcc.createResource(appManager.getResourceManagement().getUniqueResourceName(
				hm_hlConfig.resourceName), ThermostatPattern.class);
		activate(device); // does not activate value resources
	}

	public Thermostat(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		patAcc = appManager.getResourcePatternAccess();
		device = patAcc.createResource(appManager.getResourceManagement().getUniqueResourceName(
				hm_hlConfig.resourceName), ThermostatPattern.class);
		activate(device);// does not activate value resources
		// device.model.activate(true);
		addMandatoryChannels();
	}

	// charge sensor missing?
	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "ATTRIBUTE:0001":
			device.remoteDesiredTemperature.setCelsius(value.getFloatValue());
			device.remoteDesiredTemperature.activate(true);
			break;
		case "ATTRIBUTE:0002":
			device.currentTemperature.setCelsius(value.getFloatValue());
			device.currentTemperature.activate(true);
			break;
		case "ATTRIBUTE:0003":
			device.valvePosition.setValue(value.getFloatValue());
			device.valvePosition.activate(true);
			break;
		case "ATTRIBUTE:0004":
			device.batteryVoltage.setValue(value.getFloatValue());
			device.batteryVoltage.activate(true);
			break;
		}
	}

	private void activate(ThermostatPattern device) {
		// do not activate value resources, since they do not contain sensible values yet
		ResourceUtils.activateComplexResources(device.model, true, appManager.getResourceAccess());
		device.model.valve().setting().controllable().setValue(false); // valve is not directly controllable, only via temp setting
		device.model.valve().setting().controllable().activate(false);
	}

	private void addMandatoryChannels() {
		HM_hlConfig attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0001";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_DesiredTemperature";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0002";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_CurrentTemperature";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0003";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_ValvePosition";
		attributeConfig.chLocator = addChannel(attributeConfig);

		attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0004";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_BatteryStatus";
		attributeConfig.chLocator = addChannel(attributeConfig);

		HM_hlConfig commandConfig = new HM_hlConfig();
		commandConfig.driverId = hm_hlConfig.driverId;
		commandConfig.interfaceId = hm_hlConfig.interfaceId;
		commandConfig.channelAddress = "COMMAND:01";
		commandConfig.timeout = -1;
		commandConfig.resourceName = hm_hlConfig.resourceName + "_DesiredTemp";
		commandConfig.chLocator = addChannel(commandConfig);

		// Add listener to register on/off commands
		device.localDesiredTemperature.addValueListener(this, false);
	}

	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_VALVE_RES_NAME + config.deviceAddress.replace(':', '_');
	}

	/**
	 * Listener called whenever the management system updated the temperature setpoint.
	 */
	@Override
	public void resourceChanged(TemperatureResource res) {
		float localDesiredTemp = res.getCelsius();
		ChannelConfiguration locator = this.commandChannel.get("COMMAND:01");
		writeToChannel(locator, new FloatValue(localDesiredTemp));
	}

	@Override
	protected void terminate() {
		removeChannels();
	}
}
