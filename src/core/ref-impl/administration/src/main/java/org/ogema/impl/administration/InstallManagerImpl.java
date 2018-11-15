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
package org.ogema.impl.administration;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.installationmanager.InstallationManagement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;

@Component(specVersion = "1.2")
@Service(InstallationManagement.class)
public class InstallManagerImpl implements InstallationManagement {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger("ogema.administration");

	@Reference
	private PermissionManager pMan;

	// TODO synchronization... volatile fields?
	private ApplicationRegistry appReg;

	// private ServiceTracker<ApplicationSource, ApplicationSource> tracker;

	private BundleContext osgi;

	@Activate
	protected void activate(final BundleContext ctx, Map<String, Object> config) {
		this.osgi = ctx;
		this.appReg = pMan.getApplicationRegistry();
	}

	@Override
	public void install(final InstallableApplication iapp) {
		if (!pMan.handleSecurity(new AdminPermission(AdminPermission.APP)))
			throw new SecurityException("Permission to install app denied: app name: " + iapp.getLocation());

		boolean res = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				Bundle b = osgi.getBundle(iapp.getLocation());
				if (b == null) {
					return install0(iapp, false);
				}
				else {
					iapp.setBundle(b);
					return update0(iapp);
				}
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
		logger.info("Bundle updated from " + b.getLocation());
		return true;
	}

	private boolean install0(InstallableApplication iapp, boolean start) {
		Bundle b = null;
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
		if (start) {
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
				appid = appReg.getAppByBundle(iapp.getBundle());
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
				logger.info("App installation failed! Installed bundle is probably not an OGEMA Application: "
						+ b.getLocation());
				return false;
			}
		}
		return true;
	}

	@Override
	public InstallableApplication createInstallableApp(String address, String name) {
		InstallableApplication app = new InstallableApp(address, name);
		return app;
	}

	@Override
	public InstallableApplication createInstallableApp(Bundle b) {
		InstallableApplication app = new InstallableApp(b);
		app.setAppid(appReg.getAppByBundle(b));
		return app;
	}
}
