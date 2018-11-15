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
