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
