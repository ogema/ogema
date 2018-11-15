/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.driver.hmhl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.driver.hmhl.devices.CO2Detector;
import org.ogema.driver.hmhl.devices.DoorWindowSensor;
import org.ogema.driver.hmhl.devices.MotionDetector;
import org.ogema.driver.hmhl.devices.PowerMeter;
import org.ogema.driver.hmhl.devices.Remote;
import org.ogema.driver.hmhl.devices.SmokeSensor;
import org.ogema.driver.hmhl.devices.SwitchPlug;
import org.ogema.driver.hmhl.devices.THSensor;
import org.ogema.driver.hmhl.devices.Thermostat;
import org.ogema.driver.hmhl.devices.WaterSensor;
import org.slf4j.Logger;

public class HM_hlDriver implements Application, DeviceScanListener, Runnable {
	/** Cached ApplicationManager */
	protected ApplicationManager appManager;
	/** Map of active devices */
	protected final Map<String, HM_hlDevice> devices; // String ==
	// interface:deviceAddress
	protected final DeviceDescriptor deviceDesc;
	public final Map<String, ChannelConfiguration> channelMap; // Map a name to a
	private volatile Thread thread;
	// channelLocator
	// (resourceId)

	private Logger logger;// = org.slf4j.LoggerFactory.getLogger("hm_hl");

	public HM_hlDriver() {
		this.devices = new HashMap<String, HM_hlDevice>();
		this.deviceDesc = new DeviceDescriptor();
		this.channelMap = new HashMap<String, ChannelConfiguration>();
	}

	@Override
	public void start(ApplicationManager appManager) {
		logger = appManager.getLogger();
		this.appManager = appManager;
		thread = new Thread(this, "homematic-hl-deviceScan");
		thread.start();
	}

	@Override
	public void stop(AppStopReason reason) {
		Activator.bundleIsRunning = false;
		for (HM_hlDevice device : devices.values()) {
			device.close();
		}
		if (thread != null) 
			thread.interrupt();
		thread = null;
	}

	/**
	 * Just to support shell commands
	 * 
	 * @param config
	 */
	public void resourceAvailable(HM_hlConfig config) {
		String[] splitAddress = config.channelAddress.split(":");
		HM_hlDevice device = null;
		if (!devices.containsKey(config.interfaceId + ":" + config.deviceAddress)) {
			String s = deviceDesc.getSubType(splitAddress[0]);
			switch (s) {
			case "THSensor":
				device = new THSensor(this, appManager, config);
				devices.put(config.interfaceId + ":" + config.deviceAddress, device);
				break;
			case "threeStateSensor":  // can be either a water sensor or a door window contact
				boolean isDoorWindowContact = config.deviceParameters.equals("00B1"); // FIXME better way to identify this?
				if (isDoorWindowContact)
					device = new DoorWindowSensor(this, appManager, config);
				else
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
			case "switch":
				device = new SwitchPlug(this, appManager, config);
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
				throw new RuntimeException("Type not supported: " + s);
			}
		}
		else {
			device = devices.get(config.interfaceId + ":" + config.deviceAddress);
		}
		if (device != null)
			device.addChannel(config);

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
			String s = deviceDesc.getSubType(deviceLocator.getParameters());
			switch (s) {
			case "THSensor":
				device = new THSensor(this, appManager, deviceLocator);
				break;
			case "threeStateSensor": // water sensor or door window contact
				boolean isDoorWindowContact = deviceLocator.getParameters().equals("00B1"); // FIXME better way to identify this?
				if (isDoorWindowContact)
					device = new DoorWindowSensor(this, appManager, deviceLocator);
				else
					device = new WaterSensor(this, appManager, deviceLocator);
				break;
			case "thermostat":
				device = new Thermostat(this, appManager, deviceLocator);
				break;
			case "powerMeter":
				device = new PowerMeter(this, appManager, deviceLocator);
				break;
			case "switch":
				device = new SwitchPlug(this, appManager, deviceLocator);
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
				throw new RuntimeException("Type not supported: " + s);
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
			} catch (InterruptedException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!Activator.bundleIsRunning)
				return;
			logger.debug("Start device scan ...");
			try {
				this.appManager.getChannelAccess().discoverDevices("homematic-driver", "USB", null, this);
			} catch (ChannelAccessException e) {
				logger.warn("device scan failed", e.getCause());
				e.printStackTrace();
			}
			logger.debug("... device scan finished!");
		}
	}
}
