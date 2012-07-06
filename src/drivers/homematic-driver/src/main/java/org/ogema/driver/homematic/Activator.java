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

	@Activate
	public void activate(final BundleContext context, Map<String, Object> config) throws Exception {

		Thread connectThread = new Thread() {
			@Override
			public void run() {

				Connection con = new Connection(connectionLock, "USB", "HMUSB");
				synchronized (connectionLock) {
					while (!con.hasConnection()) {
						try {
							connectionLock.wait();
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}
				}

				driver = new HMDriver(channelAccess);
				driver.addConnection(con);

				new ShellCommands(driver, context);

				registration = context.registerService(ChannelDriver.class, driver, null);
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
	}

	protected void setChannelAccess(ChannelAccess ca) {
		this.channelAccess = ca;
	}
}
