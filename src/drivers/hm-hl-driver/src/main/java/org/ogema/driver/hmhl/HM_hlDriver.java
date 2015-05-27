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
package org.ogema.driver.hmhl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.driver.hmhl.devices.MotionDetector;
import org.ogema.driver.hmhl.devices.PowerMeter;
import org.ogema.driver.hmhl.devices.Remote;
import org.ogema.driver.hmhl.devices.CO2Detector;
import org.ogema.driver.hmhl.devices.SmokeSensor;
import org.ogema.driver.hmhl.devices.THSensor;
import org.ogema.driver.hmhl.devices.Thermostat;
import org.ogema.driver.hmhl.devices.WaterSensor;
import org.ogema.driver.hmhl.Activator;
import org.slf4j.Logger;

public class HM_hlDriver implements Application, DeviceScanListener, Runnable {
	/** Cached ApplicationManager */
	protected ApplicationManager appManager;
	/** Map of active devices */
	protected final Map<String, HM_hlDevice> devices; // String ==
	// interface:deviceAddress
	protected final DeviceDescriptor deviceDesc;
	public final Map<String, ChannelLocator> channelMap; // Map a name to a
	// channelLocator
	// (resourceId)

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("hm_hl");

	public HM_hlDriver() {
		this.devices = new HashMap<String, HM_hlDevice>();
		this.deviceDesc = new DeviceDescriptor();
		this.channelMap = new HashMap<String, ChannelLocator>();
	}

	@Override
	public void start(ApplicationManager appManager) {
		this.appManager = appManager;
		new Thread(this, "homematic-hld-deviceScan").start();
	}

	@Override
	public void stop(AppStopReason reason) {
		for (HM_hlDevice device : devices.values()) {
			device.close();
		}
	}

	public void resourceAvailable(HM_hlConfig config) {
		String[] splitAddress = config.channelAddress.split(":");
		HM_hlDevice device = null;
		if (!devices.containsKey(config.interfaceId + ":" + config.deviceAddress)) {
			switch (deviceDesc.getSubType(splitAddress[0])) {
			case "THSensor":
				device = new THSensor(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "threeStateSensor":
				device = new WaterSensor(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "thermostat":
				device = new Thermostat(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "powerMeter":
				device = new PowerMeter(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "smokeDetector":
				device = new SmokeSensor(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "CO2Detector":
				device = new CO2Detector(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "motionDetector":
				device = new MotionDetector(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "remote":
			case "pushbutton":
			case "swi":
				device = new Remote(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			default:
				throw new RuntimeException("Message not supported");
			}
		}
		else {
			device = devices.get(config.interfaceId + ":" + config.deviceAddress);
		}
		if (device != null)
			device.addChannel(config);

	}

	public void resourceUnavailable(HM_hlConfig config) {
		HM_hlDevice device = devices.get(config.interfaceId + ":" + config.deviceAddress);
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

	public HM_hlDevice getDevice(String key) {
		return devices.get(key);
	}

	public DeviceDescriptor getDeviceDescriptor() {
		return deviceDesc;
	}

	@Override
	public void deviceFound(DeviceLocator deviceLocator) {

		logger.debug("Device found!");
		String deviceKey = deviceLocator.getInterfaceName() + ":" + deviceLocator.getDeviceAddress();
		HM_hlDevice device = null;
		if (!devices.containsKey(deviceKey)) {

			switch (deviceDesc.getSubType(deviceLocator.getParameters())) {
			case "THSensor":
				device = new THSensor(this, appManager, deviceLocator);
				break;
			case "threeStateSensor":
				device = new WaterSensor(this, appManager, deviceLocator);
				break;
			case "thermostat":
				device = new Thermostat(this, appManager, deviceLocator);
				break;
			case "powerMeter":
				device = new PowerMeter(this, appManager, deviceLocator);
				break;
			case "smokeDetector":
				device = new SmokeSensor(this, appManager, deviceLocator);
				break;
			case "CO2Detector":
				device = new CO2Detector(this, appManager, deviceLocator);
				break;
			case "motionDetector":
				device = new MotionDetector(this, appManager, deviceLocator);
				break;
			case "remote":
			case "pushbutton":
			case "swi":
				device = new Remote(this, appManager, deviceLocator);
				break;
			default:
				throw new RuntimeException("Message not supported");
			}
			if (device != null)
				System.out.println(deviceKey);
			devices.put(deviceKey, device);
		}
		else {
			device = devices.get(deviceKey);
		}
	}

	@Override
	public void finished(boolean success, Exception e) {
		logger.debug("DeviceScan finished!");
	}

	@Override
	public void progress(float ratio) {
	}

	@Override
	public void run() {
		while (true && Activator.bundleIsRunning) {
			try {
				Thread.sleep(Constants.DEVICE_SCAN_WAITING_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("Start device scan ...");
			try {
				this.appManager.getChannelAccess().discoverDevices("homematic-driver", "USB", null, this);
			} catch (UnsupportedOperationException e) {
				logger.error("homematic-driver seems not to support device scan.");
			} catch (NoSuchInterfaceException e) {
				logger
						.error("homematic-driver reported problem during the communication oder the specified interface.");
			} catch (NoSuchDriverException e) {
				logger
						.debug("Either the homematic-driver is not yet installed or the coordinator hardware is not connected.");
			}
			logger.info("... device scan finished!");
		}
	}
}
