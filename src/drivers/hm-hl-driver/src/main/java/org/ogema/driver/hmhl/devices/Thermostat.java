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
package org.ogema.driver.hmhl.devices;

import org.apache.commons.lang3.ArrayUtils;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.Converter;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.driver.hmhl.models.ThermostatDataModel;
import org.ogema.model.sensors.StateOfChargeSensor;

public class Thermostat extends HM_hlDevice implements ResourceListener {

	private FloatResource batteryStatus;
	private TemperatureResource currentTemp;
	private TemperatureResource desiredTemp;
	private float remoteDesiredTemp;

	public Thermostat(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public Thermostat(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		byte[] array = null;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		byte msgtype = array[array.length - 1];
		// byte msgflag = array[array.length - 2];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		if (type.equals("0095")) {
			if ((msgtype == 0x10 && msg[0] == 0x0A) || (msgtype == 0x02 && msg[0] == 0x01)) {
				float bat = 0;
				float batt = 95;
				float remoteCurrentTemp = 0;
				long desTemp = 0;
				long valvePos = 0;
				long err = 0;
				String err_str = "";
				long ctrlMode = 0;
				String ctrlMode_str = "";

				if (msgtype == 0x10) {
					bat = ((float) (Converter.toLong(msg[3] & 0x1F))) / 10 + 1.5F;
					remoteCurrentTemp = ((float) (Converter.toLong(msg, 1, 2) & 0x3FF)) / 10;
					desTemp = (Converter.toLong(msg, 1, 2) >> 10);
					valvePos = Converter.toLong(msg[4] & 0x7F);
					err = Converter.toLong(msg[3] >> 5);
				}
				else {
					desTemp = Converter.toLong(msg, 1, 2);
					err = Converter.toLong(msg[3] >> 1);
					batt = ((err & 0x80) > 0) ? 5 : 95;
				}
				remoteDesiredTemp = (desTemp & 0x3f) / 2;
				err = err & 0x7;
				ctrlMode = Converter.toLong((msg[5] >> 6) & 0x3);

				if (msg.length >= 7) { // Messages with Party Mode
					// TODO: Implement Party features
				}

				if ((msg.length >= 6) && (ctrlMode == 3)) { // Msg with Boost
					// TODO: Calculation with Boost Time
				}
				switch (Converter.toInt(err)) {
				case 0:
					err_str = "OK";
					break;
				case 1:
					err_str = "Valve tight";
					break;
				case 2:
					err_str = "adjust Range too large";
					break;
				case 3:
					err_str = "adjust Range too small";
					break;
				case 4:
					err_str = "communication error";
					break;
				case 5:
					err_str = "unknown";
					break;
				case 6:
					err_str = "low Battery";
					batt = 5;
					break;
				case 7:
					err_str = "Valve Error Position";
					break;
				}

				switch (Converter.toInt(ctrlMode)) {
				case 0:
					ctrlMode_str = "auto";
					break;
				case 1:
					ctrlMode_str = "manual";
					break;
				case 2:
					ctrlMode_str = "party(urlaub)";
					break;
				case 3:
					ctrlMode_str = "boost";
					break;
				default:
					ctrlMode_str = Long.toHexString(ctrlMode);
					break;
				}

				System.out.println("Measured Temperature: " + remoteCurrentTemp + " C");
				currentTemp.setCelsius(remoteCurrentTemp);
				System.out.println("Desired Temperature: " + remoteDesiredTemp + " C");
				System.out.println("Battery Voltage: " + bat + " V");
				batteryStatus.setValue(batt);
				System.out.println("Valve Position: " + valvePos + " %");
				System.out.println("Error: " + err_str);
				System.out.println("Control Mode: " + ctrlMode_str);

			}
			else if (msgtype == 0x59) { // inform about new value
				// TODO: team msg
			}
			else if (msgtype == 0x3F) { // Timestamp request important!
				// TODO: push @ack,$shash,"${mNo}803F$ioId${src}0204$s2000";
			}

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
		ThermostatDataModel valve = resourceManager.createResource(hm_hlConfig.resourceName, ThermostatDataModel.class);

		StateOfChargeSensor eSens = (StateOfChargeSensor) valve.battery().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		batteryStatus.activate(true);
		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		currentTemp = (TemperatureResource) valve.temperature().reading().create();
		currentTemp.activate(true);
		currentTemp.setKelvin(0);
		currentTemp.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		desiredTemp = (TemperatureResource) valve.temperature().settings().setpoint().create();
		desiredTemp.activate(true);
		desiredTemp.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		// Add listener to register on/off commands
		desiredTemp.addResourceListener(this, false);
	}

	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_VALVE_RES_NAME + config.deviceAddress.replace(':', '_');
	}

	@Override
	public void resourceChanged(Resource res) {

		float localDesiredTemp = ((TemperatureResource) res).getCelsius();
		localDesiredTemp = (float) (Math.ceil(localDesiredTemp * 2) / 2);

		ChannelLocator locator = this.commandChannel.get("COMMAND:01");

		if (localDesiredTemp > 31.5)
			localDesiredTemp = 31.5f;
		if (localDesiredTemp < 0)
			localDesiredTemp = 0;
		float f = (localDesiredTemp * 2.0f);
		int i = (int) f;
		byte b = (byte) (i & 0x000000FF);
		String bs = Converter.toHexString(b);
		// Syntax: Commando + Desiredtemp * 2 + Flag + Type
		writeToChannel(locator, "8104" + bs + "B0" + "11");
	}
}
