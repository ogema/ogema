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
