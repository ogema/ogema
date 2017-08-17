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
/**
 * 
 */
package org.ogema.driver.acudc243;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceDemandListener;

/**
 * This upper level driver patiently waits for a configuration resource to show up. When one or more configurations are
 * made available, it creates a AcuDC243Device for each. This in turn tries to connect to the physical device and
 * updates the corresponding ElectricityMeter instance.
 * 
 * @author pau
 * 
 */
public class AcuDC243Driver implements ResourceDemandListener<AcuDC243Configuration> {
	private ApplicationManager appManager;
	private OgemaLogger logger;
	private List<AcuDC243Device> devices = new ArrayList<AcuDC243Device>();

	public AcuDC243Driver(ApplicationManager appManager) {

		this.appManager = appManager;
		this.logger = appManager.getLogger();

		logger.info("AcuDC243-driver started");

		// register demand for AcuDC243Configuration
		appManager.getResourceAccess().addResourceDemand(AcuDC243Configuration.class, this);
	}

	public void shutdown() {
		logger.info("AcuDC243-driver stopped");

		appManager.getResourceAccess().removeResourceDemand(AcuDC243Configuration.class, this);

		for (AcuDC243Device device : devices) {
			device.close();
		}

		devices.clear();
	}

	@Override
	public void resourceAvailable(AcuDC243Configuration resource) {
		AcuDC243Device device = new AcuDC243Device(appManager, resource);
		devices.add(device);
	}

	@Override
	public void resourceUnavailable(AcuDC243Configuration resource) {
		for (AcuDC243Device device : devices) {
			if (device.configurationResource.equals(resource)) {
				devices.remove(device);
				device.close();
			}
		}
	}

	public List<AcuDC243Device> getDevices() {
		return devices;
	}
}
