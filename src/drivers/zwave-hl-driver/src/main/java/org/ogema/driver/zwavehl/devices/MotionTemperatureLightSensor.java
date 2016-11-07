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
package org.ogema.driver.zwavehl.devices;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.BrightnessResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.zwavehl.ZWaveHlConfig;
import org.ogema.driver.zwavehl.ZWaveHlDevice;
import org.ogema.driver.zwavehl.ZWaveHlDriver;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 * see e.g. http://www.fibaro.com/en/the-fibaro-system/motion-sensor
 * 
 * TODO temperature and luminance not reported yet -> fix settings
 * TODO many more channels available...
 */
public class MotionTemperatureLightSensor extends ZWaveHlDevice {

	private TemperatureResource tempSens;
	private BooleanResource motionSens;
	private BrightnessResource lightSens;
	private FloatResource batteryStatus;
	private TimeResource wakeUpInterval;

	public MotionTemperatureLightSensor(ZWaveHlDriver driver, ApplicationManager appManager, ZWaveHlConfig config) {
		super(driver, appManager, config);
	}

	public MotionTemperatureLightSensor(ZWaveHlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		// System.out.println("    vvv parse value for motion sensor... " + channelAddress + "; value: " + value.getStringValue());
		switch (channelAddress) {
		case "0031:0001:0001":
			tempSens.setValue(value.getFloatValue()+273.15F);
			tempSens.activate(false);
			break;
		case "0031:0001:0003":
			lightSens.setValue(value.getFloatValue());
			lightSens.activate(false);
			break;
		case "0080:0001:0000":
			Byte bb = value.getByteArrayValue()[0];
			batteryStatus.setValue(bb.intValue()/100);
			batteryStatus.activate(false);
			break;
		case "0030:0001:0000":
			motionSens.setValue(value.getBooleanValue());
			motionSens.activate(false);
			break;
		case "0084:0001:0000":
			wakeUpInterval.setValue(value.getLongValue());
			wakeUpInterval.activate(false);
			break;
		default: 
			System.out.println("new channel " + channelAddress);
		}
	}

	private ZWaveHlConfig createConfig(String channel, String name) {
		ZWaveHlConfig config = new ZWaveHlConfig();
		config.driverId = zwaveHlConfig.driverId;
		config.interfaceId = zwaveHlConfig.interfaceId;
		config.deviceAddress = zwaveHlConfig.deviceAddress;
		config.channelAddress = channel;
		config.resourceName = zwaveHlConfig.resourceName + "_" + name; // does this make a differnece?
		config.chConfiguration = addChannel(config);
		return config;
	}
	
	protected void init() {
		
		SensorDevice sensorDevice = resourceManager.createResource(zwaveHlConfig.resourceName, SensorDevice.class);
		sensorDevice.sensors().<ResourceList<Sensor>> create().setElementType(Sensor.class);
		tempSens = sensorDevice.sensors().getSubResource("temperatureSensor", TemperatureSensor.class).reading().create();
		motionSens = sensorDevice.sensors().getSubResource("motionSensor", MotionSensor.class).reading().create();
		lightSens = sensorDevice.sensors().getSubResource("lightSensor", LightSensor.class).reading().create();
		batteryStatus = sensorDevice.electricityStorage().chargeSensor().reading().create();
		wakeUpInterval = sensorDevice.addDecorator("wakeUpInterval", TimeResource.class);

		ResourceUtils.activateComplexResources(sensorDevice, true, appManager.getResourceAccess());
		tempSens.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		motionSens.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		lightSens.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		wakeUpInterval.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
	}
	
	@Override
	public void channelFound(ChannelLocator channel) {
		String ch = channel.getChannelAddress();
		logger.debug(String.format("Channel %s detected!", ch));
		switch (ch) {
		case "0031:0001:0001": 
			createConfig(ch,"Temperature");
			break;
		case "0031:0001:0003": 
			createConfig(ch,"Luminance");
			break;
		case "0080:0001:0000": 
			createConfig(ch,"Battery");
			break;
		case "0030:0001:0000":
			createConfig(ch,"Motion");
			break;
		case "0084:0001:0000": 
			createConfig(ch,"WakeUpInterval");
			break;
		default:
			logger.warn(String.format("Unexpected channel %s detected!", ch)); // TODO there are actually many more channels
			break;
		}
	}

	@Override
	protected void unifyResourceName(ZWaveHlConfig config) {
		config.resourceName = "ZWave_" + config.resourceName.replace('-', '_').replace('.', '_') + config.deviceAddress;
	}

/*	@Override
	public void resourceChanged(BooleanResource resource) {
		ChannelLocator locator;
		BooleanValue newState = new BooleanValue(resource.getValue());
		if (resource.equals(onOff[0])) {
			locator = this.valueChannel.get("0025:0001:0000");
			writeToChannel(locator, newState);
		}
		else if (resource.equals(onOff[1])) {
			locator = this.valueChannel.get("0025:0002:0000");
			writeToChannel(locator, newState);
		}
		else if (resource.equals(onOff[2])) {
			locator = this.valueChannel.get("0025:0003:0000");
			writeToChannel(locator, newState);
		}
	}
*/

	@Override
	protected void terminate() {
		// release the exclusive access by requesting READ_ONLY access.
		if (tempSens != null)
			tempSens.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		if (lightSens != null)
			lightSens.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		if (motionSens != null)
			motionSens.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		if (batteryStatus != null)
			batteryStatus.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		if (wakeUpInterval != null)
			wakeUpInterval.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		removeChannels();
	}

	@Override
	public void finished(boolean success) {

	}

	@Override
	public void progress(float ratio) {

	}
}
