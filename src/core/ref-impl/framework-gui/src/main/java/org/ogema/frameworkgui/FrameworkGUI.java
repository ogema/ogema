/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.frameworkgui;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.osgi.framework.BundleContext;

// The annotations encapsule the OSGi required. They expose the service Application
// to OSGi, which the OGEMA framework uses to detect this piece of code as an
// OGEMA application.
@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class FrameworkGUI implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;

	@Reference
	private AdministrationManager administrationManager;

	@Reference
	private PermissionManager permissionManager;

	BundleContext bundleContext;

	@Activate
	protected void activate(BundleContext ctx) {
		this.bundleContext = ctx;
	}

	/**
	 * Start method is called by the framework once this application has been discovered. From the application's
	 * perspective, this is where the program starts. Applications memorize the reference to their ApplicationManager
	 * and usually register timers or resource demands here.
	 * 
	 * The example application registers a timer task to be periodically invoked by the framework.
	 */
	@Override
	public void start(ApplicationManager appManager) {
		// Store references to the application manager and common services for future use.
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());

		AccessManager accessManager = permissionManager.getAccessManager();
		FrameworkGUIController controller = new FrameworkGUIController(administrationManager, bundleContext,
				accessManager, permissionManager);

		appManager.getWebAccessManager().registerWebResource("/ogema", "org/ogema/frameworkgui/gui");
		appManager.getWebAccessManager().registerWebResource("/apps/ogema/framework/gui",
				new FrameworkGUIServlet(controller));
		try {
        	if (Boolean.getBoolean("org.ogema.gui.usecdn")) {
        		appManager.getWebAccessManager().registerStartUrl("/ogema/index2.html");
        	}
        } catch (SecurityException ok) {}
	}

	/*
	 * This is called when the application is stopped by the framework.
	 */
	@Override
	public void stop(AppStopReason reason) {
		if (appMan == null)
			return;
		appMan.getWebAccessManager().unregisterWebResource("/ogema");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/framework/gui");
		logger.debug("{} stopped", getClass().getName());
		appMan = null;
		resAcc = null;
		resMan = null;
		logger = null;
	}
	
	@Deactivate
	public void deactivate() {
		this.bundleContext = null;
		try {
			stop(null);
		} catch (Exception e) {}
	}

}
