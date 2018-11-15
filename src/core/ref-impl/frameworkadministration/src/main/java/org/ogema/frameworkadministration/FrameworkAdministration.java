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
