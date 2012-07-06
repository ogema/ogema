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
package org.ogema.driver.generic_zb.devices;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.driver.generic_zb.Constants;
import org.ogema.driver.generic_zb.Generic_ZbConfig;
import org.ogema.driver.generic_zb.Generic_ZbDevice;
import org.ogema.driver.generic_zb.Generic_ZbDriver;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;

public class ColorDimmableLight extends Generic_ZbDevice implements ResourceListener, ResourceStructureListener {

	private static final byte[] on = {}; // The command does not have a payload
	private static final ByteArrayValue ON = new ByteArrayValue(on);

	private static final byte[] off = {}; // The command does not have a payload
	private static final ByteArrayValue OFF = new ByteArrayValue(off);

	String topLevelName;
	private ElectricDimmer light;
	private BooleanResource onOff;
	private FloatResource intensity;
	private FloatResource onIntensity;
	private BooleanResource isOn;
	private Generic_ZbConfig offCmdConfig;
	private Generic_ZbConfig onCmdConfig;
	private Generic_ZbConfig toggleCmdConfig;
	private Generic_ZbConfig onOffConfig;
	private Generic_ZbConfig moveToLevelCmdConfig;
	private Generic_ZbConfig moveToColorCmdConfig;
	private Generic_ZbConfig onLevelCmdConfig;

	public ColorDimmableLight(Generic_ZbDriver driver, ApplicationManager appManager, Generic_ZbConfig config) {
		super(driver, appManager, config);
	}

	public ColorDimmableLight(Generic_ZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
	}

