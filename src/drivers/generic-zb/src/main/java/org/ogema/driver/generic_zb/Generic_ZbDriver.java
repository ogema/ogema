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
package org.ogema.driver.generic_zb;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.driver.generic_zb.devices.ColorDimmableLight;
import org.ogema.driver.generic_zb.devices.DevelcoSmartPlug;
import org.ogema.driver.generic_zb.devices.MainsPowerOutlet;
import org.ogema.driver.generic_zb.devices.SmartPlug;
import org.ogema.driver.xbee.Constants;
import org.slf4j.Logger;

public class Generic_ZbDriver implements Application, DeviceScanListener {
	/** Cached ApplicationManager */
	protected ApplicationManager appManager;
	/** Map of active Fls_Pp devices */
	protected final Map<String, Generic_ZbDevice> devices = new HashMap<String, Generic_ZbDevice>(); // String == interface:deviceAddress
	public final Map<String, ChannelConfiguration> channelMap =  new HashMap<String, ChannelConfiguration>(); // Map a name to a channelLocator (resourceId)
	private DeviceScanListener deviceScanListener;

	public static final Logger logger = org.slf4j.LoggerFactory.getLogger("generic-zb");

	public Generic_ZbDriver() {
	}

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager;
	}

	@Override
	public void stop(AppStopReason reason) {

		for (Generic_ZbDevice device : devices.values()) {
			device.close();
		}

		ChannelAccess ca = appManager.getChannelAccess();
		
		for (ChannelConfiguration chConf : channelMap.values()) {
			ca.deleteChannel(chConf);
		}
	}

	public void resourceAvailable(Generic_ZbConfig config) {
		if (!devices.containsKey(config.interfaceId + ":" + config.deviceAddress)) {
			Generic_ZbDevice device;
			switch (config.deviceId) {
			case Constants.COLOR_DIMMABLE_LIGHT_DEVICE_ID:
				device = new ColorDimmableLight(this, appManager, config);
				System.out.println("device created");
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case Constants.MAINS_POWER_OUTLET_DEVICE_ID:
				device = new MainsPowerOutlet(this, appManager, config);
				System.out.println("device created");
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			}
		}
		else {
			System.out.println("Add channel: " + config.channelAddress);
			System.out.println("device: " + config.deviceAddress);
			devices.get(config.interfaceId + ":" + config.deviceAddress).addChannel(config);
		}
	}

	public void resourceUnavailable(Generic_ZbConfig config) {
		Generic_ZbDevice device = devices.get(config.interfaceId + ":" + config.deviceAddress);
		if (device != null) {
			device.deleteChannel(config);
			// TODO check endpoints
			if (device.attributeChannel.isEmpty() && device.commandChannel.isEmpty()) {
				devices.remove(config.interfaceId + ":" + config.deviceAddress);
				device.close();
				device = null;
			}
		}
	}

	public Generic_ZbDevice getDevice(String key) {
		return devices.get(key);
	}

	public DeviceScanListener getDeviceScanListener() {
		return deviceScanListener;
	}

	@Override
	public void deviceFound(DeviceLocator deviceLocator) {
		String[] splitStringArray = deviceLocator.getParameters().split(":");
		if (devices
				.containsKey(deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress().toUpperCase())) {
			return;
		}
		if (splitStringArray[0].equals("ZigBee")) {
			if (splitStringArray[3].equals(Constants.HOME_AUTOMATION_PROFILE_ID)) {
				String deviceId = splitStringArray[2];
				Generic_ZbDevice zbDevice = null;
				switch (deviceId) {
				case Constants.COLOR_DIMMABLE_LIGHT_DEVICE_ID_STRING:
					zbDevice = new ColorDimmableLight(this, appManager, deviceLocator, null);
					break;
				case Constants.MAINS_POWER_OUTLET_DEVICE_ID_STRING:
					zbDevice = new DevelcoSmartPlug(this, appManager, deviceLocator, splitStringArray[1]);
					break;
				case Constants.SMART_PLUG_DEVICE_ID_STRING:
					zbDevice = new SmartPlug(this, appManager, deviceLocator, splitStringArray[1]);
					break;
				default:
					logger.info("Unsupported device detected: " + deviceLocator);
					break;
				}
				if (zbDevice != null) {
					logger.debug("device created");
					devices.put(deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress().toUpperCase(),
							zbDevice);
				}
			}

			// Handle non standard profile philips hue
			if (splitStringArray[3].equals(Constants.PHILIPS_HUE_MANUFACTURER_PROFILE)) {
				String deviceId = splitStringArray[2];
				Generic_ZbDevice zbDevice = null;
				switch (deviceId) {
				case Constants.PHILIPS_HUE_ID_STRING:
					zbDevice = new ColorDimmableLight(this, appManager, deviceLocator, splitStringArray[1]);
					break;
				default:
					logger.info("Unsupported device detected: " + deviceLocator);
					break;
				}
				if (zbDevice != null) {
					logger.debug("device created");
					devices.put(deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress().toUpperCase(),
							zbDevice);
				}
			}

			// Handle non standard profile develco
			if (splitStringArray[3].equals(Constants.DEVELCO_MANUFACTURER_PROFILE)) {
				String deviceId = splitStringArray[2];
				Generic_ZbDevice zbDevice = null;
				switch (deviceId) {
				case Constants.DEVELCO_ZHWR202_ID_STRING:
					// zbDevice = new DevelcoSmartPlug(this, appManager, deviceLocator);
					break;
				default:
					logger.info("Unsupported device detected: " + deviceLocator);
					break;
				}
			}
		}
	}

	@Override
	public void finished(boolean success, Exception e) {
		logger.debug("Generic ZBee Driver: DeviceScan finished!");
	}

	@Override
	public void progress(float ratio) {

	}
}
