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
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.frameworkadministration.controller.AppStoreController;
import org.ogema.frameworkadministration.controller.LoggerController;
import org.ogema.frameworkadministration.controller.UserController;
import org.ogema.frameworkadministration.servlet.FAServletLogger;
import org.ogema.frameworkadministration.servlet.FAServletUser;
import org.ogema.persistence.ResourceDB;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.useradmin.UserAdmin;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class FrameworkAdministration implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;

	@Reference
	private AdministrationManager administrationManager;

	@Reference
	private PermissionManager permissionManager;

	@Reference
	UserAdmin userAdmin;

	private FAServletAppStore appStoreServlet;

	@Reference
	private ResourceDB resourceDB;
	private BundleContext bundleContext;
	private long bundleID;
	private InstallationManagement installationManager;

	@Override
	public void start(ApplicationManager appManager) {
		// Store references to the application manager and common services for future use.
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());

		bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		AccessManager accessManager = permissionManager.getAccessManager();

		bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		permissionManager = (PermissionManager) bundleContext.getService(bundleContext
				.getServiceReference(PermissionManager.class.getName()));
		installationManager = (InstallationManagement) bundleContext.getService(bundleContext
				.getServiceReference(InstallationManagement.class.getName()));

		resourceDB = (ResourceDB) bundleContext.getService(bundleContext
				.getServiceReference(ResourceDB.class.getName()));

		LoggerController.getInstance().setAdministrationManager(administrationManager);

		AppStoreController.getInstance().setAdministrationManager(administrationManager);
		AppStoreController.getInstance().setPermissionManager(permissionManager);
		AppStoreController.getInstance().setResourceDB(resourceDB);
		AppStoreController.getInstance().setBundleContext(bundleContext);
		AppStoreController.getInstance().setInstallationManager(installationManager);

		UserController.getInstance().setAccessManager(accessManager);
		UserController.getInstance().setPermissionManager(permissionManager);
		UserController.getInstance().setBundleContext(bundleContext);
		UserController.getInstance().setAdministrationManager(administrationManager);
		UserController.getInstance().setAppManager(appManager);
		UserController.getInstance().setUserAdmin(userAdmin);

		bundleID = bundleContext.getBundle().getBundleId();

		String aliasHtml = appManager.getWebAccessManager().registerWebResource("/ogema/frameworkadminindex",
				"org/ogema/frameworkadministration/gui");

		String aliasLoggerServlet = appManager.getWebAccessManager().registerWebResource("/apps/ogema/frameworkadmin",
				new FAServletLogger());

		String aliasUserServlet = appManager.getWebAccessManager().registerWebResource(
				"/apps/ogema/frameworkadminuser", new FAServletUser());

		appStoreServlet = new FAServletAppStore(permissionManager, bundleContext, bundleID, administrationManager);
		appStoreServlet.register(permissionManager.getWebAccess());

		logger.info("registered html on {}", aliasHtml);
		logger.info("registered logger servlet on {}", aliasLoggerServlet);
		logger.info("registered user servlet on {}", aliasUserServlet);

	}

	@Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResource("/ogema/frameworkadminindex");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/frameworkadmin");
		appMan.getWebAccessManager().unregisterWebResource("/apps/ogema/frameworkadminuser");
		appStoreServlet.unregister(permissionManager.getWebAccess());
		logger.debug("{} stopped", getClass().getName());
	}

}
