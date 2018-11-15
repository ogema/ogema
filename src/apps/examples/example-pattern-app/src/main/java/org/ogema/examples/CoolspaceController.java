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
package org.ogema.examples;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.devices.whitegoods.CoolingDevice;

/**
 * A simple device controller.  
 */
public class CoolspaceController {

	private final OgemaLogger logger;
	private CoolspacePattern device;

	/**
	 * Creates and starts a new controller for a cooling device that was found.
	 *
	 * @param device Completed resource pattern for the cooling device
	 * @param appMan Reference to the application manager (as entry point to
	 * OGEMA)
	 */
	public CoolspaceController(CoolspacePattern device, ApplicationManager appMan) {
		this.logger = appMan.getLogger();
		this.device = device;
		// Add a resource listener to the temperature reading, which is invoked whenever the temperature of the device changes.
		device.temperature.addValueListener(tempChange);
	}

	/**
	 * Gets the device controlled by this controller.
	 */
	public CoolingDevice getDevice() {
		return device.model;
	}

	/**
	 * Listener for a temperature change. Called whenever the temperature of the
	 * cooling device changes and possibly changes the on/off settings if the
	 * temperature left the set constraints.
	 */
	private final ResourceValueListener<TemperatureResource> tempChange = new ResourceValueListener<TemperatureResource>() {

		@Override
		public void resourceChanged(TemperatureResource resource) {

			// read out current temperature and limits.
			final float T = device.temperature.getValue();
			final float min = device.minTemp.getValue();
			final float max = device.maxTemp.getValue();
			logger.debug(device + "::T = " + T);

			if (max < min) { // just a sanity check, in case the limits are non-sensible
				logger.warn("Temperature limits set for device " + device + " are not sensible: Tmax=" + max
						+ " < Tmin=" + min + ". Will not control device this time");
				return;
			}
			
			// react if limits are violated
			if (T > max || T < min) logger.info("Operating limits for fridge " + device.model.getLocation() + " violated.");
			if (T > max) {
				if (device.stateControl.getValue() != true) {
					logger.debug("turning device " + device + " on");
					device.stateControl.setValue(true);
				}
				return;
			}
			if (T < min) {
				if (device.stateControl.getValue() != false) {
					logger.debug("turning device " + device + " off");
					device.stateControl.setValue(false);
				}
				return;
			}

			// current control settings are fine for current value: do nothing.
		}
	};

	// Cleanup method in case the controller is removed (e.g. the device pattern was no longer valid): Unregisters the listener on the temperature reading.
	void stopListener() {
		device.temperature.removeValueListener(tempChange);
	}
}
