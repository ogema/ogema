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
package org.ogema.driver.generic_zb.devices;

import static org.ogema.driver.xbee.Constants.COLOR_DIMMABLE_LIGHT_DEVICE_ID;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.driver.generic_zb.ChAddrConstants;
import org.ogema.driver.generic_zb.Generic_ZbConfig;
import org.ogema.driver.generic_zb.Generic_ZbDevice;
import org.ogema.driver.generic_zb.Generic_ZbDriver;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;

public class ColorDimmableLight extends Generic_ZbDevice {

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

	private String deviceName;
	private List<SampledValueContainer> onOffChannelList;

	public ColorDimmableLight(Generic_ZbDriver driver, ApplicationManager appManager, Generic_ZbConfig config) {
		super(driver, appManager, config);
	}

	public ColorDimmableLight(Generic_ZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator,
			String name) {
		super(driver, appManager, deviceLocator);
		deviceName = name;
		addMandatoryChannels();
	}

	private void addMandatoryChannels() {
		if (deviceName != null)
			generic_ZbConfig.resourceName = deviceName;

		offCmdConfig = new Generic_ZbConfig();
		offCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		offCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		offCmdConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		offCmdConfig.channelAddress = ChAddrConstants.OFF_CMD_ADDRESS;
		offCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		offCmdConfig.resourceName = generic_ZbConfig.resourceName;
		offCmdConfig.resourceName += "_TurnOff"; // In case of several devices with the same
		// resourceName
		offCmdConfig.chConfiguration = addChannel(offCmdConfig);

		onCmdConfig = new Generic_ZbConfig();
		onCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		onCmdConfig.channelAddress = ChAddrConstants.ON_CMD_ADDRESS;
		onCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		onCmdConfig.resourceName = generic_ZbConfig.resourceName;
		onCmdConfig.resourceName += "_TurnOn"; // In case of several devices with the same
		// resourceName
		onCmdConfig.chConfiguration = addChannel(onCmdConfig);

		toggleCmdConfig = new Generic_ZbConfig();
		toggleCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		toggleCmdConfig.channelAddress = ChAddrConstants.TOGGLE_CMD_ADDRESS;
		toggleCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		toggleCmdConfig.resourceName = generic_ZbConfig.resourceName;
		toggleCmdConfig.resourceName += "_Toggle"; // In case of several devices with the same
		// resourceName
		toggleCmdConfig.chConfiguration = addChannel(toggleCmdConfig);

		onOffConfig = new Generic_ZbConfig();
		onOffConfig.interfaceId = generic_ZbConfig.interfaceId;
		onOffConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onOffConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		onOffConfig.channelAddress = ChAddrConstants.ON_OFF_ATTR_ADDRESS;
		onOffConfig.timeout = 2000;
		onOffConfig.resourceName = generic_ZbConfig.resourceName;
		onOffConfig.resourceName += "_OnOffAttribute"; // In case of several devices with the same
		// resourceName
		onOffConfig.chConfiguration = addChannel(onOffConfig);

		// create container list for cyclic read
		ChannelLocator chloc = onOffConfig.chConfiguration.getChannelLocator();
		SampledValueContainer container = new SampledValueContainer(chloc);
		onOffChannelList = new ArrayList<SampledValueContainer>(1);
		onOffChannelList.add(container);

		moveToLevelCmdConfig = new Generic_ZbConfig();
		moveToLevelCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		moveToLevelCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		moveToLevelCmdConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		moveToLevelCmdConfig.channelAddress = ChAddrConstants.MOVE_TO_LEVEL_CMD_ADDRESS;
		moveToLevelCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		moveToLevelCmdConfig.resourceName = generic_ZbConfig.resourceName;

		moveToLevelCmdConfig.resourceName += "_MoveToLevel";

		moveToLevelCmdConfig.chConfiguration = addChannel(moveToLevelCmdConfig);

		onLevelCmdConfig = new Generic_ZbConfig();
		onLevelCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		onLevelCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onLevelCmdConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		onLevelCmdConfig.channelAddress = ChAddrConstants.ONLEVEL_ATTR_ADDRESS;
		onLevelCmdConfig.timeout = 2500; // Not necessary because it's hard coded for Commands
		onLevelCmdConfig.resourceName = generic_ZbConfig.resourceName;

		onLevelCmdConfig.resourceName += "_OnLevel";

		onLevelCmdConfig.chConfiguration = addChannel(onLevelCmdConfig);

		// ZigBee Cluster Library page 318
		moveToColorCmdConfig = new Generic_ZbConfig();
		moveToColorCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		moveToColorCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		moveToColorCmdConfig.deviceId = COLOR_DIMMABLE_LIGHT_DEVICE_ID;
		moveToColorCmdConfig.channelAddress = ChAddrConstants.MOVE_TO_COLOR_CMD_ADDRESS;
		moveToColorCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		moveToColorCmdConfig.resourceName = generic_ZbConfig.resourceName;

		moveToColorCmdConfig.resourceName += "_MoveToColor";

		moveToColorCmdConfig.chConfiguration = addChannel(moveToColorCmdConfig);

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
		onOff.addValueListener(new OnOffListener());
		intensity.addValueListener(new IntensityListener());

		updateOnOffFeedback();
	}

