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

import org.apache.commons.lang3.ArrayUtils;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.driver.hmhl.Constants;
import org.ogema.driver.hmhl.HM_hlConfig;
import org.ogema.driver.hmhl.HM_hlDevice;
import org.ogema.driver.hmhl.HM_hlDriver;
import org.ogema.driver.hmhl.models.RemoteControl;
import org.ogema.model.sensors.StateOfChargeSensor;

public class Remote extends HM_hlDevice {

	private FloatResource batteryStatus;
	private ResourceList<BooleanResource> longPress;
	private ResourceList<BooleanResource> shortPress;
	private BooleanResource[] shorts, longs;
	private long btncnt = 0;
	private byte oldflag = 0x00;
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
		byte[] array = null;

		if (value instanceof ByteArrayValue) {
			array = value.getByteArrayValue();
		}
		byte msgtype = array[array.length - 1];
		byte msgflag = array[array.length - 2];
		byte[] msg = ArrayUtils.removeAll(array, array.length - 2, array.length - 1);

		if ((msgtype & 0xF0) == 0x40) {
			final int btn_no = (msg[0] & 0x3F) - 1;
			if (!switchesInited)
				initSwitches();
			if ((msg[0] & 0x40) > 0) {
				if (msgflag != oldflag) { // long press
					oldflag = msgflag;
					BooleanResource sw = longs[btn_no];
					if ((msgflag & 0x20) > 0) {
						sw.setValue(false); // Release
						System.out.println("Long Pressed button: " + sw.getValue());
					}
					else if (msg[1] != btncnt) {
						sw.setValue(true); // Press
						System.out.println("Long Pressed button: " + sw.getValue());
					}
				}
			}
			else if (msg[1] != btncnt) { // short press
				final BooleanResource sw = shorts[btn_no];
				boolean oldValue = sw.getValue();
				sw.setValue(!oldValue); // press
				System.out.println("Short Pressed button value: " + sw.getValue());
				System.out.println("Short Pressed button count: " + btncnt);
			}
			String err_str = ((msg[0] & 0x80) > 0) ? "low" : "ok";
			float batt = ((msg[0] & 0x80) > 0) ? 5 : 95;
			System.out.println("Battery: " + err_str);
			batteryStatus.setValue(batt);
			btncnt = msg[1];
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
		attributeConfig.channelAddress = "ATTRIBUTE:0001";
		attributeConfig.timeout = -1;
		attributeConfig.resourceName = hm_hlConfig.resourceName + "_Attribute";
		attributeConfig.chLocator = addChannel(attributeConfig);

		/*
		 * Initialize the resource tree
		 */
		// Create top level resource
		RemoteControl rem = resourceManager.createResource(hm_hlConfig.resourceName, RemoteControl.class);
		rem.activate(true);

		StateOfChargeSensor eSens = rem.battery().chargeSensor().create();
		batteryStatus = (FloatResource) eSens.reading().create();
		batteryStatus.activate(true);
		batteryStatus.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
		batteryStatus.setValue(95);

		longPress = (ResourceList<BooleanResource>) rem.longPress().create();
		shortPress = (ResourceList<BooleanResource>) rem.shortPress().create();

		int numOfLongElements = longPress.size();
		int numOfShortElements = shortPress.size();

		// Get number of button channels
		String[] channels = deviceDescriptor.getChannels(type);
		for (String channel : channels) {
			String[] splitChannel = channel.split(":");
			numOfSwitches = Integer.parseInt(splitChannel[2]) - Integer.parseInt(splitChannel[1]) + 1;
			if (splitChannel[0].equals("Sw") || splitChannel[0].equals("Btn")) {
				for (int i = numOfLongElements; i < numOfSwitches; i++) {
					BooleanResource ll = longPress.add();
					ll.activate(true);
					ll.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
				}

				for (int i = numOfShortElements; i < numOfSwitches; i++) {
					BooleanResource sh = shortPress.add();
					sh.activate(true);
					sh.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);
				}
			}
		}
		longPress.activate(true);
		shortPress.activate(true);
	}

	protected void unifyResourceName(HM_hlConfig config) {
		config.resourceName += Constants.HM_REMOTE_RES_NAME + config.deviceAddress.replace(':', '_');
	}

}
