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
package org.ogema.app.securityconsumer;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;

public class Activator implements BundleActivator, Application {
	public static boolean bundleIsRunning = true;

	public void start(BundleContext bc) throws IOException {
		bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext bc) throws IOException {
		bundleIsRunning = false;
	}

	@Override
	public void start(ApplicationManager appManager) {
		try {
			new Zwave(appManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new HMPowerboxTest(appManager.getResourceAccess());
	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub

	}

}
