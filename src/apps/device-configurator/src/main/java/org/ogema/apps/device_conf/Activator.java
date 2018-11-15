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
package org.ogema.apps.device_conf;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.ogema.core.application.Application;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator for bundle device-configurator
 * 
 * @author bjg
 * 
 */
@Component
public class Activator {

	private ServiceRegistration<?> serviceRegistration;

	@Activate
	public void start(BundleContext context) throws Exception {

		DeviceConfigurator application = new DeviceConfigurator(context);
		serviceRegistration = context.registerService(Application.class.getName(), application, null);
	}

	@Deactivate
	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
	}

}
