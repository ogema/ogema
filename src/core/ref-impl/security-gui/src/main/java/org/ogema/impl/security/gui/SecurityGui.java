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
package org.ogema.impl.security.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.installationmanager.SourcesManagement;
import org.ogema.core.security.WebAccessManager;
import org.ogema.persistence.ResourceDB;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.LoggerFactory;

/**
 * Main class of the security GUI application. Since the application is started, SecurityGUIServlet object is created.
 * 
 * @author Zekeriya Mansuroglu
 *
 */

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class SecurityGui implements Application {

	static final String PERMS_ENTRY_NAME = "OSGI-INF/permissions.perm";
	static final String allPerm = "java.security.AllPermission";
	static final String FORBIDDENS_FILE = "./security/forbiddens.perm";
	private static final String GUI_ALIAS = "/security-gui";
	private static final String GUI_SERVLET_ALIAS = "/security/config";

	@Reference
	private PermissionManager pMan;
	@Reference
	AdministrationManager admin;
	@Reference
	UserAdmin ua;
	@Reference
	ResourceDB db;

	long bundleID;

	@Reference
	InstallationManagement instMan;
	
	@Reference 
	SourcesManagement sources;

	ApplicationManager appMngr;

	BundleContext osgi;
	private SecurityGuiServlet sgs;
	private WebAccessManager wam;

	@Activate
	public void activate(BundleContext bc) {
		this.osgi = bc;
		this.bundleID = bc.getBundle().getBundleId();
	}
	
	@Deactivate
	public void deactivate() {
		this.osgi = null;
	}

	@Override
	public void start(final ApplicationManager appManager) {
		this.appMngr = appManager;
		this.wam = appManager.getWebAccessManager();
		sgs = new SecurityGuiServlet(pMan, SecurityGui.this);
		wam.registerWebResource(GUI_ALIAS, "/admin");
		wam.registerWebResource(GUI_SERVLET_ALIAS, sgs);
	}

	@Override
	public void stop(AppStopReason reason) {
		if (wam == null)
			return;
		wam.unregisterWebResource(GUI_ALIAS);
		wam.unregisterWebResource(GUI_SERVLET_ALIAS);
		appMngr = null;
		wam = null;
		sgs = null;
	}

	public static List<String> getLocalPerms(Bundle b) {
		List<String> permsArray = new ArrayList<>();
		BufferedReader br = null;
		URL url = b.getEntry(PERMS_ENTRY_NAME);
		if (url != null) {
			InputStream is;
			try {
				is = url.openStream();
				br = new BufferedReader(new InputStreamReader(is));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String line;
		if (br == null) {// If the jar entry doesn't exist, AllPermission is desired.
			permsArray.add(allPerm);
		}
		else {
			try {
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("#") || line.startsWith("//") || line.equals("")) {
						continue;
					}
					permsArray.add(line);
				}
			} catch (IOException e) {
			}
		}
		return permsArray;
	}

	static List<String> getForbiddens() {
		List<String> permsArray = new ArrayList<>();
		BufferedReader br = null;
		InputStream is;
		try {
			is = new FileInputStream(new File(FORBIDDENS_FILE));
			br = new BufferedReader(new InputStreamReader(is));
		} catch (IOException e) {
			LoggerFactory.getLogger(SecurityGui.class)
					.warn("Forbidden permissions not set; falling back to default setting. " + e);
		}
		String line;
		if (br == null) {
			// will lead to an IllegalArgumentException in DefaultFilter#init,
			// that will cause the default filter to be used
			permsArray.add(allPerm);
		}
		else {
			try {
				br.mark(6);
				int c = br.read();
				if (c != 0xefbbbf)
					br.reset();
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("#") || line.startsWith("//") || line.equals("")) {
						continue;
					}
					permsArray.add(line);
				}
			} catch (IOException e) {
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return permsArray;
	}

	UserAdmin getUserAdmin() {
		return ua;
	}
}
