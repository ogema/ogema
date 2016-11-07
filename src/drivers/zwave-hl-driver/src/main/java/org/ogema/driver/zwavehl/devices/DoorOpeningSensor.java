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
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.zwavehl.ZWaveHlConfig;
import org.ogema.driver.zwavehl.ZWaveHlDevice;
import org.ogema.driver.zwavehl.ZWaveHlDriver;
import org.ogema.model.sensors.DoorWindowSensor;
import org.ogema.model.sensors.GenericBinarySensor;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.tools.resource.util.ResourceUtils;

/**
 * 
 * @author baerthbn
 * 
 */
public class DoorOpeningSensor extends ZWaveHlDevice {

	private FloatResource battery;
	private BooleanResource doorOpen;
	private BooleanResource alarm;
	private DoorWindowSensor openingDevice;

	public DoorOpeningSensor(ZWaveHlDriver driver, ApplicationManager appManager, ZWaveHlConfig config) {
		super(driver, appManager, config);
	}

	public DoorOpeningSensor(ZWaveHlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		logger.debug(String.format("DoorOpeningSensor created %s", deviceLocator.toString()));
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "0030:0001:0000":
			doorOpen.setValue(value.getBooleanValue());
			doorOpen.activate(false);
			break;
		case "009C:0001:0000":
			Byte b = value.getByteArrayValue()[0];
			if (b.intValue() == 0)
				alarm.setValue(false);
			else
				alarm.setValue(true);
			alarm.activate(false);
			break;
		case "0080:0001:0000":
			Byte bb = value.getByteArrayValue()[0];
			battery.setValue(bb.intValue());
			battery.activate(false);
			break;
		}
	}

	protected void init() {
		logger.debug("DoorOpeningSensor created!");
		openingDevice = resourceManager.createResource(zwaveHlConfig.resourceName, DoorWindowSensor.class);
		openingDevice.activate(false);
	}

	private void initDoorOpen() {
		ZWaveHlConfig attributeConfig = new ZWaveHlConfig();
		attributeConfig.driverId = zwaveHlConfig.driverId;
		attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
		attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
		attributeConfig.channelAddress = "0030:0001:0000";
		attributeConfig.resourceName = zwaveHlConfig.resourceName + "_DoorOpen";
		attributeConfig.chConfiguration = addChannel(attributeConfig);

//		GenericBinarySensor dSens = (GenericBinarySensor) openingDevice.doorOpen().create();
		doorOpen = openingDevice.reading().create();
		doorOpen.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		try {
			readValue(attributeConfig.channelAddress);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initAlarm() {
		ZWaveHlConfig attributeConfig = new ZWaveHlConfig();
		attributeConfig = new ZWaveHlConfig();
		attributeConfig.driverId = zwaveHlConfig.driverId;
		attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
		attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
		attributeConfig.channelAddress = "009C:0001:0000";
		attributeConfig.resourceName = zwaveHlConfig.resourceName + "_Alarm";
		attributeConfig.chConfiguration = addChannel(attributeConfig);

		GenericBinarySensor aSens = openingDevice.alarm().create();
		openingDevice.alarm().activate(false);
		alarm = aSens.reading().create();
		alarm.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		try {
			readValue(attributeConfig.channelAddress);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initBatStat() {
		ZWaveHlConfig attributeConfig = new ZWaveHlConfig();
		attributeConfig = new ZWaveHlConfig();
		attributeConfig.driverId = zwaveHlConfig.driverId;
		attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
		attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
		attributeConfig.channelAddress = "0080:0001:0000";
		attributeConfig.resourceName = zwaveHlConfig.resourceName + "_BatteryStatus";
		attributeConfig.chConfiguration = addChannel(attributeConfig);

		StateOfChargeSensor eSens =  openingDevice.battery().chargeSensor().create();
		ResourceUtils.activateComplexResources(openingDevice.battery(), true, appManager.getResourceAccess());
		battery = eSens.reading().create();
		battery.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		try {
			readValue(attributeConfig.channelAddress);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void unifyResourceName(ZWaveHlConfig config) {
		config.resourceName = "ZWave_" + config.resourceName + config.deviceAddress.replace(':', '_');
	}

	@Override
	protected void terminate() {
		if (doorOpen != null)
			doorOpen.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		if (alarm != null)
			alarm.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		if (battery != null)
			battery.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		removeChannels();
	}

	@Override
	public void channelFound(ChannelLocator channel) {
		String ch = channel.getChannelAddress();
		logger.debug(String.format("Channel %s detected!", ch));
		switch (ch) {
		case "0080:0001:0000": // Battery status
			initBatStat();
			break;
		case "009C:0001:0000": // Alarm
			initAlarm();
			break;
		case "0030:0001:0000": // Door open
			initDoorOpen();
			break;
		default:
			logger.warn(String.format("Unexpected channel %s detected!", ch));
			break;
		}
	}

	@Override
	public void finished(boolean success) {

	}

	@Override
	public void progress(float ratio) {

	}
}
