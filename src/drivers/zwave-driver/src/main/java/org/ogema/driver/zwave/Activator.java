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
package org.ogema.driver.zwave;

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

/**
 * 
 * @author baerthbn
 * 
 */
@Component(specVersion = "1.2")
public class Activator {
	// FIXME We have to make the driver static, because reinstantiation causes a VM crash due to problems in the
	// openzwave lib.
	static private ZWaveDriver driver;
	private ServiceRegistration<?> registration;
	@Reference
	private HardwareManager hardwareManager;
	@Reference(bind = "setChannelAccess")
	protected ChannelAccess channelAccess;

	public static boolean bundleIsRunning;

	@Activate
	public void activate(final BundleContext context, Map<String, Object> config) throws Exception {
		bundleIsRunning = true;
		if (driver == null)
			driver = new ZWaveDriver(channelAccess, hardwareManager);
		driver.establishConnection();
		registration = context.registerService(ChannelDriver.class, driver, null);
		new ShellCommands(driver, context);
	}

	@Deactivate
	public void deactivate(Map<String, Object> config) throws Exception {
		bundleIsRunning = false;
		for (Map.Entry<String, Connection> entry : driver.getConnections().entrySet()) {
			entry.getValue().close();
		}

		if (registration != null)
			registration.unregister();
	}

	protected void setChannelAccess(ChannelAccess ca) {
		this.channelAccess = ca;
	}
}
