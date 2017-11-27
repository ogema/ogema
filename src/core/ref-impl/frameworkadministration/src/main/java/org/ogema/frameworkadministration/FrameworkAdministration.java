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
package org.ogema.frameworkadministration;

import org.ogema.frameworkadministration.servlet.FAServletAppStore;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.frameworkadministration.controller.AppStoreController;
import org.ogema.frameworkadministration.controller.LoggerController;
import org.ogema.frameworkadministration.controller.UserController;
import org.ogema.frameworkadministration.servlet.FAServletLogger;
import org.ogema.frameworkadministration.servlet.FAServletUser;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.useradmin.UserAdmin;

@Component(service = Application.class)
public class FrameworkAdministration implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;
    
	private AppStoreController appStoreController;

	private AdministrationManager administrationManager;

	private PermissionManager permissionManager;

	UserAdmin userAdmin;

	private FAServletAppStore appStoreServlet;

	private long bundleID;
	private BundleContext bundleContext;
    
    @Activate
    public void activate(BundleContext ctx) {
        this.bundleContext = ctx;
    }

	@Override
	public void start(ApplicationManager appManager) {
		// Store references to the application manager and common services for future use.
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());
		AccessManager accessManager = permissionManager.getAccessManager();

		LoggerController.getInstance().setAdministrationManager(administrationManager);

		UserController.getInstance().setAccessManager(accessManager);
		UserController.getInstance().setPermissionManager(permissionManager);
//		UserController.getInstance().setBundleContext(bundleContext);
		UserController.getInstance().setAdministrationManager(administrationManager);
		UserController.getInstance().setAppManager(appManager);
//		UserController.getInstance().setUserAdmin(userAdmin);

		bundleID = bundleContext.getBundle().getBundleId();

		String aliasHtml = appManager.getWebAccessManager().registerWebResource("/ogema/frameworkadminindex",
				"org/ogema/frameworkadministration/gui");
		appManager.getWebAccessManager().registerStartUrl("/ogema/frameworkadminindex/index.html");
		String aliasLoggerServlet = appManager.getWebAccessManager().registerWebResource("/apps/ogema/frameworkadmin",
				new FAServletLogger());

		String aliasUserServlet = appManager.getWebAccessManager().registerWebResource(
				"/apps/ogema/frameworkadminuser", new FAServletUser());

		appStoreServlet = new FAServletAppStore(permissionManager, bundleContext, bundleID, administrationManager,
				appStoreController);
		appStoreServlet.register(appManager.getWebAccessManager());

		logger.debug("registered html on {}", aliasHtml);
		logger.debug("registered logger servlet on {}", aliasLoggerServlet);
		logger.debug("registered user servlet on {}", aliasUserServlet);

	}

	// release resources
	@Override
	public void stop(AppStopReason reason) {
		if (appMan == null)
			return;
		appMan.getWebAccessManager().unregisterWebResource("/ogema/frameworkadminindex");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/frameworkadmin");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/frameworkadminuser");
		appStoreServlet.unregister(appMan.getWebAccessManager());
		logger.debug("{} stopped", getClass().getName());
		appMan = null;
		resAcc = null;
		resMan = null;
		logger = null;
		appStoreServlet = null;
		UserController.getInstance().setAccessManager(null);
		UserController.getInstance().setPermissionManager(null);
//		UserController.getInstance().setBundleContext(bundleContext);
		UserController.getInstance().setAdministrationManager(null);
		UserController.getInstance().setAppManager(null);
	}
    
    @Deactivate
    public void deactivate(BundleContext ctx) {
    }
    
    @Reference
    void setAdministrationManager(AdministrationManager administrationManager) {
        this.administrationManager = administrationManager;
    }

    @Reference
    void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Reference
    void setUserAdmin(UserAdmin userAdmin) {
        this.userAdmin = userAdmin;
    }
    
    @Reference
    void setAppStoreController(AppStoreController asc) {
        this.appStoreController = asc;
    }

}
