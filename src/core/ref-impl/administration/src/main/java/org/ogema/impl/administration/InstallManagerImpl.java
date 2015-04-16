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
package org.ogema.impl.administration;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;

@Component(specVersion = "1.1")
@Service(InstallationManagement.class)
public class InstallManagerImpl implements InstallationManagement {

	HashMap<String, ApplicationSource> appStores;

	static final String LOCAL_APPSTORE_NAME = "localAppDirectory";
	static final String LOCAL_APPSTORE_LOCATION = "./appstore/";
	static final String PROP_NAME_LOCAL_APPSTORE_LOCATION = "org.ogema.local.appstore";

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	private PermissionManager pMan;

	AdministrationManager admin;

	ServiceTracker<ApplicationSource, ApplicationSource> tracker;

	BundleContext osgi;

	String localAppStore;

	AppStore tmpFileUpload;

	public void start(BundleContext bc, PermissionManager pm) {
		this.pMan = pm;
		this.admin = pm.getAdminManager();
		this.osgi = bc;

		localAppStore = System.getProperty(PROP_NAME_LOCAL_APPSTORE_LOCATION, LOCAL_APPSTORE_LOCATION);
		tmpFileUpload = new AppStore("localTempDirectory", "./temp/", true);
		initAppStores();

		ServiceTrackerCustomizer<ApplicationSource, ApplicationSource> trackerCustomizer = new ServiceTrackerCustomizer<ApplicationSource, ApplicationSource>() {

			@Override
			public ApplicationSource addingService(ServiceReference<ApplicationSource> sr) {
				ApplicationSource app = osgi.getService(sr);
				if (app == null) {
					logger.warn("got a null service object from service reference {}, bundle {}", sr, sr.getBundle());
					return null;
				}
				appStores.put(app.getName(), app);
				return app;
			}

			@Override
			public void modifiedService(ServiceReference<ApplicationSource> sr, ApplicationSource t) {
			}

			@Override
			public void removedService(ServiceReference<ApplicationSource> sr, ApplicationSource t) {
				appStores.remove(t.getName());
			}
		};

		tracker = new ServiceTracker<>(osgi, ApplicationSource.class, trackerCustomizer);
		tracker.open();
		// this.pMan = admin.pMan;
	}

	private void initAppStores() {
		appStores = new HashMap<>();
		appStores.put("localAppDirectory", new AppStore("localAppDirectory", localAppStore, true));
		appStores.put("remoteFHGAppStore", new AppStore("IIS_EXT_FTP", "ftp://ftpext:1234@ftp.iis.fraunhofer.de/",
				false));
	}

	@Override
	public void install(final InstallableApplication iapp) {
		if (!pMan.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Permission to install app denied: app name: " + iapp.getLocation());

		boolean res = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				Bundle b = osgi.getBundle(iapp.getLocation());
				if (b == null)
					return install0(iapp);
				else
					return update0(iapp);

			}
		});

		if (!res)
			throw new RuntimeException();
	}

	protected Boolean update0(InstallableApplication iapp) {
		Bundle b = iapp.getBundle();
		iapp.setState(InstallableApplication.InstallState.BUNDLE_INSTALLING);
		try {
			b.update();
		} catch (BundleException e1) {
			logger.info("Bundle update failed!");
			e1.printStackTrace();
			return false;
		}
		iapp.setState(InstallableApplication.InstallState.BUNDLE_INSTALLED);
		iapp.setBundle(b);
		logger.info("Bundle installed from " + b.getLocation());

		try {
			iapp.getBundle().start();
		} catch (BundleException e1) {
			logger.info("Bundle start failed!" + b.getLocation());
			e1.printStackTrace();
			return false;
		}
		// Wait until the Application service is reachable
		int tries = 20;
		AppID appid = null;
		while (appid == null && tries-- > 0) {
			appid = admin.getAppByBundle(iapp.getBundle());
			if (appid == null)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
		}
		if (appid != null) {
			iapp.setAppid(appid);
		}
		else {
			iapp.setState(InstallableApplication.InstallState.ABORTED);
			logger.info("App installation failed! Installed bundle is probably not an Application: " + b.getLocation());
			return false;
		}
		return true;
	}

	private boolean install0(InstallableApplication iapp) {
		Bundle b = null;
		AppPermission appPerm = iapp.getGrantedPermissions();
		pMan.installPerms(appPerm);
		{
			iapp.setState(InstallableApplication.InstallState.BUNDLE_INSTALLING);
			try {
				b = osgi.installBundle(iapp.getLocation());
			} catch (BundleException e1) {
				e1.printStackTrace();
			}
			if (b != null) {
				iapp.setState(InstallableApplication.InstallState.BUNDLE_INSTALLED);
				iapp.setBundle(b);
				logger.info("Bundle installed from " + b.getLocation());
			}
			else {
				logger.info("Bundle installation failed!");
				return false;
			}
		}

		try {
			iapp.getBundle().start();
		} catch (BundleException e1) {
			logger.info("Bundle start failed!" + b.getLocation());
			e1.printStackTrace();
			return false;
		}
		// Wait until the Application service is reachable
		int tries = 20;
		AppID appid = null;
		while (appid == null && tries-- > 0) {
			appid = admin.getAppByBundle(iapp.getBundle());
			if (appid == null)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
		}
		if (appid != null) {
			iapp.setAppid(appid);
		}
		else {
			iapp.setState(InstallableApplication.InstallState.ABORTED);
			logger.info("App installation failed! Installed bundle is probably not an Application: " + b.getLocation());
			return false;
		}
		return true;
	}

	@Override
	public ApplicationSource connectAppSource(String address) {
		ApplicationSource src = appStores.get(address);
		if (src != null)
			src.connect();
		return src;
	}

	@Override
	public void disconnectAppSource(String address) {
		ApplicationSource src = appStores.get(address);
		if (src != null)
			src.disconnect();
	}

	@Override
	public List<ApplicationSource> getConnectedAppSources() {
		return new ArrayList<ApplicationSource>(appStores.values());
	}

	@Override
	public InstallableApplication createInstallableApp(String address, String name) {
		InstallableApplication app = new InstallableApp(address, name);
		return app;
	}

	@Override
	public InstallableApplication createInstallableApp(Bundle b) {
		InstallableApplication app = new InstallableApp(b);
		app.setAppid(admin.getAppByBundle(b));
		return app;
	}

	@Override
	public ApplicationSource getDefaultAppStore() {
		return appStores.get(LOCAL_APPSTORE_NAME);
	}
}
