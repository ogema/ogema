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
