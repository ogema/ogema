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

import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.Converter;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.model.sensors.StateOfChargeSensor;

public class Remote extends HM_hlDevice {

	private FloatResource batteryStatus;
	private ResourceList<BooleanResource> longPress;
	private ResourceList<BooleanResource> shortPress;
	private BooleanResource[] shorts, longs;
	private boolean switchesInited;
	private int numOfSwitches;

	public Remote(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		super(driver, appManager, config);
	}

	public Remote(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		super(driver, appManager, deviceLocator);
		addMandatoryChannels();
		initSwitches();
	}

	@Override
	protected void parseValue(Value value, String channelAddress) {
		if (!switchesInited)
			initSwitches();
		String channelType = channelAddress.split(":")[0];
		byte[] arr = DatatypeConverter.parseHexBinary(channelAddress.split(":")[1]);

		if (channelType.equals("ATTRIBUTE")) {
			if (arr[0] == 0) { // Shorts
				BooleanResource sw = shorts[arr[1] - 1];
				sw.setValue(value.getBooleanValue());
				sw.activate(true);
			}
			else if (arr[0] == 1) { // Longs
				BooleanResource sw = longs[arr[1] - 1];
				sw.setValue(value.getBooleanValue());
				sw.activate(true);
			}
		}
	}

	private void initSwitches() {
		int i = 0;
		shorts = new BooleanResource[numOfSwitches];
		longs = new BooleanResource[numOfSwitches];

		List<BooleanResource> list = longPress.getAllElements();
		while (true) {
			longs[i] = list.get(i);
			if (++i >= numOfSwitches)
				break;
		}
		i = 0;
		list = shortPress.getAllElements();
		while (true) {
			shorts[i] = list.get(i);
			if (++i >= numOfSwitches)
				break;
		}
		switchesInited = true;
	}

	@SuppressWarnings("unchecked")
	private void addMandatoryChannels() {
		HM_hlConfig attributeConfig = new HM_hlConfig();
		attributeConfig.driverId = hm_hlConfig.driverId;
		attributeConfig.interfaceId = hm_hlConfig.interfaceId;
		attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
		attributeConfig.channelAddress = "ATTRIBUTE:0300";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_Battery";
		attributeConfig.chLocator = addChannel(attributeConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
        @SuppressWarnings("deprecation")
		org.ogema.driver.hmhl.models.RemoteControl rem =
                resourceManager.createResource(hm_hlConfig.resourceName, org.ogema.driver.hmhl.models.RemoteControl.class);

		StateOfChargeSensor eSens = rem.battery().chargeSensor().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		// batteryStatus.activate(true);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		batteryStatus.setValue(95);

		longPress = (ResourceList<BooleanResource>) rem.longPress().create();
		shortPress = (ResourceList<BooleanResource>) rem.shortPress().create();
		// Remote control value resources can be set active immediately, since the default value of
		// the status resource (longPress = false, shortPress = false)
		// is actually the correct value as long as no message is received
		rem.activate(true);
		int numOfLongElements = longPress.size();
		int numOfShortElements = shortPress.size();

		// Get number of button channels
		String[] channels = deviceDescriptor.getChannels(type);
		for (String channel : channels) {
			String[] splitChannel = channel.split(":");
			numOfSwitches = Integer.parseInt(splitChannel[2]) - Integer.parseInt(splitChannel[1]) + 1;
			if (splitChannel[0].equals("Sw") || splitChannel[0].equals("Btn")) {
				for (int i = numOfLongElements; i < numOfSwitches; i++) {
//					BooleanResource ll = longPress.add();
					String resName;
					int resourceId;
					if (i % 2 == 0) {
						resName = Constants.BUTTON_OFF_LONG;
						resourceId = i / 2;
					} else {
						resName = Constants.BUTTON_ON_LONG;
						resourceId = (i-1) / 2;
					}
					BooleanResource ll = longPress.addDecorator(resName + resourceId, BooleanResource.class);
					// ll.activate(true);
					ll.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

				}
				for (int i = 0; i < numOfSwitches; i++) {
					attributeConfig = new HM_hlConfig();
					attributeConfig.driverId = hm_hlConfig.driverId;
					attributeConfig.interfaceId = hm_hlConfig.interfaceId;
					attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
					attributeConfig.channelAddress = "ATTRIBUTE:01" + Converter.toHexString((byte) (i + 1));
					attributeConfig.timeout = -1;
					attributeConfig.resourceName = hm_hlConfig.resourceName + "_longPressedButton_" + (i);
					attributeConfig.chLocator = addChannel(attributeConfig);
				}

				for (int i = numOfShortElements; i < numOfSwitches; i++) {
					String resName;
					int resourceId;
					if (i % 2 == 0) {
						resName = Constants.BUTTON_OFF_SHORT;
						resourceId = i / 2;
					} else {
						resName = Constants.BUTTON_ON_SHORT;
						resourceId = (i-1) / 2;
					}
					BooleanResource sh = shortPress.addDecorator(resName + resourceId, BooleanResource.class);
//					BooleanResource sh = shortPress.add();
					// sh.activate(true);
					sh.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

				}
				for (int i = 0; i < numOfSwitches; i++) {
					attributeConfig = new HM_hlConfig();
					attributeConfig.driverId = hm_hlConfig.driverId;
					attributeConfig.interfaceId = hm_hlConfig.interfaceId;
					attributeConfig.deviceAddress = hm_hlConfig.deviceAddress;
					attributeConfig.channelAddress = "ATTRIBUTE:00" + Converter.toHexString((byte) (i + 1));
					attributeConfig.timeout = -1;
					attributeConfig.resourceName = hm_hlConfig.resourceName + "_shortPressedButton_" + (i);
					attributeConfig.chLocator = addChannel(attributeConfig);
				}
			}
		}
		// longPress.activate(true);
		// shortPress.activate(true);
	}

	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_REMOTE_RES_NAME + config.deviceAddress.replace(':', '_');
	}

	@Override
	protected void terminate() {
		removeChannels();
	}
}
