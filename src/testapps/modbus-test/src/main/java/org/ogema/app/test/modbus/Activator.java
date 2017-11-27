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
/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.app.test.modbus;

import java.io.IOException;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.StringResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator, Application {
	private ServiceRegistration<Application> me;
	private TEADevice device;

	public void start(BundleContext bc) throws IOException {
		me = bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext bc) throws IOException {
		me.unregister();
	}

	@Override
	public void start(final ApplicationManager appManager) {
		TEAConfigurationModel conf = appManager.getResourceManagement().createResource("teadeviceconfig",
				TEAConfigurationModel.class);
		conf.activate(true);
		StringResource devAddr = conf.deviceAddress().create();
		devAddr.activate(true);
		devAddr.setValue("localhost:502");
		StringResource resname = conf.resourceName().create();
		resname.setValue("teaResource");
		device = new TEADevice(appManager, conf);
	}

	@Override
	public void stop(AppStopReason reason) {
		device.close();
	}
	

}
