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
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.driver.zwavehl.ZWaveHlConfig;
import org.ogema.driver.zwavehl.ZWaveHlDevice;
import org.ogema.driver.zwavehl.ZWaveHlDriver;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;

/**
 * 
 * @author baerthbn
 * 
 */
public class SwitchBox extends ZWaveHlDevice implements ResourceValueListener<BooleanResource> {

	private BooleanResource onOff;
	private BooleanResource isOn;
	private SingleSwitchBox switchBox;

	public SwitchBox(ZWaveHlDriver driver, ApplicationManager appManager, ZWaveHlConfig config) {
		super(driver, appManager, config);
	}

	public SwitchBox(ZWaveHlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		init();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		switch (channelAddress) {
		case "0025:0001:0000":
			isOn.setValue(value.getBooleanValue());
			break;
		}
	}

	protected void init() {
		switchBox = resourceManager.createResource(zwaveHlConfig.resourceName, SingleSwitchBox.class);
	}

	private void initOnOff() {
		ZWaveHlConfig attributeConfig = new ZWaveHlConfig();
		attributeConfig.driverId = zwaveHlConfig.driverId;
		attributeConfig.interfaceId = zwaveHlConfig.interfaceId;
		attributeConfig.deviceAddress = zwaveHlConfig.deviceAddress;
		attributeConfig.channelAddress = "0025:0001:0000";
		attributeConfig.resourceName = zwaveHlConfig.resourceName + "_OnOffToggle";
		attributeConfig.chLocator = addChannel(attributeConfig);

		// The on/off switch
		onOff = (BooleanResource) switchBox.onOffSwitch().stateControl().create();
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);
		isOn = (BooleanResource) switchBox.onOffSwitch().stateFeedback().create();
		isOn.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		switchBox.activate(true);
		onOff.addValueListener(this, true);
	}

	@Override
	protected void unifyResourceName(ZWaveHlConfig config) {
		config.resourceName = "ZWave_" + config.resourceName.replace('-', '_') + config.deviceAddress;
	}

	@Override
	public void resourceChanged(BooleanResource resource) {
		ChannelLocator locator = this.valueChannel.get("0025:0001:0000");
		BooleanValue newState = new BooleanValue(resource.getValue());
		writeToChannel(locator, newState);
	}

	@Override
	protected void terminate() {
		// Remove listener to register on/off commands
		onOff.removeValueListener(this);
		removeChannels();
		onOff.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
		isOn.requestAccessMode(AccessMode.READ_ONLY, AccessPriority.PRIO_LOWEST);
	}

	@Override
	public void channelFound(ChannelLocator channel) {
		String ch = channel.getChannelAddress();
		switch (ch) {
		case "0025:0001:0000": // OnOff switch
			initOnOff();
			break;
		default:
			logger.warn("Unexpected channel detected!");
			break;
		}

	}

	@Override
	public void finished(boolean success) {

	}

	@Override
	public void progress(float ratio) {

	}
}
