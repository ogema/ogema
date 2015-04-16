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
package org.ogema.apps.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.ogema.accesscontrol.AppPermissionFilter;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.security.WebAccessManager;
import org.ogema.persistence.ResourceDB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

public class OgemaAdmin implements Application, BundleActivator, UserAdminListener {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private PermissionManager pMan;
	AdministrationManager admin;
	ServiceTracker<HttpService, HttpService> tracker;

	ResourceDB db;

	long bundleID;

	InstallationManagement instMan;

	ApplicationManager appMngr;

	public void start(BundleContext bc) throws BundleException {
		final BundleContext ctx = bc;
		this.osgi = bc;
		final OgemaAdmin adminapp = this;
		this.bundleID = bc.getBundle().getBundleId();
		/*
		 * Get PermissionManager to delegate the permission checks
		 */
		pMan = (PermissionManager) bc.getService(bc.getServiceReference(PermissionManager.class.getName()));
		admin = (AdministrationManager) bc.getService(bc.getServiceReference(AdministrationManager.class.getName()));
		instMan = (InstallationManagement) bc
				.getService(bc.getServiceReference(InstallationManagement.class.getName()));
		db = (ResourceDB) bc.getService(bc.getServiceReference(ResourceDB.class.getName()));

		bc.registerService(UserAdminListener.class.getName(), this, null);

		bc.registerService(OgemaAdmin.class, this, null);
		bc.registerService(Application.class, this, null);

		// Register some http resources
		// pMan.getWebAccess().registerWebResource("/example", "/web");

		ServiceTrackerCustomizer<HttpService, HttpService> cust = new ServiceTrackerCustomizer<HttpService, HttpService>() {

			@Override
			public HttpService addingService(ServiceReference<HttpService> sr) {
				WebAccessManager wam = pMan.getWebAccess();
				HttpService http = (HttpService) ctx.getService(ctx.getServiceReference(HttpService.class.getName()));
				new AdminServlet(wam, pMan, adminapp);
				new SimulatorServlet(wam);
				return http;
			}

			@Override
			public void modifiedService(ServiceReference<HttpService> sr, HttpService t) {
			}

			@Override
			public void removedService(ServiceReference<HttpService> sr, HttpService t) {
			}
		};

		tracker = new ServiceTracker<>(ctx, HttpService.class, cust);
		tracker.open();
	}

	public void stop(BundleContext context) {
	}

	@Override
	public void start(ApplicationManager appManager) {
		logger.info("AppPermission Test bundle started!");
		logger.info("{} started", getClass().getName());

		this.appMngr = appManager;

		// createTestUser("master1", false);
		// createTestUser("rest12", false);
		// createTestUser("guest123", true);
		// Populate Resource pool for testing
		// new TestResourcePopulation(appManager.getResourceManagement()).populate();
	}

	private void createTestUser(String usr, boolean natural) {
		UserAccount account = admin.createUserAccount(usr, natural);
		if (!account.getName().equals(usr))
			assert (true);
		pMan.getAccessManager().authenticate(usr, "", natural);
		account.setNewPassword("", usr + "pw");
		pMan.getAccessManager().authenticate(usr, usr, natural);

		AppID id = admin.getAppByBundle(osgi.getBundle(bundleID));
		if (pMan.getAccessManager().isAppPermitted(usr, id)) {
			logger.info(String.format("User %s already permitted to access %s", usr, id.getIDString()));
		}
		else {
			logger.info(String.format("User %s not permitted to access %s. Permission will be granted.", usr, id
					.getIDString()));
			AppPermissionFilter props = new AppPermissionFilter(id.getBundle().getSymbolicName(), null, null, null);

			pMan.getAccessManager().addPermission(usr, props);
		}
	}

	@Override
	public void stop(AppStopReason reason) {
		logger.debug("{} stopped", getClass().getName());
	}

	BundleContext osgi;

	static final String PERMS_ENTRY_NAME = "OSGI-INF/permissions.perm";

	@Override
	public void roleChanged(UserAdminEvent event) {
		logger.info(String.format("UserAdnminEvent %s occured. Role: %s, Type: %s", event.toString(), event.getRole(),
				event.getType()));
	}
}
