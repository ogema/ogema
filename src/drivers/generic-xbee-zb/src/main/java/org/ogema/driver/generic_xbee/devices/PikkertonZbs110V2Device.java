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
package org.ogema.driver.generic_xbee.devices;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.driver.generic_xbee.Constants;
import org.ogema.driver.generic_xbee.GenericXbeeZbConfig;
import org.ogema.driver.generic_xbee.GenericXbeeZbDevice;
import org.ogema.driver.generic_xbee.GenericXbeeZbDriver;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricFrequencySensor;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;
import org.ogema.model.sensors.EnergyAccumulatedSensor;
import org.slf4j.Logger;

public class PikkertonZbs110V2Device extends GenericXbeeZbDevice implements ResourceListener {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("generic_xbee_hl");

	private enum ParsingState {
		VALUES_RECEIVED, IDENTIFIER_PARSED, VALUE_PARSED
	}

	private static final String ZBS110_NAME = "ZBS110V2_";

	private final byte[] POW = { 0x50, 0x4F, 0x57 };
	private final byte[] FREQ = { 0x46, 0x52, 0x45, 0x51 };
	private final byte[] VRMS = { 0x56, 0x52, 0x4D, 0x53 };
	private final byte[] IRMS = { 0x49, 0x52, 0x4D, 0x53 };
	private final byte[] LOAD = { 0x4C, 0x4F, 0x41, 0x44 };
	private final byte[] WORK = { 0x57, 0x4F, 0x52, 0x4B };
	private byte[] identifier;
	private byte[] value;

	private ParsingState parsingState = ParsingState.VALUES_RECEIVED;

	private ByteBuffer rawValues;
	private ByteBuffer readValuesBuffer = ByteBuffer.allocate(128); // 128 is ZigBees MTU
	private GenericXbeeZbConfig attributeConfig;
	private SingleSwitchBox zbs110Device;
	private BooleanResource onOff;
	private ElectricCurrentResource iRes;
	private VoltageResource vRes;
	private PowerResource pRes;
	private FrequencyResource fRes;
	private EnergyResource eRes;
	private BooleanResource isOn;

	private final byte[] powOn = { 0x53, 0x45, 0x54, 0x20, 0x50, 0x4F, 0x57, 0x3D, 0x4F, 0x4E, 0x0A };
	private final byte[] powOff = { 0x53, 0x45, 0x54, 0x20, 0x50, 0x4F, 0x57, 0x3D, 0x4F, 0x46, 0x46, 0x0A };

	/**
	 * Use this constructor only if your configuration contains a valid channel.
	 * 
	 * @param driver
	 * @param appManager
	 * @param configurationResource
	 */
	public PikkertonZbs110V2Device(GenericXbeeZbDriver driver, ApplicationManager appManager,
			GenericXbeeZbConfig configurationResource) {
		super(driver, appManager, configurationResource);
		attributeConfig = new GenericXbeeZbConfig();
		// addMandatoryChannels();
		addChannel(configurationResource);
	}

	public PikkertonZbs110V2Device(GenericXbeeZbDriver driver, ApplicationManager appManager,
			DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		attributeConfig = new GenericXbeeZbConfig();
		addMandatoryChannels();
	}

