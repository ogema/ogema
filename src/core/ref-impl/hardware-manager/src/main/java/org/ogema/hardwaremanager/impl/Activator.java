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
package org.ogema.hardwaremanager.impl;

import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.hardwaremanager.api.NativeAccess;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BundleActivator required by the OSGi framework.
 */
public class Activator implements BundleActivator {
	private final static Logger logger = LoggerFactory.getLogger(Activator.class);;

	private ServiceRegistration<HardwareManager> managerRegistration;
	private ServiceReference<NativeAccess> nativeAccessReference;
	private HardwareManagerImpl manager;
	private NativeAccess nativeAccess;

	@Override
	public void start(BundleContext bcontext) throws Exception {

		// import the NativeAccess serivce
		nativeAccessReference = bcontext.getServiceReference(NativeAccess.class);

		if (null != nativeAccessReference)
			nativeAccess = bcontext.getService(nativeAccessReference);
		else {
			nativeAccess = new DummyNativeAccess();
			logger.warn("Starting hardwaremanager with Dummy NativeAccess!. NO DEVICES will be found. "
					+ "Start a bundle exporting the service {} before starting this bundle.", NativeAccess.class
					.getName());
		}

		manager = new HardwareManagerImpl(nativeAccess);

		// export the HardwareManager service
		managerRegistration = bcontext.registerService(HardwareManager.class, manager, null);
	}

	@Override
	public void stop(BundleContext bcontext) throws Exception {
		managerRegistration.unregister();
		manager.exit();
		if (nativeAccessReference != null)
			bcontext.ungetService(nativeAccessReference);
	}
}
