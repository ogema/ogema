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
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.units.AngleResource;
import org.ogema.core.model.units.ElectricCurrentResource;
import org.ogema.core.model.units.EnergyResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.VoltageResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.driver.zwavehl.ZWaveHlConfig;
import org.ogema.driver.zwavehl.ZWaveHlDevice;
import org.ogema.driver.zwavehl.ZWaveHlDriver;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.devices.sensoractordevices.MultiSwitchBox;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.sensors.ElectricCurrentSensor;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.ElectricVoltageSensor;
import org.ogema.model.sensors.EnergyAccumulatedSensor;
import org.ogema.model.sensors.ReactivePowerAngleSensor;

/**
 * 
 * @author baerthbn
 * 
 */
public class RelaySwitch extends ZWaveHlDevice implements ResourceValueListener<BooleanResource> {

	private ResourceList<SingleSwitchBox> singleSwitches;
	private BooleanResource[] onOff = new BooleanResource[3];
	private BooleanResource[] isOn = new BooleanResource[3];
	private ElectricCurrentResource[] iRes = new ElectricCurrentResource[3];
	private VoltageResource[] vRes = new VoltageResource[3];
	private PowerResource[] pRes = new PowerResource[3];
	private EnergyResource[] eRes = new EnergyResource[3];
	private AngleResource[] aRes = new AngleResource[3];

	public RelaySwitch(ZWaveHlDriver driver, ApplicationManager appManager, ZWaveHlConfig config) {
		super(driver, appManager, config);
	}

