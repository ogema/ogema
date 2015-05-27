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
package ${package};

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
