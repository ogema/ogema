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
package org.ogema.apps.app.installation;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.AdministrationManager;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.installationmanager.InstallationManagement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class AppInstallation implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;

	@Reference
	private AdministrationManager administrationManager;

	@Reference
	private PermissionManager permissionManager;

	@Reference
	private InstallationManagement installationManager;

	private BundleContext bundleContext;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());

		String webResourcePackage = "org.ogema.apps.app.installation".replace(".", "/");

		bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();

		AppInstallationController appController = new AppInstallationController(permissionManager, installationManager);
		AppInstallationServlet appServlet = new AppInstallationServlet(appController, administrationManager,
				bundleContext);

		appManager.getWebAccessManager().registerWebResourcePath("/", webResourcePackage + "/gui");
		appManager.getWebAccessManager().registerWebResourcePath("/servlet", appServlet);

	}

	@Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResourcePath("/");
		appMan.getWebAccessManager().unregisterWebResourcePath("/servlet");
	}

}
