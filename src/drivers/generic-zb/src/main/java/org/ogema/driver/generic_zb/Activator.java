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
package org.ogema.driver.generic_zb;

import org.ogema.core.application.Application;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;

public class Activator implements BundleActivator {

	private ServiceRegistration<?> serviceRegistration;
	private Generic_ZbDriver driver;
	private ShellCommands shell;
	protected boolean running;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("generic_zigbee_hl");

	@Override
	public void start(BundleContext context) throws Exception {
		driver = new Generic_ZbDriver();
		Application application = driver;
		serviceRegistration = context.registerService(Application.class.getName(), application, null);
		shell = new ShellCommands(driver, context);
		running = true;
		Thread scan = new Thread(new Runnable() {

			@Override
			public void run() {
				int scantimes = 5;
				String scan = System.getProperty("org.ogema.driver.xbee.scannetwork");
				boolean forever = false;
				if (scan != null)
					forever = true;
				while (running) {
					if (!forever && scantimes >= 0)
						scantimes--;
					if (!forever && scantimes < 0)
						running = false;
					try {
						Thread.sleep(17000);
						logger.debug("Device scan started ...");
						shell.deviceScan(null);
						logger.debug(".. device scan finished.");
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
		scan.setName("generic-zb-hld-scanDevices");
		scan.start();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		running = false;
		serviceRegistration.unregister();
	}

}
