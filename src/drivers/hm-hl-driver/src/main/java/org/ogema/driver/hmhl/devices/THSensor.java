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

import org.apache.commons.lang3.ArrayUtils;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.Converter;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.storage.ElectricityStorage;
import org.ogema.model.sensors.StateOfChargeSensor;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.TemperatureSensor;

public class THSensor extends HM_hlDevice {

	private FloatResource rHumidity;
	private TemperatureResource tRes;
	private FloatResource batteryStatus;

	public THSensor(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public THSensor(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		byte[] array = null;
		long temp = 0;
		long hum = 0;
		long err = 0;
		String err_str = "";
		float batt = 95;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		else {
			throw new IllegalArgumentException("unsupported value type: " + value);
		}
		byte msgtype = array[array.length - 1];
		// byte msgflag = array[array.length - 2];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		if (msgtype == 0x70) {
			temp = Converter.toLong(msg, 0, 2);
			err_str = ((temp & 0x8000) > 0) ? "low" : "ok";
			batt = ((temp & 0x8000) > 0) ? 5 : 95;
			if ((temp & 0x4000) > 0)
				temp -= 0x8000;

			if (msg.length > 2)
				hum = Converter.toLong(msg, 2, 1);
		}
		else if (msgtype == 0x53) {
			temp = Converter.toLong(msg, 2, 2);
			if ((temp & 0xC00) > 0)
				temp -= 0x10000;
			err = Converter.toLong(msg[0]);
			err_str = ((err & 0x80) > 0) ? "low" : "ok";
			batt = ((temp & 0x8000) > 0) ? 5 : 95;
		}

		//		System.out.println("Temperatur: " + ((float) temp) / 10 + " C");
		//		System.out.println("State of Battery: " + err_str);
		tRes.setCelsius(temp / 10f);
		if (hum < 100) {
			//			System.out.println("Humidity: " + hum + "%");
			rHumidity.setValue(hum);
		}
		batteryStatus.setValue(batt);
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

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		SensorDevice thDevice = resourceManager.createResource(hm_hlConfig.resourceName, SensorDevice.class);

		thDevice.sensors().create();
		thDevice.sensors().activate(true);

		HumiditySensor hSensor = thDevice.sensors().addDecorator("humidity", HumiditySensor.class);
		rHumidity = hSensor.reading().create();
		rHumidity.activate(true);
		rHumidity.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		hSensor.activate(true);

		TemperatureSensor tSensor = thDevice.sensors().addDecorator("temperature", TemperatureSensor.class);
		tRes = tSensor.reading().create();
		tRes.activate(true);
		//		tRes.setKelvin(0);
		tRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		tSensor.activate(true);
		thDevice.activate(true);

		ElectricityStorage battery = thDevice.electricityStorage().create();
		IntegerResource batteryType = battery.type().create();
		batteryType.setValue(1); // "generic battery"
		batteryType.activate(true);
		StateOfChargeSensor eSens = battery.chargeSensor().create();
		batteryStatus = eSens.reading().create();
		batteryStatus.activate(true);
		//		batteryStatus.setValue(95);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		eSens.activate(true);
		battery.activate(true);
	}

	@Override
	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_TH_RES_NAME + config.deviceAddress.replace(':', '_');
	}
}
