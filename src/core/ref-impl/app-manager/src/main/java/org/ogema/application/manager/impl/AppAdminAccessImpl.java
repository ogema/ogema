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
/**
 *
 */
package org.ogema.application.manager.impl;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdminLogger;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.administration.RegisteredPatternListener;
import org.ogema.core.administration.RegisteredResourceDemand;
import org.ogema.core.administration.RegisteredResourceListener;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.administration.RegisteredTimer;
import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Zekeriya Mansuroglu, Fraunhofer IIS
 */
public class AppAdminAccessImpl implements AdminApplication {

	final AppIDImpl id;
	final private Bundle bundle;
	final ApplicationManagerImpl appman;
	AppPermission perms;
	protected ServiceRegistration<AdminApplication> registration;

	public AppAdminAccessImpl(Bundle b, Application app, AppIDImpl id, ApplicationManagerImpl appman, AccessManager am) {
		this.id = id;
		this.bundle = b;
		this.appman = appman;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see org.ogema.core.installationmanager.InstallableApplication#getName()
	//	 */
	//	@Override
	//	public String getName() {
	//		return bundle.getSymbolicName();
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see org.ogema.core.installationmanager.InstallableApplication#getDescription()
	//	 */
	//	@Override
	//	public String getDescription() {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see org.ogema.core.installationmanager.InstallableApplication#getPresentationURI()
	//	 */
	//	@Override
	//	public String getPresentationURI() {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see org.ogema.core.installationmanager.InstallableApplication#getPresentationSnippetURI()
	//	 */
	//	@Override
	//	public String getPresentationSnippetURI() {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * 
	//	 * @see org.ogema.core.installationmanager.InstallableApplication#getOSGiPermissionsRequested()
	//	 */
	//	@Override
	//	public List<String> getPermissionDemand() {
	//		List<String> permsArray = new ArrayList<>();
	//		URL requestedPermissions = bundle.getEntry(PERMS_ENTRY_NAME);
	//
	//		BufferedReader br = null;
	//		try {
	//			if (requestedPermissions != null) { // If the jar entry doesn't exist, AllPermission is desired.
	//				InputStream is = requestedPermissions.openStream();
	//				br = new BufferedReader(new InputStreamReader(is));
	//			}
	//		} catch (IOException e) {
	//			appman.getLogger().warn("", e);
	//		}
	//		String line;
	//		if (br == null) {
	//			permsArray.add(allPerm);
	//		}
	//		else {
	//			try {
	//				line = br.readLine();
	//				while (line != null) {
	//					line = line.trim();
	//					if (line.startsWith("#") || line.startsWith("//") || line.equals("")) {
	//						continue;
	//					}
	//					permsArray.add(line);
	//					line = br.readLine();
	//				}
	//			} catch (IOException e) {
	//				appman.getLogger().warn("", e);
	//			}
	//		}
	//		return Collections.synchronizedList(permsArray);
	//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.installationmanager.InstallableApplication#install(java.util.List, java.lang.String)
	 */
	// @Override
	// public void install(List<AppPermission> grantOsgiPerms, String group) {
	// // TODO Auto-generated method stub
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdminApplication#getID()
	 */
	@Override
	public AppID getID() {
		return id;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see org.ogema.core.administration.AdminApplication#getPermissions()
	// */
	// @Override
	// public List<AppPermissionType> getPermissions() {
	// return requestedPermissions.getTypes();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdminApplication#stop()
	 */
	//	@Override
	//	public void stop() {
	//		// TODO Auto-generated method stub
	//
	//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdminApplication#start()
	 */
	//	@Override
	//	public void start() {
	//		// TODO Auto-generated method stub
	//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdminApplication#remove()
	 */
	//	@Override
	//	public void remove() {
	//		// TODO Auto-generated method stub
	//
	//	}

	@Override
	public Bundle getBundleRef() {
		return bundle;
	}

	@Override
	public List<RegisteredResourceDemand> getResourceDemands() {
		return appman.resMan.getResourceDemands();
	}

	@Override
	public List<RegisteredResourceListener> getResourceListeners() {
		return appman.resMan.getResourceListeners();
	}

	@Override
	public List<RegisteredValueListener> getValueListeners() {
		return appman.resMan.getValueListeners();
	}

	@Override
	public List<RegisteredStructureListener> getStructureListeners() {
		return appman.resMan.getStructureListeners();
	}

	@Override
	public List<RegisteredPatternListener> getPatternListeners() {
		if (appman.advAcc == null)
			return Collections.emptyList();
		else {
			return appman.advAcc.getRegisteredPatternListeners();
		}
	}

	@Override
	public List<RegisteredTimer> getTimers() {
		return appman.getTimers();
	}

	@Override
	public List<RegisteredAccessModeRequest> getAccessModeRequests() {
		return appman.resMan.getAccessRequests();
	}

	@Override
	public List<AdminLogger> getLoggers() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public boolean isRunning() {
		return !appman.executor.isShutdown();
	}

	@Override
	public String toString() {
		return getID().toString();
	}
	
	@Override
	public WebAccessManager getWebAccess() {
		return appman.tracker.getWebAccessManager(appman.getAppID());
	}
	
	@Override
	public boolean isWebAccessAllowed(HttpServletRequest req) {
		SessionAuth sesAuth = (SessionAuth) req.getSession().getAttribute("ogemaAuth");
		String user = null;
		if (sesAuth != null) // if security is disabled sesAuth is null
			user = sesAuth.getName();
		AccessManager accMan = appman.tracker.getPermissionManager().getAccessManager();
		return accMan.isAppPermitted(user, getID());
	}

}
