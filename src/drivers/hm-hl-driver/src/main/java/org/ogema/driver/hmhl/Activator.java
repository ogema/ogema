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
