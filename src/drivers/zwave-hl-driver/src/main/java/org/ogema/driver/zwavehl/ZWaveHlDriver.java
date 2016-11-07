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
package org.ogema.driver.zwavehl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.driver.zwavehl.devices.DoorOpeningSensor;
import org.ogema.driver.zwavehl.devices.MotionTemperatureLightSensor;
import org.ogema.driver.zwavehl.devices.RelaySwitch;
import org.ogema.driver.zwavehl.devices.SwitchBox;

/**
 * 
 * @author baerthbn
 * 
 */
public class ZWaveHlDriver implements Application, DeviceListener, DeviceScanListener {
	/** Cached ApplicationManager */
	protected ApplicationManager appManager;
	/** Map of active devices */
	protected final Map<String, ZWaveHlDevice> devices; // String == interface:deviceAddress

	private OgemaLogger logger;

	public ZWaveHlDriver() {
		this.devices = new HashMap<String, ZWaveHlDevice>();
	}

	@Override
	public void start(ApplicationManager appManager) {
		logger = appManager.getLogger();
		this.appManager = appManager;
		// new Thread(this, "zwave-hl-deviceScan").start();
		try {
			appManager.getChannelAccess().addDeviceListener("zwave-driver", this);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop(AppStopReason reason) {
		for (ZWaveHlDevice device : devices.values()) {
			device.terminate();
		}
	}

	/**
	 * This method is called by the ShellCommand "createChannel" to add a new device to high-level driver.
	 * 
	 * @param config
	 */
	public void resourceAvailable(ZWaveHlConfig config) {
		ZWaveHlDevice device = null;
		if (!devices.containsKey(config.interfaceId + ":" + config.deviceAddress)) {
			switch (config.deviceParameters.split(":")[0]) {
			case "010f.0700.1000": // MANU_ID:PROD_TYPE:PROD_ID
				device = new DoorOpeningSensor(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "0154.1100.0001":
				device = new SwitchBox(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "013C.0001.0003":
			case "013c.0001.0003":
				device = new RelaySwitch(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "010f.0800.1001":
				device = new MotionTemperatureLightSensor(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;				
			default:
				logger.error("Device " + config.deviceParameters.split(":")[0] + " not supported");
				// throw new RuntimeException("Message not supported");
			}
		}
		else {
			device = devices.get(config.interfaceId + ":" + config.deviceAddress);
		}
		if (device != null)
			device.addChannel(config);

	}

	public void resourceUnavailable(ZWaveHlConfig config) {
		ZWaveHlDevice device = devices.get(config.interfaceId + ":" + config.deviceAddress);
		if (device != null) {
			device.deleteChannel(config);
			if (device.valueChannel.isEmpty()) {
				devices.remove(config.interfaceId + ":" + config.deviceAddress);
				device.close();
				device = null;
			}
		}
	}

	public ZWaveHlDevice getDevice(String key) {
		return devices.get(key);
	}

	/**
	 * This method is called when a new device has been found by channelmanager's startDeviceScan, implemented in the
	 * LL-Driver.
	 */
	@Override
	public void deviceFound(DeviceLocator deviceLocator) {
		logger.debug("Device found!");
		String deviceKey = deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress();
		ZWaveHlDevice device = null;
		if (!devices.containsKey(deviceKey)) {
			switch (deviceLocator.getParameters().split(":")[0]) {
			case "010f.0700.1000": // MANU_ID:PROD_TYPE:PROD_ID
				device = new DoorOpeningSensor(this, appManager, deviceLocator);
				break;
			case "0154.1100.0001":
				device = new SwitchBox(this, appManager, deviceLocator);
				break;
			case "013C.0001.0003":
			case "013c.0001.0003":
				device = new RelaySwitch(this, appManager, deviceLocator);
				break;
			case "010f.0800.1001":
				device = new MotionTemperatureLightSensor(this, appManager, deviceLocator);
				break;				
			default:
				logger.error("Device " + deviceLocator.getParameters().split(":")[0] + " not supported");
			}
			if (device != null)
				devices.put(deviceKey, device);
		}
	}

	@Override
	public void finished(boolean success, Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void progress(float ratio) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method is called when a new device has been found by the LL driver.
	 */
	@Override
	public void deviceAdded(DeviceLocator deviceLocator) {
		logger.debug("Device found!");
		String deviceKey = deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress();
		ZWaveHlDevice device = null;
		if (!devices.containsKey(deviceKey)) {
			switch (deviceLocator.getParameters().split(":")[0]) {
			case "010f.0700.1000": // MANU_ID:PROD_TYPE:PROD_ID
				device = new DoorOpeningSensor(this, appManager, deviceLocator);
				break;
			case "0154.1100.0001":
				device = new SwitchBox(this, appManager, deviceLocator);
				break;
			case "013C.0001.0003":
			case "013c.0001.0003":
				device = new RelaySwitch(this, appManager, deviceLocator);
				break;
			case "010f.0800.1001":
				device = new MotionTemperatureLightSensor(this, appManager, deviceLocator);
				break;				
			case "0000.0001.0001": // UZB Z-Wave USB Adapter, coordinator device
			case "0109.1001.0101":
				break;
			default:
				logger.error("Device " + deviceLocator.getParameters().split(":")[0] + " not supported");
			}
			if (device != null) {
				devices.put(deviceKey, device);
			}
		}
	}

	@Override
	public void deviceRemoved(DeviceLocator locator) {
		String iface = locator.getInterfaceName();
		String addr = locator.getDeviceAddress();
		String key = iface + ":" + addr;
		ZWaveHlDevice dev = devices.remove(key);
		dev.terminate();
	}

	public ChannelConfiguration getChannel(String resourceId) {
		ChannelConfiguration chConf = null;
		
		for (ZWaveHlDevice device : devices.values()) {
			chConf = device.getChannel(resourceId);
			
			if (chConf != null)
				break;
		}
		return chConf;
	}
}
