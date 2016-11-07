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

import org.ogema.core.application.Application;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private ServiceRegistration<?> serviceRegistration;
	private HM_hlDriver driver;
	public static volatile boolean bundleIsRunning;
	private ShellCommands shellCommands;

	@Override
	public synchronized void start(BundleContext context) throws Exception {
		bundleIsRunning = true;
		driver = new HM_hlDriver();
		Application application = driver;
		serviceRegistration = context.registerService(Application.class.getName(), application, null);
		this.shellCommands = new ShellCommands(driver, context);
	}

	@Override
	public synchronized void stop(BundleContext context) throws Exception {
		bundleIsRunning = false;
		if (serviceRegistration != null)  
			serviceRegistration.unregister(); // should cause driver.stop() callback
		if (shellCommands != null) 
			shellCommands.close();
		shellCommands = null;
		driver = null;
	}

}
