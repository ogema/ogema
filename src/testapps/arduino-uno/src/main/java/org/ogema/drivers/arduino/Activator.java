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
package org.ogema.drivers.arduino;

import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.drivers.arduino.data.ClimateData;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private ServiceRegistration<?> registration;
	private ServiceReference<HardwareManager> hardwareManagerReference;
	private static HardwareManager hardwareManager;
	private static final boolean debug = true;

	@Override
	public void start(BundleContext context) throws Exception {
		if (debug)
			System.out.println("\n\n\n\n\n******Starting Arduino-Driver******");
		// import the HardwareManager serivce
		hardwareManagerReference = context.getServiceReference(HardwareManager.class);
		hardwareManager = context.getService(hardwareManagerReference);
		if (debug) {
			for (HardwareDescriptor desc : hardwareManager.getHardwareDescriptors()) {
				String identifier = desc.getIdentifier();
				String portName = null;
				System.out.println("Identifier: " + identifier);
				if (desc.getHardwareType() == HardwareType.USB) {
					portName = ((UsbHardwareDescriptor) desc).getPortName();
				}
				else if (desc.getHardwareType() == HardwareType.SERIAL) {
					portName = ((SerialHardwareDescriptor) desc).getPortName();
				}
				System.out.println("Port name: " + portName + "\n");
			}
		}
		String iface = "usb:1-1.3:1.0:2341:0043:7533531323735150D0C2";

		ClimateDataImpl climateDataImpl = new ClimateDataImpl(iface, null);
		context.registerService(ClimateData.class, climateDataImpl, null);
		// create driver instance
		// driver = new ModbusDriver();

		// export ChannelDriver Service to OSGi
		// the OGEMA core listens for newly added ChannelDriver services and
		// imports/registers them
		// for the OSGi filtering to work, the driver is exported under the name
		// of the INTERFACE
		// registration = context.registerService(ChannelDriver.class.getName(), driver, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {

		if (hardwareManagerReference != null)
			context.ungetService(hardwareManagerReference);

		if (registration != null)
			registration.unregister();
	}

	public static HardwareManager getHardwareManager() {
		return hardwareManager;
	}
}
