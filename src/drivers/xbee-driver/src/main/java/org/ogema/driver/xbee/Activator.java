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
package org.ogema.driver.xbee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;

@Component(specVersion = "1.1")
public class Activator {
	private XBeeDriver driver;
	private ServiceRegistration<?> registration;

	public static ArrayList<String> hardwareIds = new ArrayList<String>();

	@Reference
	private HardwareManager hardwareManager;
	@Reference(bind = "setChannelAccess")
	protected ChannelAccess channelAccess;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("xbee-driver");

	public void activate(BundleContext context, Map<String, Object> config) throws Exception {
		// hardwareManagerReference = context.getServiceReference(HardwareManager.class);
		// hardwareManager = context.getService(hardwareManagerReference);

		File hardwareIdFile = context.getDataFile("hardwareIds.config");
		if (hardwareIdFile == null) { // TODO handle properly
			logger
					.info("The platform does not support bundle data files."
							+ " The Hardware id's of the interface the coordinater expected on can not be cached persistently."
							+ " The interface name can be specified as the value of the property org.ogema.driver.xbee.portname, like COM1, /dev/ttyUSB0");
		}
		else if (!hardwareIdFile.exists()) {
			FileOutputStream fos = new FileOutputStream(hardwareIdFile);
			PrintWriter pw = new PrintWriter(fos);
			pw.write("usb:1-1.2:1.0:0403:6001:");
			pw.close();
			fos.close();
			hardwareIds.add("0403:6001");
		}
		else {
			BufferedReader br = new BufferedReader(new FileReader(hardwareIdFile));
			String line = br.readLine();
			if (line == null) { // File is empty
				BufferedWriter bw = new BufferedWriter(new FileWriter(hardwareIdFile));
				bw.write("0403:6001"); // XStick hardwareId
				bw.newLine();
				bw.close();
			}
			else {
				hardwareIds.add(line);
			}
			while ((line = br.readLine()) != null) {
				hardwareIds.add(line);
				logger.info("Hardware id found " + line);
			}
			br.close();

		}

		final File remoteDevicesFile = context.getDataFile("remoteDevices.config");
		if (remoteDevicesFile == null) {
			logger.info("The platform does not support bundle data files."
					+ " No connection infornation of any devices can be cached. "
					+ "The network setup could take some extra seconds.");
		}
		else if (!remoteDevicesFile.exists()) {
			remoteDevicesFile.createNewFile();
		}

		driver = new XBeeDriver(channelAccess, hardwareManager, remoteDevicesFile);

		new ShellCommands(driver, context, hardwareManager);

		registration = context.registerService(ChannelDriver.class, driver, null);
	}

	public void deactivate(Map<String, Object> config) throws Exception {
		registration.unregister();
	}

	public HardwareManager getHardwareManager() {
		return hardwareManager;
	}

	protected void setChannelAccess(ChannelAccess ca) {
		this.channelAccess = ca;
	}
}