	public RelaySwitch(ZWaveHlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "0025:0001:0000":
			isOn[1].setValue(value.getBooleanValue());
			isOn[2].setValue(value.getBooleanValue());
			break;
		case "0025:0002:0000":
			isOn[1].setValue(value.getBooleanValue());
			break;
		case "0025:0003:0000":
			isOn[2].setValue(value.getBooleanValue());
			break;
		case "0032:0001:0000":
			eRes[0].setValue(value.getFloatValue());
			break;
		case "0032:0002:0000":
			eRes[1].setValue(value.getFloatValue());
			break;
		case "0032:0003:0000":
			eRes[2].setValue(value.getFloatValue());
			break;
		case "0032:0001:0008":
			pRes[0].setValue(value.getFloatValue());
			break;
		case "0032:0002:0008":
			pRes[1].setValue(value.getFloatValue());
			break;
		case "0032:0003:0008":
			pRes[2].setValue(value.getFloatValue());
			break;
		case "0032:0001:0010":
			vRes[0].setValue(value.getFloatValue());
			break;
		case "0032:0002:0010":
			vRes[1].setValue(value.getFloatValue());
			break;
		case "0032:0003:0010":
			vRes[2].setValue(value.getFloatValue());
			break;
		case "0032:0001:0014":
			iRes[0].setValue(value.getFloatValue());
			break;
		case "0032:0002:0014":
			iRes[1].setValue(value.getFloatValue());
			break;
		case "0032:0003:0014":
			iRes[2].setValue(value.getFloatValue());
			break;
		case "0032:0001:0018":
			aRes[0].setValue((float) Math.acos(value.getFloatValue()));
			break;
		case "0032:0002:0018":
			aRes[1].setValue((float) Math.acos(value.getFloatValue()));
			break;
		case "0032:0003:0018":
			aRes[2].setValue((float) Math.acos(value.getFloatValue()));
			break;
		}
	}

	protected void init() {
		ZWaveHlConfig attributeConfig;

		/*
		 * Initialize the resource tree Note that the device is a combination of two relays where SingleSwitchBox at
		 * index 0 in the list below controls both relays at once. isOn attribute is skipped for this list entry because
		 * it doesn't make sense.
		 */
		// Create top level resource
		MultiSwitchBox relaySwitch = resourceManager.createResource(zwaveHlConfig.resourceName, MultiSwitchBox.class);
		relaySwitch.activate(true);
		singleSwitches = relaySwitch.switchboxes().create();
		SingleSwitchBox ssb;
		for (int i = 0; i < 3; i++) {
			if (singleSwitches.size() >= i + 1)
				ssb = singleSwitches.getAllElements().get(i);
			else
				ssb = singleSwitches.add();

			onOff[i] = (BooleanResource) ssb.onOffSwitch().stateControl().create();
			if (i != 0)
				isOn[i] = (BooleanResource) ssb.onOffSwitch().stateFeedback().create();

			ssb.activate(true);
			onOff[i].requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);
			if (i != 0)
				isOn[i].requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
			onOff[i].addValueListener(this, true);

			attributeConfig = new ZWaveHlConfig();
			attributeConfig.driverId = zwaveHlConfig.driverId;
			attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
			attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
			attributeConfig.channelAddress = "0025:000" + (i + 1) + ":0000";
			attributeConfig.resourceName = zwaveHlConfig.resourceName + "_OnOffToggle_CH" + (i + 1);
			attributeConfig.chLocator = addChannel(attributeConfig);
			// if (i != 0)
			// readValue("0025:000" + (i + 1) + ":0000");

			// The connection attribute and its children, current, voltage, power, frequency
			ElectricityConnection conn = ssb.electricityConnection().create();
			conn.activate(true);

			ElectricCurrentSensor iSens = conn.currentSensor().create();
			iRes[i] = iSens.reading().create();
			// iRes[i].activate(true);
			iRes[i].requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

			attributeConfig = new ZWaveHlConfig();
			attributeConfig.driverId = zwaveHlConfig.driverId;
			attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
			attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
			attributeConfig.channelAddress = "0032:000" + (i + 1) + ":0014";
			attributeConfig.resourceName = zwaveHlConfig.resourceName + "_Current_CH" + (i + 1);
			attributeConfig.chLocator = addChannel(attributeConfig);
			// readValue("0032:000" + (i + 1) + ":0014");

			ElectricVoltageSensor vSens = (ElectricVoltageSensor) conn.voltageSensor().create();
			vRes[i] = (VoltageResource) vSens.reading().create();
			// vRes[i].activate(true);
			vRes[i].requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

			attributeConfig = new ZWaveHlConfig();
			attributeConfig.driverId = zwaveHlConfig.driverId;
			attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
			attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
			attributeConfig.channelAddress = "0032:000" + (i + 1) + ":0010";
			attributeConfig.resourceName = zwaveHlConfig.resourceName + "_Voltage_CH" + (i + 1);
			attributeConfig.chLocator = addChannel(attributeConfig);
			// readValue("0032:000" + (i + 1) + ":0014");

			ElectricPowerSensor pSens = (ElectricPowerSensor) conn.powerSensor().create();
			pRes[i] = (PowerResource) pSens.reading().create();
			// pRes[i].activate(true);
			pRes[i].requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

			attributeConfig = new ZWaveHlConfig();
			attributeConfig.driverId = zwaveHlConfig.driverId;
			attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
			attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
			attributeConfig.channelAddress = "0032:000" + (i + 1) + ":0008";
			attributeConfig.resourceName = zwaveHlConfig.resourceName + "_Power_CH" + (i + 1);
			attributeConfig.chLocator = addChannel(attributeConfig);
			// readValue("0032:000" + (i + 1) + ":0008");

			ReactivePowerAngleSensor aSens = (ReactivePowerAngleSensor) conn.reactiveAngleSensor().create();
			aRes[i] = (AngleResource) aSens.reading().create();
			// aRes[i].activate(true);
			aRes[i].requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

			attributeConfig = new ZWaveHlConfig();
			attributeConfig.driverId = zwaveHlConfig.driverId;
			attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
			attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
			attributeConfig.channelAddress = "0032:000" + (i + 1) + ":0018";
			attributeConfig.resourceName = zwaveHlConfig.resourceName + "_Power_CH" + (i + 1);
			attributeConfig.chLocator = addChannel(attributeConfig);
			// readValue("0032:000" + (i + 1) + ":0018");

			// Add accumulated energy attribute
			EnergyAccumulatedSensor energy = ssb.electricityConnection().energySensor().create();
			eRes[i] = (EnergyResource) energy.reading().create();
			// eRes[i].activate(true);
			eRes[i].requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

			attributeConfig = new ZWaveHlConfig();
			attributeConfig.driverId = zwaveHlConfig.driverId;
			attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
			attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
			attributeConfig.channelAddress = "0032:000" + (i + 1) + ":0000";
			attributeConfig.resourceName = zwaveHlConfig.resourceName + "_Energy_CH" + (i + 1);
			attributeConfig.chLocator = addChannel(attributeConfig);
			// readValue("0032:000" + (i + 1) + ":0000");
		}

	}

	@Override
	protected void unifyResourceName(ZWaveHlConfig config) {
		config.resourceName = "ZWave_" + config.resourceName.replace('-', '_').replace('.', '_') + config.deviceAddress;
	}

	@Override
	public void resourceChanged(BooleanResource resource) {
		ChannelLocator locator;
		BooleanValue newState = new BooleanValue(resource.getValue());
		if (resource.equals(onOff[0])) {
			locator = this.valueChannel.get("0025:0001:0000");
			writeToChannel(locator, newState);
		}
		else if (resource.equals(onOff[1])) {
			locator = this.valueChannel.get("0025:0002:0000");
			writeToChannel(locator, newState);
		}
		else if (resource.equals(onOff[2])) {
			locator = this.valueChannel.get("0025:0003:0000");
			writeToChannel(locator, newState);
		}
	}

	@Override
	protected void terminate() {
		// release the exclusive access by requesting READ_ONLY access.
		for (int i = 0; i < 3; i++) {
			if (singleSwitches.size() >= i + 1) {
				onOff[i].removeValueListener(this);
				onOff[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
				if (i != 0)
					isOn[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
				iRes[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);

				vRes[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);

				pRes[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);

				aRes[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);

				eRes[i].requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
			}
			else
				break;
		}
		removeChannels();
	}

	@Override
	public void channelFound(ChannelLocator channel) {

	}

	@Override
	public void finished(boolean success) {

	}

	@Override
	public void progress(float ratio) {

	}
}