	private void addMandatoryChannels() {
		attributeConfig.driverId = configurationResource.driverId;
		attributeConfig.interfaceId = configurationResource.interfaceId;
		attributeConfig.deviceAddress = configurationResource.deviceAddress;
		attributeConfig.channelAddress = Constants.ZBS_110V2_ATTR_ADDRESS;
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = configurationResource.resourceName + "_SwitchBox";
		addChannel(attributeConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		zbs110Device = resourceManager.createResource(attributeConfig.resourceName, SingleSwitchBox.class);
		zbs110Device.activate(true);

		// The on/off switch
		onOff = (BooleanResource) zbs110Device.onOffSwitch().stateControl().create();
		onOff.activate(true);
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		// The connection attribute and its children, current, voltage, power, frequency
		ElectricityConnection conn = (ElectricityConnection) zbs110Device.electricityConnection().create();
		conn.activate(true);

		ElectricCurrentSensor iSens = (ElectricCurrentSensor) conn.currentSensor().create();
		iRes = (ElectricCurrentResource) iSens.reading().create();
		iRes.activate(true);
		iRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricVoltageSensor vSens = (ElectricVoltageSensor) conn.voltageSensor().create();
		vRes = (VoltageResource) vSens.reading().create();
		vRes.activate(true);
		vRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricPowerSensor pSens = (ElectricPowerSensor) conn.powerSensor().create();
		pRes = (PowerResource) pSens.reading().create();
		pRes.activate(true);
		pRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		ElectricFrequencySensor fSens = (ElectricFrequencySensor) conn.frequencySensor().create();
		fRes = (FrequencyResource) fSens.reading().create();
		fRes.activate(true);
		fRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// Add accumulated energy attribute
		EnergyAccumulatedSensor energy = (EnergyAccumulatedSensor) conn.energySensor().create();
		eRes = (EnergyResource) energy.reading().create();
		eRes.activate(true);
		eRes.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		isOn = (BooleanResource) zbs110Device.onOffSwitch().stateFeedback().create();
		isOn.activate(true);
		isOn.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// Add listener to register on/off commands
		onOff.addResourceListener(this, false);
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

	private void parseValues() {
		PikkertonZbs110Values parsedValues = new PikkertonZbs110Values();

		byte b;
		int parsedCounter = 0;

		logger.debug("Raw values:");
		for (byte by : rawValues.array()) {
			System.out.print((char) by);
		}

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
				else if ((b < '0' || b > '9') && b != '.' && !Arrays.equals(identifier, POW)) { // Get rid of unit chars
					--parsedCounter;
					break;
				}
				else {
					readValuesBuffer.put(b);
					break;
				}
			case VALUE_PARSED:
				for (byte b1 : value) {
					System.out.print((char) b1);
				}
				System.out.println();
				if (Arrays.equals(identifier, POW)) {
					if ('N' == value[1]) { // ON
						parsedValues.pow = true;
					}
					else if (value[1] == 'F') { // OFF
						parsedValues.pow = false;
					}
				}
				else if (Arrays.equals(identifier, FREQ)) { // TODO find a more efficient way that does not create a new
					// String
					parsedValues.freq = Float.parseFloat(new String(value));
				}
				else if (Arrays.equals(identifier, VRMS)) {
					parsedValues.vrms = Short.parseShort(new String(value));
				}
				else if (Arrays.equals(identifier, IRMS)) {
					parsedValues.irms = Short.parseShort(new String(value));
				}
				else if (Arrays.equals(identifier, LOAD)) {
					parsedValues.load = Integer.parseInt(new String(value));
				}
				else if (Arrays.equals(identifier, WORK)) {
					parsedValues.work = Float.parseFloat(new String(value));
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

		logger.info("############ Pikkerton ZBS-110 values parsed ############");
		logger.info("Power: " + parsedValues.pow);
		logger.info("Frequency: " + parsedValues.freq);
		logger.info("Voltage: " + parsedValues.vrms);
		logger.info("Current: " + parsedValues.irms);
		logger.info("Load: " + parsedValues.load);
		logger.info("Energy: " + parsedValues.work);

		isOn.setValue(parsedValues.pow);
		iRes.setValue(parsedValues.irms);
		vRes.setValue(parsedValues.vrms);
		fRes.setValue(parsedValues.freq);
	}

	@Override
	public void resourceChanged(Resource res) {
		logger.debug("onOff method call");

		if (!(res instanceof BooleanResource))
			return;
		if (!(res.getName().equals("onoff")))
			return;

		boolean onoffCommand = ((BooleanResource) res).getValue();

		// Here the on/off command channel should be written
		// Currently only 1 channel for everything
		ChannelLocator channelLocator = driver.channelMap.get(attributeConfig.resourceName);
		if (onoffCommand) { // Turn on
			try {
				channelAccess.setChannelValue(channelLocator, new ByteArrayValue(powOn));
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
		}
		else { // Turn off
			try {
				channelAccess.setChannelValue(channelLocator, new ByteArrayValue(powOff));
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
		}

	}

	public void unifyResourceName(GenericXbeeZbConfig xbeeConfig) {
		xbeeConfig.resourceName = ZBS110_NAME + xbeeConfig.deviceAddress.replace(':', '_');
	}

	@Override
	public JSONObject packValuesAsJSON() {
		PikkertonZbs110Values parsedValues = new PikkertonZbs110Values();

		JSONObject parseValuesJSONObject = new JSONObject();

		try {
			parseValuesJSONObject.put("pow", parsedValues.pow);
			parseValuesJSONObject.put("freq", parsedValues.freq);
			parseValuesJSONObject.put("vrms", parsedValues.vrms);
			parseValuesJSONObject.put("irms", parsedValues.irms);
			parseValuesJSONObject.put("load", parsedValues.load);
			parseValuesJSONObject.put("work", parsedValues.work);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return parseValuesJSONObject;
	}

	class PikkertonZbs110Values {
		public boolean pow;
		public float freq;
		public short vrms;
		public short irms;
		public int load;
		public float work;
	}
}
