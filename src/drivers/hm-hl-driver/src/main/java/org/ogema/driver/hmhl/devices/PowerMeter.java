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

import static org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType.FIXED_INTERVAL;

import org.apache.commons.lang3.ArrayUtils;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.Converter;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricFrequencySensor;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;
import org.ogema.model.sensors.EnergyAccumulatedSensor;

public class PowerMeter extends HM_hlDevice implements ResourceValueListener<BooleanResource> {

	private byte random = (byte) 0x01;

	private BooleanResource onOff;
	private BooleanResource isOn;
	private ElectricCurrentResource iRes;
	private VoltageResource vRes;
	private PowerResource pRes;
	private FrequencyResource fRes;
	private EnergyResource eRes;

	public PowerMeter(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public PowerMeter(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator dl) {
		super(driver, appManager, dl);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		byte[] array = null;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		else {
			throw new IllegalArgumentException("unsupported value type: " + value);
		}
		byte msgtype = array[array.length - 1];
		// byte msgflag = array[array.length - 2];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		String state_str = "";
		String timedon;

		if ((msgtype == 0x10 && msg[0] == 0x06) || (msgtype == 0x02 && msg[0] == 0x01)) {
			// The whole button story
			long state = Converter.toLong(msg[2]);
			if (state == 0x00) {
				state_str = "off";
				isOn.setValue(false);
			}
			else if (state == 0xC8) {
				state_str = "on";
				isOn.setValue(true);
			}

			long err = Converter.toLong(msg[3]);
			timedon = ((err & 0x40) > 0) ? "running" : "off";

			//			System.out.println("State: " + state_str);
			//			System.out.println("Timed-on: " + timedon);
		}
		else if (msgtype == 0x5E || msgtype == 0x5F) {
			// The Metering Story
			float eCnt = ((float) Converter.toLong(msg, 0, 3)) / 10;
			float power = ((float) Converter.toLong(msg, 3, 3)) / 100;
			float current = ((float) Converter.toLong(msg, 6, 2)) / 1;
			float voltage = ((float) Converter.toLong(msg, 8, 2)) / 10;
			float frequence = ((float) Converter.toLong(msg[10])) / 100 + 50;
			boolean boot = (Converter.toLong(msg, 0, 3) & 0x800000) > 0;

			//			System.out.println("Energy Counter: " + eCnt + " Wh");
			eRes.setValue(eCnt);
			//			System.out.println("Power: " + power + " W");
			pRes.setValue(power);
			//			System.out.println("Current: " + current + " mA");
			iRes.setValue(current);
			//			System.out.println("Voltage: " + voltage + " V");
			vRes.setValue(voltage);
			//			System.out.println("Frequence: " + frequence + " Hz");
			fRes.setValue(frequence);
			//			System.out.println("Boot: " + boot);
		}

	}

	private void addMandatoryChannels() {
		HM_hlConfig attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0001";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_Attribute";
		attributeConfig.chLocator = addChannel(attributeConfig);

		HM_hlConfig commandConfig = new HM_hlConfig();
		commandConfig.driverId = hm_hlConfig.driverId;
		commandConfig.interfaceId = hm_hlConfig.interfaceId;
		commandConfig.channelAddress = "COMMAND:01";
		commandConfig.resourceName = hm_hlConfig.resourceName + "_Command";
		commandConfig.chLocator = addChannel(commandConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		SingleSwitchBox powerMeter = resourceManager.createResource(hm_hlConfig.resourceName, SingleSwitchBox.class);
		//powerMeter.activate(true);

		// The on/off switch
		onOff = (BooleanResource) powerMeter.onOffSwitch().stateControl().create();
//		onOff.activate(true);
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		isOn = (BooleanResource) powerMeter.onOffSwitch().stateFeedback().create();
//		isOn.activate(true);
		isOn.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// The connection attribute and its children, current, voltage, power,
		// frequency
		ElectricityConnection conn = powerMeter.electricityConnection().create();
		conn.activate(true);

		ElectricCurrentSensor iSens = conn.currentSensor().create();
		iRes = iSens.reading().create();
//		iRes.activate(true);
		//		iRes.setValue(0);
		iRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricVoltageSensor vSens = (ElectricVoltageSensor) conn.voltageSensor().create();
		vRes = (VoltageResource) vSens.reading().create();
//		vRes.activate(true);
		//		vRes.setValue(0);
		vRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricPowerSensor pSens = (ElectricPowerSensor) conn.powerSensor().create();
		pRes = (PowerResource) pSens.reading().create();
//		pRes.activate(true);
		//		pRes.setValue(0);
		pRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricFrequencySensor fSens = (ElectricFrequencySensor) conn.frequencySensor().create();
		fRes = (FrequencyResource) fSens.reading().create();
//		fRes.activate(true);
		//		fRes.setValue(0);
		fRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// Add accumulated energy attribute
		EnergyAccumulatedSensor energy = powerMeter.electricityConnection().energySensor().create();
		eRes = (EnergyResource) energy.reading().create();
//		eRes.activate(true);
		//		eRes.setValue(0);
		eRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		powerMeter.activate(true);

		// Add listener to register on/off commands
		onOff.addValueListener(this, true);

		// Get state
		ChannelLocator locator = this.commandChannel.get("COMMAND:01");
		writeToChannel(locator, "010E" + "A0" + "01"); // Syntax: Commando +
		// Flag + Type
		configureLogging();
	}

	private void configureLogging() {
		// configure temperature for logging once per minute
		final RecordedDataConfiguration powerConf = new RecordedDataConfiguration();
		powerConf.setStorageType(FIXED_INTERVAL);
		powerConf.setFixedInterval(60 * 1000l);
		pRes.getHistoricalData().setConfiguration(powerConf);

		// configure state-feedback for logging
		final RecordedDataConfiguration currentConfig = new RecordedDataConfiguration();
		currentConfig.setStorageType(FIXED_INTERVAL);
		currentConfig.setFixedInterval(60 * 1000l);
		iRes.getHistoricalData().setConfiguration(currentConfig);
	}

	@Override
	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_POWER_RES_NAME + config.deviceAddress.replace(':', '_');
	}

	@Override
	public void resourceChanged(BooleanResource resource) {

		// Here the on/off command channel should be written
		// Currently only 1 channel for everything
		ChannelLocator locator = this.commandChannel.get("COMMAND:01");
		// Toggle
		writeToChannel(locator, this.hm_hlConfig.deviceAddress + "4001" + Converter.toHexString(random++) + "A0" + "3E"); // Syntax:
		// Commando
		// +
		// randombyte
		// +
		// Flag
		// +
		// Type
		// Get state
		writeToChannel(locator, "010E" + "A0" + "01"); // Syntax: Commando +
		// Flag + Type

	}
}
