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
package org.ogema.impl.administration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AdminPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.AdminLogger;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.FrameworkClock;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.InstallationManagement;
import org.ogema.core.logging.LoggerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 *
 */
@Component(specVersion = "1.1")
@Service(AdministrationManager.class)
public class AdministrationImpl extends SecurityManager implements AdministrationManager {

	@Reference
	protected FrameworkClock clock;

	@Reference
	protected LoggerFactory loggerFactory;

	/**
	 * List of installed apps by app id string
	 */
	HashMap<String, AdminApplication> appAcc;
	/**
	 * List of installed apps by the bundle reference
	 */
	// FIXME: there can be more than 1 app per bundle
	private HashMap<Bundle, AppID> appsByBundles;

	PermissionManager pMan;
	ServiceTracker<PermissionManager, PermissionManager> pManTracker;

	@Reference
	private InstallationManagement instMngr;

	private AccessManager accessMngr;

	private BundleContext context;

	public AdministrationImpl() {
		appAcc = new HashMap<>();
		appsByBundles = new HashMap<>();
	}

	@Override
	public InstallationManagement getInstallationManager() {
		return instMngr;
	}

	protected void activate(final BundleContext ctx, Map<String, Object> config) {
		this.context = ctx;

		ServiceTrackerCustomizer<AdminApplication, AdminApplication> adminAppCust = new ServiceTrackerCustomizer<AdminApplication, AdminApplication>() {

			@Override
			public AdminApplication addingService(ServiceReference<AdminApplication> sr) {
				AdminApplication aaa = ctx.getService(sr);
				addAppAccess(aaa);
				return aaa;
			}

			@Override
			public void modifiedService(ServiceReference<AdminApplication> sr, AdminApplication t) {
			}

			@Override
			public void removedService(ServiceReference<AdminApplication> sr, AdminApplication t) {
				removeAppAccess(t);
			}

		};

		ServiceTracker<AdminApplication, AdminApplication> tracker = new ServiceTracker<>(ctx, AdminApplication.class,
				adminAppCust);
		tracker.open();

		ServiceTrackerCustomizer<PermissionManager, PermissionManager> permMancust = new ServiceTrackerCustomizer<PermissionManager, PermissionManager>() {

			@Override
			public PermissionManager addingService(ServiceReference<PermissionManager> sr) {
				pMan = ctx.getService(sr);
				completeInit(ctx);
				return pMan;
			}

			@Override
			public void modifiedService(ServiceReference<PermissionManager> sr, PermissionManager t) {
			}

			@Override
			public void removedService(ServiceReference<PermissionManager> sr, PermissionManager t) {
			}

		};

		pManTracker = new ServiceTracker<>(ctx, PermissionManager.class, permMancust);
		pManTracker.open();
	}

	protected void completeInit(BundleContext ctx) {
		this.accessMngr = pMan.getAccessManager();
		((InstallManagerImpl) instMngr).start(context, pMan);
		pManTracker.close();
	}

	synchronized private void removeAppAccess(AdminApplication aaa) {
		String appIDString = aaa.getID().getIDString();
		appAcc.remove(appIDString);
	}

	synchronized private void addAppAccess(AdminApplication aaa) {
		AppID appid = aaa.getID();
		String appIDString = appid.getIDString();

		// Add app to the list of installed apps
		appAcc.put(appIDString, aaa);
		appsByBundles.put(aaa.getBundleRef(), appid);
	}

	@Override
	public AppID getAppByBundle(Bundle b) {
		for (Map.Entry<Bundle, AppID> appEntry : appsByBundles.entrySet()) {
			if (appEntry.getKey().equals(b)) {
				return appEntry.getValue();
			}
		}
		return null;
	}

	@Override
	public AdminApplication getAppById(String id) {
		AdminApplication aaa = appAcc.get(id);
		return aaa;
	}

	@Override
	synchronized public List<AdminApplication> getAllApps() {
		return new ArrayList<>(appAcc.values());
	}

	@Override
	public List<UserAccount> getAllUsers() {
		ArrayList<UserAccount> res = new ArrayList<>();

		List<String> users = accessMngr.getAllUsers();
		for (String user : users) {
			UserAccount acc = UserAccountImpl.createinstance(user, accessMngr.isNatural(user), pMan);
			res.add(acc);
		}
		return res;
	}

	@Override
	public void setUserCredential(String userName, String password) {
		accessMngr.setNewPassword(userName, password);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AdminLogger> getAllLoggers() {
		try {
			// XXX need additional interface/service
			Method m = loggerFactory.getClass().getMethod("getAllLoggers");
			return (List<AdminLogger>) m.invoke(loggerFactory);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
			loggerFactory.getLogger(getClass()).error("could not retrieve loggers", ex);
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
		AppID result = null;
		final Class<?> param = ignore;
		result = AccessController.doPrivileged(new PrivilegedAction<AppID>() {
			public AppID run() {
				/*
				 * get the classes on the call stack, the first element of the result is always class of this. All
				 * entries from top of stack until first occurence of the class given as parameter are skipped.
				 */
				Class<?>[] clss = getClassContext();
				int index = 0;
				for (Class<?> cls : clss) {
					clss[index++] = null;
					if (cls == param)
						break;
				}
				for (Class<?> cls : clss) {
					if (cls != null) {
						ClassLoader cl = cls.getClassLoader();
						if (cl instanceof BundleReference) {
							BundleReference ref = (BundleReference) cl;
							Bundle b = ref.getBundle();
							AppID id = appsByBundles.get(b);
							if (id != null)
								return id;
						}
					}
				}
				return null;
			}
		});
		return result;
	}

	/*
	 * Get the first occurrence of a class that belongs to an osgi bundle. All entries up to the class parameter
	 * are ignored. (non-Javadoc)
	 * 
	 * @see org.ogema.core.administration.AdministrationManager#getContextApp(java.lang.Class)
	 */
	@Override
	public Bundle getContextBundle(Class<?> ignore) {
		Bundle result = null;
		final Class<?> param = ignore;
		result = AccessController.doPrivileged(new PrivilegedAction<Bundle>() {
			public Bundle run() {
				/*
				 * get the classes on the call stack, the first element of the result is always class of this. All
				 * entries from top of stack until first occurence of the class given as parameter are skipped.
				 */
				Class<?>[] clss = getClassContext();
				int index = 0;
				for (Class<?> cls : clss) {
					clss[index++] = null;
					if (cls == param)
						break;
				}
				for (Class<?> cls : clss) {
					if (cls != null) {
						ClassLoader cl = cls.getClassLoader();
						if (cl instanceof BundleReference) {
							BundleReference ref = (BundleReference) cl;
							Bundle b = ref.getBundle();
							return b;
						}
					}
				}
				return null;
			}
		});
		return result;
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
}
