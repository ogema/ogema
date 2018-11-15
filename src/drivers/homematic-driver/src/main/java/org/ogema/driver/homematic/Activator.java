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
package org.ogema.driver.homematic;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Component(specVersion = "1.2")
public class Activator {
	private HMDriver driver;
	private ServiceRegistration<?> registration;
	@Reference
	private HardwareManager hardwareManager;
	public static volatile boolean bundleIsRunning;
	private ShellCommands sc;

	@Activate
	public synchronized void activate(final BundleContext context, Map<String, Object> config) throws Exception {
		bundleIsRunning = true;
		driver = new HMDriver();
		driver.establishConnection();
		registration = context.registerService(ChannelDriver.class, driver, null);

		sc = new ShellCommands(driver, context);
	}

	@Deactivate
	public synchronized void deactivate(Map<String, Object> config) throws Exception {
		bundleIsRunning = false;
		if (driver != null) {
			for (Map.Entry<String, Connection> entry : driver.getConnections().entrySet()) {
				entry.getValue().close();
			}
			driver.getConnections().clear();
			if (driver.connectThread != null)
				driver.connectThread.interrupt(); // thread does not stop otherwise
			if (driver.pairing != null)
				driver.pairing.interrupt();
		}
		if (registration != null)
			registration.unregister();
		registration = null;
		if (sc != null)
			sc.close();
		sc = null;
		driver = null;
	}

}
