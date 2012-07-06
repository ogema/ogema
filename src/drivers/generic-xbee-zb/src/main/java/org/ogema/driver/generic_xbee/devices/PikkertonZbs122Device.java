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
package org.ogema.driver.generic_xbee.devices;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.generic_xbee.Constants;
import org.ogema.driver.generic_xbee.GenericXbeeZbConfig;
import org.ogema.driver.generic_xbee.GenericXbeeZbDevice;
import org.ogema.driver.generic_xbee.GenericXbeeZbDriver;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.slf4j.Logger;

public class PikkertonZbs122Device extends GenericXbeeZbDevice {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("generic_xbee_hl");

	private enum ParsingState {
		VALUES_RECEIVED, IDENTIFIER_PARSED, VALUE_PARSED
	}

	private static final String ZBS122_NAME = "ZBS122_";

	private final byte[] TEM = { 0x54, 0x45, 0x4D };
	private final byte[] HUM = { 0x48, 0x55, 0x4D };
	private final byte[] BAT = { 0x42, 0x41, 0x54 };
	private final byte[] UBAT = { 0x55, 0x42, 0x41, 0x54 };
	private byte[] identifier;
	private byte[] value;

	private ParsingState parsingState = ParsingState.VALUES_RECEIVED;

	private ByteBuffer rawValues;
	private ByteBuffer readValuesBuffer = ByteBuffer.allocate(128); // 128 is ZigBees MTU
	private GenericXbeeZbConfig attributeConfig;
	TemperatureResource tRes;
	FloatResource rHumidity;

	/**
	 * Use this constructor only if your configuration contains a valid channel.
	 * 
	 * @param driver
	 * @param appManager
	 * @param config
	 */
	public PikkertonZbs122Device(GenericXbeeZbDriver driver, ApplicationManager appManager, GenericXbeeZbConfig config) {
		super(driver, appManager, config);
		attributeConfig = new GenericXbeeZbConfig();
		// addMandatoryChannels();
		addChannel(config);
	}

	public PikkertonZbs122Device(GenericXbeeZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		attributeConfig = new GenericXbeeZbConfig();
		addMandatoryChannels();
	}

