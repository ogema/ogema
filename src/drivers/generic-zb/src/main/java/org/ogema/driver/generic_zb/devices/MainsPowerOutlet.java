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

import static org.ogema.driver.xbee.Constants.MAINS_POWER_OUTLET_DEVICE_ID;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.driver.generic_zb.ChAddrConstants;
import org.ogema.driver.generic_zb.Generic_ZbConfig;
import org.ogema.driver.generic_zb.Generic_ZbDevice;
import org.ogema.driver.generic_zb.Generic_ZbDriver;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;

public class MainsPowerOutlet extends Generic_ZbDevice implements ResourceListener {

	private static final byte[] on = {}; // The command does not have a payload
	private static final ByteArrayValue ON = new ByteArrayValue(on);

	private static final byte[] off = {}; // The command does not have a payload
	private static final ByteArrayValue OFF = new ByteArrayValue(off);

	private BooleanResource onOff;
	private BooleanResource isOn;
	private Generic_ZbConfig offCmdConfig;
	private Generic_ZbConfig onCmdConfig;
	private Generic_ZbConfig toggleCmdConfig;
	private Generic_ZbConfig onOffConfig;
	private SingleSwitchBox mainsPowerOutlet;
	private String deviceName;

	public MainsPowerOutlet(Generic_ZbDriver driver, ApplicationManager appManager, Generic_ZbConfig config) {
		super(driver, appManager, config);

	}

	public MainsPowerOutlet(Generic_ZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator,
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
		offCmdConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		offCmdConfig.channelAddress = ChAddrConstants.OFF_CMD_ADDRESS;
		offCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		offCmdConfig.resourceName = generic_ZbConfig.resourceName;

		offCmdConfig.resourceName += "_TurnOff"; // In case of several devices with the same
		// resourceName
		offCmdConfig.chConfiguration = addChannel(offCmdConfig);

		onCmdConfig = new Generic_ZbConfig();
		onCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		onCmdConfig.channelAddress = ChAddrConstants.ON_CMD_ADDRESS;
		onCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		onCmdConfig.resourceName = generic_ZbConfig.resourceName;

		onCmdConfig.resourceName += "_TurnOn"; // In case of several devices with the same
		// resourceName
		onCmdConfig.chConfiguration = addChannel(onCmdConfig);

		toggleCmdConfig = new Generic_ZbConfig();
		toggleCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		toggleCmdConfig.channelAddress = ChAddrConstants.TOGGLE_CMD_ADDRESS;
		toggleCmdConfig.timeout = -1; // Not necessary because it's hard coded for Commands
		toggleCmdConfig.resourceName = generic_ZbConfig.resourceName;

		toggleCmdConfig.resourceName += "_Toggle"; // In case of several devices with the same
		// resourceName
		toggleCmdConfig.chConfiguration = addChannel(toggleCmdConfig);

		onOffConfig = new Generic_ZbConfig();
		onOffConfig.interfaceId = generic_ZbConfig.interfaceId;
		onOffConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onOffConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		onOffConfig.channelAddress = ChAddrConstants.ON_OFF_ATTR_ADDRESS;
		onOffConfig.timeout = 10000;
		onOffConfig.resourceName = generic_ZbConfig.resourceName;

		onOffConfig.resourceName += "_OnOffAttribute"; // In case of several devices with the same
		// resourceName
		onOffConfig.chConfiguration = addChannel(onOffConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		mainsPowerOutlet = resourceManager.createResource(generic_ZbConfig.resourceName, SingleSwitchBox.class);
		mainsPowerOutlet.activate(true);

		// The on/off switch
		onOff = (BooleanResource) mainsPowerOutlet.onOffSwitch().create();
		onOff.activate(true);
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		// Add listener to register on/off commands
		onOff.addResourceListener(this, false);
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
	
	@Override
	public void resourceChanged(Resource resource) {
		try {
			if (resource.equals(onOff)) {
				if (onOff.getValue())
					channelAccess.setChannelValue(onCmdConfig.chConfiguration, ON);
				else
					channelAccess.setChannelValue(offCmdConfig.chConfiguration, OFF);
			}
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateChannelValue(String chAddr, Value value) {
		try {
			switch (chAddr) {
			case ChAddrConstants.ON_OFF_ATTR_ADDRESS:
				SampledValue onoffState = channelAccess.getChannelValue(onOffConfig.chConfiguration);
				isOn.setValue(onoffState.getValue().getBooleanValue());
				break;
			default:
				break;
			}
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}
}
