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
/**
 * 
 */
package org.ogema.driver.DRS485DE;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceDemandListener;

/**
 * This upper level driver patiently waits for a configuration resource to show up. When one or more configurations are
 * made available, it creates a DRS485DEDevice for each. This in turn tries to connect to the physical device and
 * updates the corresponding ElectricityMeter instance.
 * 
 * @author pau
 * 
 */
@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class DRS485DEDriver implements Application, ResourceDemandListener<DRS485DEConfigurationModel> {
	private ApplicationManager appManager;
	private OgemaLogger logger;
	private Map<DRS485DEConfigurationModel, DRS485DEDevice> devices = new HashMap<DRS485DEConfigurationModel, DRS485DEDevice>();

	@Override
	public void start(ApplicationManager appManager) {

		this.appManager = appManager;
		this.logger = appManager.getLogger();

		logger.info("DRS485DE-driver started");

		// register demand for DRS485DEConfigurationModel
		appManager.getResourceAccess().addResourceDemand(DRS485DEConfigurationModel.class, this);
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.info("DRS485DE-driver stopped");

		appManager.getResourceAccess().removeResourceDemand(DRS485DEConfigurationModel.class, this);

		for (DRS485DEDevice device : devices.values()) {
			device.close();
		}

		devices.clear();
	}

	@Override
	public void resourceAvailable(DRS485DEConfigurationModel resource) {
		DRS485DEDevice device = new DRS485DEDevice(appManager, resource);

		devices.put(resource, device);
	}

	@Override
	public void resourceUnavailable(DRS485DEConfigurationModel resource) {
		DRS485DEDevice device = devices.remove(resource);

		if (device != null) {
			device.close();
		}
	}
}
