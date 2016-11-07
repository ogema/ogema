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
package org.ogema.driver.xbee;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
//import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Component(specVersion = "1.1")
public class Activator {
	private XBeeDriver driver;
	private ServiceRegistration<?> registration;

	@Reference
	private HardwareManager hardwareManager;

	public void activate(BundleContext context, Map<String, Object> config) throws Exception {

		this.driver = new XBeeDriver(hardwareManager);

		new ShellCommands(driver, context, hardwareManager);

		registration = context.registerService(ChannelDriver.class, driver, null);
	}

	public void deactivate(Map<String, Object> config) throws Exception {
		registration.unregister();
		driver.shutdown();
		// TODO shutdown all running threads
	}

	public HardwareManager getHardwareManager() {
		return hardwareManager;
	}
}