	private void addMandatoryChannels() {
		attributeConfig.driverId = configurationResource.driverId;
		attributeConfig.interfaceId = configurationResource.interfaceId;
		attributeConfig.deviceAddress = configurationResource.deviceAddress;
		attributeConfig.channelAddress = Constants.ZBS_122_ATTR_ADDRESS;
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = configurationResource.resourceName + "_THSensor";
		addChannel(attributeConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		SensorDevice zbs122Device = resourceManager.createResource(attributeConfig.resourceName, SensorDevice.class);

		zbs122Device.sensors().create();
		zbs122Device.sensors().activate(false);

		// The Humidity resource
		HumiditySensor hSens = zbs122Device.sensors().addDecorator("humidity", HumiditySensor.class);
		rHumidity = hSens.reading().create();
		rHumidity.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		rHumidity.activate(false);
		hSens.activate(false);

		// The temperature resource
		TemperatureSensor tSens = zbs122Device.sensors().addDecorator("temperature", TemperatureSensor.class);
		tRes = tSens.reading().create();
		tRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		tRes.activate(false);
		tSens.activate(false);
	}

	@Override
	public void addChannel(GenericXbeeZbConfig config) {
		ChannelLocator channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfig = channelAccess.getChannelConfiguration(channelLocator);
		channelConfig.setSamplingPeriod(config.timeout);

		if (driver.channelMap.containsKey(config.resourceName)) {
			logger.error("resourceName already taken.");
			return;
		}
		driver.channelMap.put(config.resourceName, channelLocator);

		try {
			channelAccess.addChannel(channelConfig);
		} catch (ChannelConfigurationException e) {
			e.printStackTrace();
		}
		addToUpdateListener(channelLocator);
	}

	@Override
	public void updateValues(Value value) {
		rawValues = ByteBuffer.wrap(value.getByteArrayValue());
		parseValues();
	}

	// @Override
	// public void updateValues() {
	// SampledValue value = null;
	// try {
	// value = channelAccess.getChannelValue(channelLocator);
	// } catch (ChannelAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// updateValues(value.getValue());
	// }
	//
	private void parseValues() {
		PikkertonZbs122Values parsedValues = new PikkertonZbs122Values();

		byte b;
		int parsedCounter = 0;

		while (rawValues.position() < rawValues.limit()) {
			b = rawValues.get();
			++parsedCounter;
			switch (parsingState) {
			case VALUES_RECEIVED:
				if ('=' == b) {
					--parsedCounter;
					readValuesBuffer.position(readValuesBuffer.position() - parsedCounter);
					identifier = new byte[parsedCounter]; // The array with the identifier will be used later to
					// determine the type of value
					readValuesBuffer.get(identifier, 0, parsedCounter);
					parsingState = ParsingState.IDENTIFIER_PARSED;
					parsedCounter = 0;
					break;
				}
				readValuesBuffer.put(b);
				break;
			case IDENTIFIER_PARSED:
				if (0x0A == b) {
					--parsedCounter;
					readValuesBuffer.position(readValuesBuffer.position() - parsedCounter);
					value = new byte[parsedCounter];
					readValuesBuffer.get(value, 0, parsedCounter);
					parsingState = ParsingState.VALUE_PARSED;
				}
				else if ((b < '0' || b > '9') && b != '.' && !Arrays.equals(identifier, BAT)) { // Get rid of unit chars
					--parsedCounter;
					break;
				}
				else {
					readValuesBuffer.put(b);
					break;
				}
			case VALUE_PARSED:
				if (Arrays.equals(identifier, TEM)) {
					parsedValues.tem = Float.parseFloat(new String(value));
				}
				else if (Arrays.equals(identifier, HUM)) { // TODO find a more efficient way that does not create a new
					// String
					parsedValues.hum = Short.parseShort(new String(value));
				}
				else if (Arrays.equals(identifier, BAT)) { // TODO char array anstatt boolean?
					if ('L' == value[0]) { // LOW
						parsedValues.bat = false;
					}
					else if (value[0] == 'O') { // OK
						parsedValues.bat = true;
					}
				}
				else if (Arrays.equals(identifier, UBAT)) {
					parsedValues.ubat = Float.parseFloat(new String(value));
				}
				parsingState = ParsingState.VALUES_RECEIVED;
				parsedCounter = 0;
				break;
			}
		}
		// Reset values, arrays and buffer
		identifier = null;
		value = null;
		readValuesBuffer.clear();

		logger.info("############ Pikkerton ZBS-122 values parsed ############");
		logger.info("Temp: " + parsedValues.tem);
		logger.info("Hum: " + parsedValues.hum);
		logger.info("Bat: " + parsedValues.bat);
		logger.info("Ubat: " + parsedValues.ubat);

		// Transfer the received values into the resource pool
		tRes.setCelsius(parsedValues.tem);
		rHumidity.setValue(parsedValues.hum);
	}

	public void unifyResourceName(GenericXbeeZbConfig xbeeConfig) {
		xbeeConfig.resourceName = ZBS122_NAME + xbeeConfig.deviceAddress.replace(':', '_');
	}

	@Override
	public JSONObject packValuesAsJSON() {
		PikkertonZbs122Values parsedValues = new PikkertonZbs122Values();
		JSONObject parseValuesJSONObject = new JSONObject();

		try {
			parseValuesJSONObject.put("tem", parsedValues.tem);
			parseValuesJSONObject.put("hum", parsedValues.hum);
			parseValuesJSONObject.put("bat", parsedValues.bat);
			parseValuesJSONObject.put("ubat", parsedValues.ubat);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return parseValuesJSONObject;
	}

	class PikkertonZbs122Values {
		public float tem;
		public short hum;
		public boolean bat; // "OK" = true if battery is ok, "LOW" = false if battery is low
		public float ubat;
	}

}
