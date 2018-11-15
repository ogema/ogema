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
/**
 * 
 */
package org.ogema.impl.administration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdminLogger;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.installationmanager.SourcesManagement;
//import org.ogema.core.logging.LoggerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;

/**
 *
 */
@Component(specVersion = "1.2")
@Service(AdministrationManager.class)
public class AdministrationImpl implements AdministrationManager {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("ogema.administration");

	@Reference
	protected FrameworkClock clock;

//	@Reference
//	protected LoggerFactory loggerFactory;
	
	@Reference
	private SourcesManagement sourcesManager;

	@Reference
	PermissionManager pMan;
	// ServiceTracker<PermissionManager, PermissionManager> pManTracker;

	@Reference
	private InstallationManagement instMngr;

	private AccessManager accessMngr;

	private BundleContext context;

	private BundleStoragePolicy storagePolicy;

	@Override
	public InstallationManagement getInstallationManager() {
		return instMngr;
	}

	private ApplicationRegistry appreg;

	@Activate
	protected synchronized void activate(final BundleContext ctx, Map<String, Object> config) {
		this.context = ctx;
		this.accessMngr = pMan.getAccessManager();
		this.appreg = pMan.getApplicationRegistry();

		/*
		 * ServiceTrackerCustomizer<PermissionManager, PermissionManager> permMancust = new
		 * ServiceTrackerCustomizer<PermissionManager, PermissionManager>() {
		 * 
		 * @Override public PermissionManager addingService(ServiceReference<PermissionManager> sr) { pMan =
		 * ctx.getService(sr); completeInit(ctx); return pMan; }
		 * 
		 * @Override public void modifiedService(ServiceReference<PermissionManager> sr, PermissionManager t) { }
		 * 
		 * @Override public void removedService(ServiceReference<PermissionManager> sr, PermissionManager t) { }
		 * 
		 * };
		 * 
		 * pManTracker = new ServiceTracker<>(ctx, PermissionManager.class, permMancust); pManTracker.open();
		 */

		storagePolicy = new BundleStoragePolicy(context, Bundle.INSTALLED | Bundle.UNINSTALLED, null);
		if (storagePolicy.restrictFSAccess)
			storagePolicy.open();
		else
			storagePolicy = null;
	}
	
	@Deactivate
	protected synchronized void deactivate(BundleContext ctx) {
		this.context = null;
		this.accessMngr = null;
		this.appreg = null;
		this.storagePolicy = null;
	}

	/*
	 * protected void completeInit(BundleContext ctx) { this.accessMngr = pMan.getAccessManager(); ((InstallManagerImpl)
	 * instMngr).start(context, pMan); pManTracker.close(); }
	 */

	@Override
	public AppID getAppByBundle(Bundle b) {
		return appreg.getAppByBundle(b);
	}

	@Override
	public AdminApplication getAppById(String id) {
		return appreg.getAppById(id);
	}

	@Override
	synchronized public List<AdminApplication> getAllApps() {
		return appreg.getAllApps();
	}

	@Override
	public List<UserAccount> getAllUsers() {
		ArrayList<UserAccount> res = new ArrayList<>();

		List<String> users = accessMngr.getAllUsers();
		for (String user : users) {
			UserAccount acc = UserAccountImpl.createinstance(user, accessMngr.isNatural(user), pMan);
			assert acc.getName() != null && acc.getName().equals(user) : "Invalid user account created for user " + user;
			res.add(acc);
		}
		return res;
	}

	@Override
	public UserAccount getUser(String name) {
		UserAccount acc = UserAccountImpl.createinstance(name, accessMngr.isNatural(name), pMan);
		return acc;
	}

//	@Override
//	public void setUserCredential(String userName, String password) {
//		accessMngr.setNewPassword(userName, password);
//	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AdminLogger> getAllLoggers() {
		try {
			final ServiceReference<org.ogema.core.logging.LoggerFactory> ref = context.getServiceReference(org.ogema.core.logging.LoggerFactory.class);
			final org.ogema.core.logging.LoggerFactory loggerFactory = context.getService(ref);
			Method m = loggerFactory.getClass().getMethod("getAllLoggers"); // assuming we have an OgemaLoggerFactory
			return (List<AdminLogger>) m.invoke(loggerFactory);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException  
				| NullPointerException | IllegalStateException ex) {
			// not actually an error, if another logger is used instead of the ogema-logger
			logger.error("could not retrieve loggers: " + ex); 
			return Collections.emptyList();
		}
	}

	/*
	 * Get the first occurrence of a class that belongs to an ogema application. All entries up to the class parameter
	 * are ignored. (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getContextApp(java.lang.Class)
	 */
	@Override
	public AppID getContextApp(Class<?> ignore) {
		return appreg.getContextApp(ignore);
	}

	/*
	 * Get the first occurrence of a class that belongs to an osgi bundle. All entries up to the class parameter are
	 * ignored. (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getContextApp(java.lang.Class)
	 */
	@Override
	public Bundle getContextBundle(Class<?> ignore) {
		return appreg.getContextBundle(ignore);
	}

	@Override
	public FrameworkClock getFrameworkClock() {
		return clock;
	}

	@Override
	public UserAccount createUserAccount(String name, boolean isnatural) {
		if (!pMan.handleSecurity(new AdminPermission(AdminPermission.USER)))
			throw new SecurityException("Permission to create user denied: user name " + name);
		UserAccount res = new UserAccountImpl(name, isnatural, pMan);
		return res;
	}

	@Override
	public void removeUserAccount(String name) {
		if (!pMan.handleSecurity(new AdminPermission(AdminPermission.USER)))
			throw new SecurityException("Permission to remove user denied: user name " + name);
		pMan.getAccessManager().removeUser(name);
	}

	@Override
	public SourcesManagement getSources() {
		return sourcesManager;
	}
}
