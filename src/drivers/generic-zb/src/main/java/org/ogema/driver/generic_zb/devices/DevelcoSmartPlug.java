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
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.driver.generic_zb.ChAddrConstants;
import org.ogema.driver.generic_zb.Generic_ZbConfig;
import org.ogema.driver.generic_zb.Generic_ZbDevice;
import org.ogema.driver.generic_zb.Generic_ZbDriver;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricFrequencySensor;
import org.ogema.model.sensors.ElectricVoltageSensor;

public class DevelcoSmartPlug extends Generic_ZbDevice implements ResourceValueListener<BooleanResource> {
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
	private Generic_ZbConfig meterVoltageRMS;
	private Generic_ZbConfig meterCurrentRMS;
	private Generic_ZbConfig meterLineFreq;
	private ElectricCurrentResource iRes;
	private SingleSwitchBox mainsPowerOutlet;
	private VoltageResource vRes;
	private FrequencyResource fRes;
	private String deviceName;
	private List<SampledValueContainer> onOffChannelList;

	public DevelcoSmartPlug(Generic_ZbDriver driver, ApplicationManager appManager, Generic_ZbConfig config) {
		super(driver, appManager, config);
		unifyResourceName(generic_ZbConfig);
	}

	public DevelcoSmartPlug(Generic_ZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator,
			String name) {
		super(driver, appManager, deviceLocator);
		deviceName = name;
		addMandatoryChannels();
	}

	private void addMandatoryChannels() {
		if (deviceName != null) {
			generic_ZbConfig.resourceName = deviceName;
			unifyResourceName(generic_ZbConfig);
		}
		offCmdConfig = new Generic_ZbConfig();
		offCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		offCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		offCmdConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		offCmdConfig.channelAddress = ChAddrConstants.OFF_CMD_ADDRESS;
		offCmdConfig.timeout = 0; // Not necessary because it's hard coded for Commands
		offCmdConfig.resourceName = generic_ZbConfig.resourceName;

		offCmdConfig.resourceName += "_TurnOff"; // In case of several devices with the same
		// resourceName
		offCmdConfig.chConfiguration = addChannel(offCmdConfig);

		onCmdConfig = new Generic_ZbConfig();
		onCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onCmdConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		onCmdConfig.channelAddress = ChAddrConstants.ON_CMD_ADDRESS;
		onCmdConfig.timeout = 0; // Not necessary because it's hard coded for Commands
		onCmdConfig.resourceName = generic_ZbConfig.resourceName;

		onCmdConfig.resourceName += "_TurnOn"; // In case of several devices with the same
		// resourceName
		onCmdConfig.chConfiguration = addChannel(onCmdConfig);

		toggleCmdConfig = new Generic_ZbConfig();
		toggleCmdConfig.interfaceId = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceAddress = generic_ZbConfig.interfaceId;
		toggleCmdConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		toggleCmdConfig.channelAddress = ChAddrConstants.TOGGLE_CMD_ADDRESS;
		toggleCmdConfig.timeout = 0; // Not necessary because it's hard coded for Commands
		toggleCmdConfig.resourceName = generic_ZbConfig.resourceName;

		toggleCmdConfig.resourceName += "_Toggle"; // In case of several devices with the same
		// resourceName
		toggleCmdConfig.chConfiguration = addChannel(toggleCmdConfig);

		onOffConfig = new Generic_ZbConfig();
		onOffConfig.interfaceId = generic_ZbConfig.interfaceId;
		onOffConfig.deviceAddress = generic_ZbConfig.interfaceId;
		onOffConfig.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
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

		meterVoltageRMS = new Generic_ZbConfig();
		meterVoltageRMS.interfaceId = generic_ZbConfig.interfaceId;
		meterVoltageRMS.deviceAddress = generic_ZbConfig.interfaceId;
		meterVoltageRMS.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		meterVoltageRMS.channelAddress = ChAddrConstants.DEVELCO_METER_VOLTAGE_ATTRIBUTE_ADDRESS;
		meterVoltageRMS.timeout = 20000;
		meterVoltageRMS.resourceName = generic_ZbConfig.resourceName;

		meterVoltageRMS.resourceName += "_MeterVoltageRMS"; // In case of several devices with the same
		// resourceName
		meterVoltageRMS.chConfiguration = addChannel(meterVoltageRMS);

		meterCurrentRMS = new Generic_ZbConfig();
		meterCurrentRMS.interfaceId = generic_ZbConfig.interfaceId;
		meterCurrentRMS.deviceAddress = generic_ZbConfig.interfaceId;
		meterCurrentRMS.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		meterCurrentRMS.channelAddress = ChAddrConstants.DEVELCO_METER_CURRENT_ATTRIBUTE_ADDRESS;
		meterCurrentRMS.timeout = 20000;
		meterCurrentRMS.resourceName = generic_ZbConfig.resourceName;

		meterCurrentRMS.resourceName += "_MeterCurrentRMS"; // In case of several devices with the same
		// resourceName
		meterCurrentRMS.chConfiguration = addChannel(meterCurrentRMS);

		meterLineFreq = new Generic_ZbConfig();
		meterLineFreq.interfaceId = generic_ZbConfig.interfaceId;
		meterLineFreq.deviceAddress = generic_ZbConfig.interfaceId;
		meterLineFreq.deviceId = MAINS_POWER_OUTLET_DEVICE_ID;
		meterLineFreq.channelAddress = ChAddrConstants.DEVELCO_METER_FREQUENCY_ATTRIBUTE_ADDRESS;
		meterLineFreq.timeout = 20000;
		meterLineFreq.resourceName = generic_ZbConfig.resourceName;
		meterLineFreq.resourceName += "_meterFreqRMS"; // In case of several devices with the same
		// resourceName
		meterLineFreq.chConfiguration = addChannel(meterLineFreq);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		mainsPowerOutlet = resourceManager.createResource(generic_ZbConfig.resourceName, SingleSwitchBox.class);
		mainsPowerOutlet.activate(true);

		// The connection attribute and its children, current, voltage, power, frequency
		ElectricityConnection conn = (ElectricityConnection) mainsPowerOutlet.electricityConnection().create();
		conn.activate(true);

		ElectricCurrentSensor iSens = (ElectricCurrentSensor) conn.currentSensor().create();
		iRes = (ElectricCurrentResource) iSens.reading().create();
		iRes.activate(true);
		iRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricVoltageSensor vSens = (ElectricVoltageSensor) conn.voltageSensor().create();
		vRes = (VoltageResource) vSens.reading().create();
		vRes.activate(true);
		vRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricFrequencySensor fSens = (ElectricFrequencySensor) conn.frequencySensor().create().create();
		fRes = (FrequencyResource) fSens.reading().create();
		fRes.activate(true);
		fRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// The on/off switch
		onOff = (BooleanResource) mainsPowerOutlet.onOffSwitch().stateControl().create();
		onOff.activate(true);
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		// Add listener to register on/off commands
		onOff.addValueListener(this);

		isOn = (BooleanResource) mainsPowerOutlet.onOffSwitch().stateFeedback().create();
		isOn.activate(true);
		isOn.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		updateOnOffState();
	}