	@Override
	public ChannelConfiguration addChannel(Generic_ZbConfig config) {
		ChannelLocator channelLocator;
		ChannelConfiguration channelConfig = null;
		String[] splitAddress = config.channelAddress.split(":");
		channelLocator = createChannelLocator(config.channelAddress);
		if (driver.channelMap.containsKey(config.resourceName)) {
			System.out.println("Error, resourceName already taken.");
			return null;
		}
		switch (splitAddress[1]) {
		case "COMMAND":
			try {
				channelConfig = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, -1);
				addToUpdateListener(channelConfig);
				commandChannel.put(config.channelAddress, channelConfig);
				driver.channelMap.put(config.resourceName, channelConfig);
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
			break;
		case "ATTRIBUTE":
			try {
				channelConfig = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, config.timeout);
				addToUpdateListener(channelConfig);
				attributeChannel.put(config.channelAddress, channelConfig);
				driver.channelMap.put(config.resourceName, channelConfig);
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
			break;
		default:
		}
		
		return channelConfig;
	}

	class OnOffListener implements ResourceValueListener<BooleanResource> {
		@Override
		public void resourceChanged(BooleanResource resource) {

			if (resource.getValue()) {
				try {
					channelAccess.setChannelValue(onCmdConfig.chConfiguration, ON);
				} catch (ChannelAccessException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					channelAccess.setChannelValue(offCmdConfig.chConfiguration, OFF);
				} catch (ChannelAccessException e) {
					e.printStackTrace();
				}
			}
			updateOnOffFeedback();
		}
	}

	class IntensityListener implements ResourceValueListener<FloatResource> {
		@Override
		public void resourceChanged(FloatResource resource) {
			Integer intensityInt = (int) (intensity.getValue() * 255);
			byte intensityByte = intensityInt.byteValue();
			byte[] payload = { intensityByte, 0x0000 }; // 8bit intensity + 16bit transition time
			try {
				channelAccess.setChannelValue(moveToLevelCmdConfig.chConfiguration, new ByteArrayValue(payload));
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateOnOffFeedback() {
		try {
			channelAccess.readUnconfiguredChannels(onOffChannelList);
			SampledValueContainer onoffState = onOffChannelList.get(0);
			Value v = onoffState.getSampledValue().getValue();
			if (v != null) {
				isOn.setValue(v.getBooleanValue());
			}
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateChannelValue(String chAddr, Value value) {
		try {
			switch (chAddr) {
			case ChAddrConstants.ON_OFF_ATTR_ADDRESS:
				SampledValue onoffState = channelAccess.getChannelValue(onOffConfig.chConfiguration);
				Value v = onoffState.getValue();
				if (v != null) {
					isOn.setValue(v.getBooleanValue());
				}
				break;
			case ChAddrConstants.ONLEVEL_ATTR_ADDRESS:
				SampledValue onlevel = channelAccess.getChannelValue(onLevelCmdConfig.chConfiguration);
				v = onlevel.getValue();
				if (v != null) {
					float intens = v.getFloatValue();
					intens /= 255.0f;
					onIntensity.setValue(intens);
				}
				break;
			default:
				break;
			}
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}

	}
}
