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
package org.ogema.driver.generic_xbee;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.driver.generic_xbee.devices.PikkertonZbs110V2Device;
import org.ogema.driver.generic_xbee.devices.PikkertonZbs122Device;
import org.slf4j.Logger;

public class GenericXbeeZbDriver implements Application, DeviceScanListener {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("generic_xbee_hl");
	/** Cached ApplicationManager */
	ApplicationManager appManager;
	ChannelAccess channelAccess;
	/** Map of active LemonegDevices */
	private final Map<String, GenericXbeeZbDevice> devices; // InterfaceId:DeviceAddress,
	// GenericXbeeZbDevice
	public final Map<String, ChannelLocator> channelMap; // Map a name to a channelLocator (resourceId)
	private int resourceNameCounter = 0;

	ChannelEventListener channelEventListener;

	public GenericXbeeZbDriver() {
		devices = new HashMap<String, GenericXbeeZbDevice>();
		channelMap = new HashMap<String, ChannelLocator>();
	}

	@Override
	public void start(ApplicationManager appManager) {
		// TODO Auto-generated method stub
		this.appManager = appManager;
		channelAccess = appManager.getChannelAccess();

		logger.info("GenericXbeeZbDriver started");
	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub

	}

	public void resourceAvailable(GenericXbeeZbConfig config) {
		if (!devices.containsKey(config.interfaceId + ":" + config.deviceAddress)) {
			String channelAddress = config.channelAddress;
			String[] splitAddress = channelAddress.split(":");
			GenericXbeeZbDevice device;
			switch (splitAddress[2]) {
			case "HA SENSORKNOTEN":
				break;
			case Constants.ZBS_122_DEVICE:
				device = new PikkertonZbs122Device(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				logger.debug("Channellocator: " + device.channelLocator.toString());
				break;
			case Constants.ZBS_110V2_DEVICE:
				device = new PikkertonZbs110V2Device(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				logger.debug("Channellocator: " + device.channelLocator.toString());
				break;
			}
		}
	}

	public Map<String, GenericXbeeZbDevice> getDevices() {
		return devices;
	}

	public void resourceUnavailable(GenericXbeeZbConfig config) {
		if (devices.containsKey(config.driverId + ":" + config.interfaceId + ":" + config.deviceAddress + ":"
				+ config.channelAddress)) {
			GenericXbeeZbDevice device = devices.get(config.interfaceId + ":" + config.deviceAddress.toUpperCase());
			devices.remove(config.interfaceId + ":" + config.deviceAddress.toUpperCase());
			// TODO removeFromUpdateListener(device.channelLocator);
			logger.info("delete: " + device.channelLocator);
			try {
				channelAccess.deleteChannel(device.channelLocator);
			} catch (ChannelConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getResourceNameCounter() {
		return resourceNameCounter++;
	}

	@Override
	public void deviceFound(DeviceLocator deviceLocator) {
		GenericXbeeZbDevice device = null;
		try {
			String[] splitStringArray = deviceLocator.getParameters().split(":");
			if (devices.containsKey(deviceLocator.getInterfaceName() + ":"
					+ deviceLocator.getDeviceAddress().toUpperCase())) {
				return;
			}
			if (splitStringArray[0].equals("XBee")) {
				switch (splitStringArray[1]) {
				case "HA SENSORKNOTEN":
					break;
				case "ZBS-122":
					device = new PikkertonZbs122Device(this, appManager, deviceLocator);
					break;
				case "ZBS-110V2":
					device = new PikkertonZbs110V2Device(this, appManager, deviceLocator);
					break;
				}
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (device != null) {
			logger.info("\nDevice found: " + deviceLocator.getDeviceAddress() + "\n" + deviceLocator.getParameters());
			devices
					.put(deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress().toUpperCase(),
							device);
		}
	}

	@Override
	public void finished(boolean success, Exception e) {
		logger.debug("Generic XBee Driver: DeviceScan finished!");
	}

	@Override
	public void progress(float ratio) {

	}
}
