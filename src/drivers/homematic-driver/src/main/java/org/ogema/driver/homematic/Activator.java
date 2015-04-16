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
import org.ogema.core.channelmanager.ChannelAccess;
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
	@Reference(bind = "setChannelAccess")
	protected ChannelAccess channelAccess;
	private final Object connectionLock = new Object();
	public static boolean bundleIsRunning = true;

	@Activate
	public void activate(final BundleContext context, Map<String, Object> config) throws Exception {

		driver = new HMDriver(channelAccess);
		registration = context.registerService(ChannelDriver.class, driver, null);

		Thread connectThread = new Thread() {
			@Override
			public void run() {

				Connection con = new Connection(connectionLock, "USB", "HMUSB");
				synchronized (connectionLock) {
					while (!con.hasConnection() && bundleIsRunning) {
						try {
							connectionLock.wait();
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}
				}

				driver.addConnection(con);

				new ShellCommands(driver, context);

				driver.enablePairing("USB");

			}
		};
		connectThread.start();
	}

	@Deactivate
	public void deactivate(Map<String, Object> config) throws Exception {
		hardwareManager.removeListener(driver);
		if (registration != null)
			registration.unregister();
		bundleIsRunning = false;
	}

	protected void setChannelAccess(ChannelAccess ca) {
		this.channelAccess = ca;
	}
}
