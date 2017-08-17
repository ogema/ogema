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
package org.ogema.tests.regression;

import java.io.IOException;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.tests.custommodel.TestCustomModelChildRemoved;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.example.app.windowheatcontrol.config.WindowHeatControlConfig;

public class Activator implements BundleActivator, Application {
	public static boolean bundleIsRunning = true;
	private ServiceRegistration<Application> me;
	private ApplicationManager appMan;
	private ResourceAccess resAcc;
	private ResourceManagement resMngr;
	private OgemaLogger logger;

	public void start(BundleContext bc) throws IOException {
		me = bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext bc) throws IOException {
		bundleIsRunning = false;
		me.unregister();
	}

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.resAcc = appManager.getResourceAccess();
		this.resMngr = appManager.getResourceManagement();
		this.logger = appManager.getLogger();
//		resourcePermissionTypePathCheck();
		createCustomModelInstance();
	}

	@Override
	public void stop(AppStopReason reason) {
	}

	private final WindowHeatControlConfig resourcePermissionTypePathCheck() {
		String configResourceDefaultName = WindowHeatControlConfig.class.getSimpleName().substring(0, 1).toLowerCase()
				+ WindowHeatControlConfig.class.getSimpleName().substring(1);
		WindowHeatControlConfig appConfigData = appMan.getResourceAccess().getResource(configResourceDefaultName);
		if (appConfigData != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			try {
				appConfigData = appMan.getResourceManagement().createResource(configResourceDefaultName,
						WindowHeatControlConfig.class);
			} catch (SecurityException e) {
				appMan.getLogger().debug("Expected SecurityException in {}", getClass().getName());
			}
			appConfigData.defaultWindowOpenTemperature().<TemperatureResource> create().setCelsius(25);
			appConfigData.roomConfigurations().create();
			appConfigData.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
		return appConfigData;
	}

	public void createCustomModelInstance() {
		Resource e = resAcc.getResource("resourceFromCustomModel");
		if (e == null) {
			e = resMngr.createResource("resourceFromCustomModel", TestCustomModelChildRemoved.class);
			e.activate(true);
			e.addOptionalElement("time_res").activate(true);
			e.addOptionalElement("bool_res").activate(true);
		}
		else {
			Resource r = resAcc.getResource("resourceFromCustomModel/bool_res");
			logger.info(r.getLocation());
			r = resAcc.getResource("resourceFromCustomModel/time_res");
			logger.info(r.getLocation());
		}
	}
}