	@Override
	public ChannelConfiguration addChannel(Generic_ZbConfig config) {
		String[] splitAddress = config.channelAddress.split(":");
		ChannelLocator channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfig = null;
		if (driver.channelMap.containsKey(config.resourceName)) {
			System.out.println("Error, resourceName already taken.");
			return null;
		}
		String type;
		if (splitAddress[1].equals("EXT")) {
			type = splitAddress[2];
		}
		else {
			type = splitAddress[1];
		}
		switch (type) {
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
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			}
			break;
		default:
		}
		return channelConfig;
	}

	@Override
	public void resourceChanged(BooleanResource resource) {
		try {
			if (resource.equals(onOff)) {
				if (onOff.getValue())
					channelAccess.setChannelValue(onCmdConfig.chConfiguration, ON);
				else
					channelAccess.setChannelValue(offCmdConfig.chConfiguration, OFF);
				updateOnOffState();
			}
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}

	private void updateOnOffState() {
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
	public void updateChannelValue(String chAddr, Value v) {
		if (v == null) {
			Generic_ZbDriver.logger.info(String.format("Develco smart plug %s channel delivered null value", chAddr));
			return;
		}
		else {
			switch (chAddr) {
			case ChAddrConstants.ON_OFF_ATTR_ADDRESS:
				// SampledValue onoffState = channelAccess.getChannelValue(onOffConfig.chLocator);
				// Value v = onoffState.getValue();
				isOn.setValue(v.getBooleanValue());
				break;
			case ChAddrConstants.DEVELCO_METER_CURRENT_ATTRIBUTE_ADDRESS:
				// SampledValue iRMS = channelAccess.getChannelValue(meterCurrentRMS.chLocator);
				// v=iRMS.getValue();
				iRes.setValue(v.getFloatValue() / 1000f);
				break;
			case ChAddrConstants.DEVELCO_METER_VOLTAGE_ATTRIBUTE_ADDRESS:
				// SampledValue vRMS = channelAccess.getChannelValue(meterVoltageRMS.chLocator);
				vRes.setValue(v.getFloatValue() / 10f);
				break;
			case ChAddrConstants.DEVELCO_METER_FREQUENCY_ATTRIBUTE_ADDRESS:
				// SampledValue f = channelAccess.getChannelValue(meterLineFreq.chLocator);
				fRes.setValue(((float) (v.getIntegerValue())) / 10.0f);
				break;
			default:
				break;
			}
		}
	}
}