	private void addMandatoryChannels() {
		offCmdConfig = new Generic_ZbConfig();
		offCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		offCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		offCmdConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		offCmdConfig.channelAddress = Constants.OFF_CMD_ADDRESS;
		offCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		offCmdConfig.resourceName = generic_ZbConfig.resourceName;
		offCmdConfig.resourceName += "_TurnOff"; // In case of several devices with the same
		// resourceName
		offCmdConfig.chLocator = addChannel(offCmdConfig);

		onCmdConfig = new Generic_ZbConfig();
		onCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		onCmdConfig.channelAddress = Constants.ON_CMD_ADDRESS;
		onCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		onCmdConfig.resourceName = generic_ZbConfig.resourceName;
		onCmdConfig.resourceName += "_TurnOn"; // In case of several devices with the same
		// resourceName
		onCmdConfig.chLocator = addChannel(onCmdConfig);

		toggleCmdConfig = new Generic_ZbConfig();
		toggleCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		toggleCmdConfig.channelAddress = Constants.TOGGLE_CMD_ADDRESS;
		toggleCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		toggleCmdConfig.resourceName = generic_ZbConfig.resourceName;
		toggleCmdConfig.resourceName += "_Toggle"; // In case of several devices with the same
		// resourceName
		toggleCmdConfig.chLocator = addChannel(toggleCmdConfig);

		onOffConfig = new Generic_ZbConfig();
		onOffConfig.interfaceId = generic_ZbConfig.interfaceId;
		onOffConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onOffConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		onOffConfig.channelAddress = Constants.ON_OFF_ATTR_ADDRESS;
		onOffConfig.timeout = 10000;
		onOffConfig.resourceName = generic_ZbConfig.resourceName;
		onOffConfig.resourceName += "_OnOffAttribute"; // In case of several devices with the same
		// resourceName
		onOffConfig.chLocator = addChannel(onOffConfig);

		moveToLevelCmdConfig = new Generic_ZbConfig();
		moveToLevelCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		moveToLevelCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		moveToLevelCmdConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		moveToLevelCmdConfig.channelAddress = Constants.MOVE_TO_LEVEL_CMD_ADDRESS;
		moveToLevelCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		moveToLevelCmdConfig.resourceName = generic_ZbConfig.resourceName;

		moveToLevelCmdConfig.resourceName += "_MoveToLevel";

		moveToLevelCmdConfig.chLocator = addChannel(moveToLevelCmdConfig);

		onLevelCmdConfig = new Generic_ZbConfig();
		onLevelCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		onLevelCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onLevelCmdConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		onLevelCmdConfig.channelAddress = Constants.ONLEVEL_ATTR_ADDRESS;
		onLevelCmdConfig.timeout = 3200; // Not necessary because it's hard coded for Commands
		onLevelCmdConfig.resourceName = generic_ZbConfig.resourceName;

		onLevelCmdConfig.resourceName += "_OnLevel";

		onLevelCmdConfig.chLocator = addChannel(onLevelCmdConfig);

		// ZigBee Cluster Library page 318
		moveToColorCmdConfig = new Generic_ZbConfig();
		moveToColorCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		moveToColorCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		moveToColorCmdConfig.deviceId = Constants.COLOR_DIMMABLE_LIGHT;
		moveToColorCmdConfig.channelAddress = Constants.MOVE_TO_COLOR_CMD_ADDRESS;
		moveToColorCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		moveToColorCmdConfig.resourceName = generic_ZbConfig.resourceName;

		moveToColorCmdConfig.resourceName += "_MoveToColor";

		moveToColorCmdConfig.chLocator = addChannel(moveToColorCmdConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		light = resourceManager.createResource(generic_ZbConfig.resourceName, ElectricDimmer.class);
		light.activate(true);

		// The on/off switch
		onOff = (BooleanResource) light.onOffSwitch().stateControl().create();
		onOff.activate(true);
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		isOn = (BooleanResource) light.onOffSwitch().stateFeedback().create();
		isOn.activate(true);
		isOn.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		intensity = (FloatResource) light.setting().stateControl().create();
		intensity.activate(true);
		intensity.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		onIntensity = (FloatResource) light.setting().stateFeedback().create();
		onIntensity.activate(true);
		onIntensity.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// TODO add color resource

		// Add listener to register on/off commands
		light.addResourceListener(this, true);
		intensity.addResourceListener(this, false);
		light.addStructureListener(this);

	}

	@Override
	public ChannelLocator addChannel(Generic_ZbConfig config) {
		ChannelLocator channelLocator;// = driver.channelMap.get(config.resourceName);
		String[] splitAddress = config.channelAddress.split(":");
		channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfig = channelAccess.getChannelConfiguration(channelLocator);
		driver.channelMap.put(config.resourceName, channelLocator);
		switch (splitAddress[1]) {
		case "COMMAND":
			commandChannel.put(config.channelAddress, channelLocator);
			channelConfig.setSamplingPeriod(-1);
			try {
				channelAccess.addChannel(channelConfig);
			} catch (ChannelConfigurationException e) {
				e.printStackTrace();
			}
			addToUpdateListener(channelLocator);
			break;
		case "ATTRIBUTE":
			attributeChannel.put(config.channelAddress, channelLocator);
			timeout = config.timeout;
			channelConfig.setSamplingPeriod(timeout);

			try {
				channelAccess.addChannel(channelConfig);
			} catch (ChannelConfigurationException e) {
				e.printStackTrace();
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			}
			addToUpdateListener(channelLocator);
			break;
		default:
		}
		return channelLocator;
	}

	@Override
	public void resourceChanged(Resource resource) {

		if (resource.equals(onOff)) {
			if (onOff.getValue()) {
				try {
					channelAccess.setChannelValue(onCmdConfig.chLocator, ON);
				} catch (ChannelAccessException e) {
					try {
						// deleteChannel(moveToLevelCmdConfig);
						onCmdConfig.chLocator = addChannel(onCmdConfig);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
				}
			}
			else {
				try {
					channelAccess.setChannelValue(offCmdConfig.chLocator, OFF);
				} catch (ChannelAccessException e) {
					try {
						// deleteChannel(moveToLevelCmdConfig);
						onCmdConfig.chLocator = addChannel(onCmdConfig);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		if (resource.equals(intensity)) {
			Integer intensityInt = (int) (intensity.getValue() * 255);
			byte intensityByte = intensityInt.byteValue();
			byte[] payload = { intensityByte, 0x0000 }; // 8bit intensity + 16bit transition time
			try {
				channelAccess.setChannelValue(moveToLevelCmdConfig.chLocator, new ByteArrayValue(payload));
			} catch (ChannelAccessException e) {
				try {
					// deleteChannel(moveToLevelCmdConfig);
					moveToLevelCmdConfig.chLocator = addChannel(moveToLevelCmdConfig);
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
			}
		}
		// TODO add color resource
	}

	@Override
	public void updateChannelValue(String chAddr, Value value) {
		try {
			switch (chAddr) {
			case Constants.ON_OFF_ATTR_ADDRESS:
				SampledValue onoffState = channelAccess.getChannelValue(onOffConfig.chLocator);
				isOn.setValue(onoffState.getValue().getBooleanValue());
				break;
			case Constants.ONLEVEL_ATTR_ADDRESS:
				SampledValue onlevel = channelAccess.getChannelValue(onLevelCmdConfig.chLocator);
				float intens = onlevel.getValue().getFloatValue();
				intens /= 255.0f;
				onIntensity.setValue(intens);
				break;
			default:
				break;
			}
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		System.out.println("ColorDimmableLight: ResourceStructureEvent Type: " + event.getType());
		System.out.println("ColorDimmableLight: ResourceStructureEvent Changed: "
				+ event.getChangedResource().getLocation());
		System.out.println("ColorDimmableLight: ResourceStructureEvent Source: " + event.getSource().getLocation());
	}
}
