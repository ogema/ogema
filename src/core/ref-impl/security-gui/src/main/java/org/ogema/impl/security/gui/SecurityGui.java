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
package org.ogema.impl.security.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
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
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

public class SecurityGui implements Application, BundleActivator {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	static final String PERMS_ENTRY_NAME = "OSGI-INF/permissions.perm";
	static final String allPerm = "java.security.AllPermission";

	private PermissionManager pMan;
	AdministrationManager admin;
	ServiceTracker<HttpService, HttpService> tracker;

	ResourceDB db;

	long bundleID;

	InstallationManagement instMan;

	ApplicationManager appMngr;
	BundleContext osgi;

	public void start(BundleContext bc) throws BundleException {
		final BundleContext ctx = bc;
		this.osgi = bc;
		final SecurityGui adminapp = this;
		this.bundleID = bc.getBundle().getBundleId();
		/*
		 * Get PermissionManager to delegate the permission checks
		 */
		pMan = (PermissionManager) bc.getService(bc.getServiceReference(PermissionManager.class.getName()));
		admin = (AdministrationManager) bc.getService(bc.getServiceReference(AdministrationManager.class.getName()));
		instMan = (InstallationManagement) bc
				.getService(bc.getServiceReference(InstallationManagement.class.getName()));
		db = (ResourceDB) bc.getService(bc.getServiceReference(ResourceDB.class.getName()));

		bc.registerService(SecurityGui.class, this, null);
		bc.registerService(Application.class, this, null);

		ServiceTrackerCustomizer<HttpService, HttpService> cust = new ServiceTrackerCustomizer<HttpService, HttpService>() {

			@Override
			public HttpService addingService(ServiceReference<HttpService> sr) {
				WebAccessManager wam = pMan.getWebAccess();
				HttpService http = (HttpService) ctx.getService(ctx.getServiceReference(HttpService.class.getName()));
				new SecurityGuiServlet(wam, pMan, adminapp);
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

	}

	@Override
	public void stop(AppStopReason reason) {
	}

	public static List<String> getLocalPerms(String location) {
		// skip protocol part of the path
		int index = location.indexOf(':');
		String jarpath;
		if (index != -1) {
			jarpath = location.substring(index + 1);
		}
		else
			jarpath = location;

		List<String> permsArray = new ArrayList<>();
		File f = new File(jarpath);
		JarFile jar = null;
		try {
			jar = new JarFile(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JarEntry perms = null;
		perms = jar.getJarEntry(PERMS_ENTRY_NAME);

		BufferedReader br = null;
		try {
			if (perms != null) {
				br = new BufferedReader(new InputStreamReader(jar.getInputStream(perms)));
			}
		} catch (IOException e) {
		}
		String line;
		if (br == null) {// If the jar entry doesn't exist, AllPermission is desired.
			permsArray.add(allPerm);
		}
		else {
			try {
				line = br.readLine();
				while (line != null) {
					line = line.trim();
					if (line.startsWith("#") || line.startsWith("//") || line.equals("")) {
						continue;
					}
					permsArray.add(line);
					line = br.readLine();
				}
				jar.close();
			} catch (IOException e) {
			}
		}
		return permsArray;
	}
}
