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
package org.ogema.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AppPermissionFilter;
import org.ogema.accesscontrol.ChannelPermission;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.app.securityprovider.Access;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.AppPermissionType;
import org.ogema.core.security.WebAccessManager;
import org.ogema.impl.security.AppPermissionTypeImpl;
import org.ogema.restricted.RestrictedAccess;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;

public class Activator implements BundleActivator, Access, Application {
	private static String fileName = "./ogemauser";
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
	AppID testAppID;
	AdministrationManager admin;
	Access acc;
	RestrictedAccess rAcc;

	BundleContext bc;

	private PermissionManager pMan;
	LogService log;
	private WebAccessManager web;

	public void start(BundleContext bc) throws BundleException {
		/*
		 * Register a service to be user by a restricted app
		 */
		this.bc = bc;
		bc.registerService(Access.class.getName(), this, null);
		bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext context) {
		// logout();
	}

	/**
	 * How to checks resource manager a ResourcePermission
	 */
	@Override
	public Object getResource(String path) {
		boolean check = false;
		try {
			if (pMan != null)
				check = pMan.handleSecurity(new ResourcePermission(path, ResourcePermission.READ));
		} catch (SecurityException e) {
			log(LogService.LOG_INFO, "ResourcePermission not granted to access to " + path);
			throw e;
		}
		if (check) {
			log(LogService.LOG_INFO, "ResourcePermission is granted to access to " + path);

			// Do actions to prepare the return value.
			return new Object();
		}
		else
			return null;
	}

	/**
	 * How to check resource manager a ResourceTypePermission
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object[] getResourcesOfType(String typename) {
		boolean check = false;
		Class<? extends Resource> cls = null;
		try {
			if (pMan != null) {
				cls = (Class<? extends Resource>) getClassPrivileged(typename);
			}
			check = pMan.handleSecurity(new ResourcePermission("*", cls, 0));
		} catch (SecurityException e) {
			log(LogService.LOG_INFO, "ResourcePermission not granted to access to resources of type " + typename);
			throw e;
		}
		if (check) {
			log(LogService.LOG_INFO, "ResourcePermission is granted to access to resources of type " + typename);

			// Do actions to prepare the return value.
			return new Object[1];
		}
		else
			return null;
	}

	private Class<?> getClassPrivileged(String typename) {
		Class<?> result = null;
		final String name = typename;
		result = AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
			public Class<?> run() {
				try {
					return Class.forName(name);
				} catch (ClassNotFoundException ioe) {
					ioe.printStackTrace();
				}
				return null;
			}
		});
		return result;
	}

	/**
	 * How to checks channel manager a ChannelPermission
	 */
	@Override
	public Object getChannel(String description) {
		boolean check = false;

		try {
			if (pMan != null)
				check = pMan.handleSecurity(new ChannelPermission(description));
		} catch (SecurityException e) {
			log(LogService.LOG_INFO, "ChannelPermission not granted to access to " + description);
			throw e;
		}
		if (check) {
			log(LogService.LOG_INFO, "ChannelPermission is granted to access to " + description);

			// Do actions to prepare the return value.
			return new Object();
		}
		else
			return null;
	}

	/**
	 * Check file access via privileged action. The caller doesn't need to have the appropriate file permission.
	 */
	@Override
	public void login(final String name) {
		final File f = new File(fileName);
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				if (f.exists()) {
					f.delete();
				}

				try {
					OutputStream os = new FileOutputStream(f);
					os.write(name.getBytes("UTF-8"));
					os.close();
					log(LogService.LOG_INFO, "User " + name + " logged in");
				} catch (IOException ioe) {
					log(LogService.LOG_WARNING, "Problem logging user in: " + ioe);
				}
				return null;
			}
		});
	}

	@Override
	public void logout() {
		final File f = new File(fileName);
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				if (!f.exists()) {
					throw new IllegalStateException("No user logged in");
				}

				f.delete();
				log(LogService.LOG_INFO, "User logged out");
				return null;
			}
		});
	}

	private void log(int level, String message) {
		if (log != null) {
			log.log(level, message);
		}
	}

	AppPermission getOwnAppPermission() throws Exception {
		AppPermission ap = null;
		ArrayList<AdminApplication> apps = (ArrayList<AdminApplication>) admin.getAllApps();
		for (AdminApplication entry : apps) {
			AppID aidi = entry.getID();
			if (aidi == null)
				throw new Exception("AppID is null.");
			String loc = aidi.getLocation();
			if (loc != null && aidi.getLocation().indexOf("access-provider") != -1) {
				testAppID = aidi;
				ap = pMan.getPolicies(aidi);
				return ap;
			}
			logger.info("App location: " + loc);
			logger.info("App ID String: " + aidi.getIDString());
		}
		return ap;
	}

	void printGrantedPerms(AppPermission ap) {
		logger.info("List of granted permissions: ");
		// Map<String, String> granted = ap.getGrantedPerms();
		List<AppPermissionType> ptypes = ap.getTypes();
		// Set<Entry<String, String>> tlrs = granted.entrySet();
		for (AppPermissionType entry : ptypes) {
			logger.info(entry.getDeclarationString());
		}
	}

	private void testSuit() {
		logger.info("\n==================\tTESTSUITE START\t===================");
		AppPermission ap = null;
		try {
			ap = getOwnAppPermission();
			printGrantedPerms(ap);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			logger.info("\n==================\tTEST START\t===================");
			testAppID();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testAddPermission();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testRemovePermission();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testAddException();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testRemoveException();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testGetExceptions();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testGetTypes();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			logger.info("\n==================\tTEST START\t===================");
			testInstallDefaultPlocies();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		try {
			logger.info("\n==================\tTESTSUITE END\t===================");
			ap.addPermission(AllPermission.class.getName(), null, null);

			pMan.installPerms(ap);
			printGrantedPerms(ap);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			logger.info("\n==================\tTEST START\t===================");
			testWebAccess();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private void testWebAccess() throws Exception {
		logger.info("SecurityTest-WebAccess: Start...");
		String alias;
		alias = web.registerWebResource("/alias1", "/web");
		logger.info(String.format("First web resource alias1 registered with name %s", alias));
		alias = web.registerWebResource("/alias1", "/images");
		logger.info(String.format("Second web resource alias1 registered with name %s", alias));
	}

	public void testAppID() throws Exception {
		logger.info("SecurityTest-AppID: Start...");
		AppPermission ap = null;
		// Get AppPermission for the test App ogema-admin
		ap = getOwnAppPermission();
		if (ap == null)
			throw new Exception("AppPermission of the target App is null");
		// Get permissions granted to ogema-admin
		printGrantedPerms(ap);
		try {
			acc.login("mns");
		} catch (Throwable e) {
			System.err.println("Unexpected Exception.");
			e.printStackTrace();
		}
		logger.info("SecurityTest-AppID: ...end");
	}

	public void testAddPermission() throws Exception {
		String testName = "SecurityTest-addPermission";
		logger.info(testName + ": Start...");
		AppPermission ap = null;
		Object alwaysNull = null;
		// Get AppPermission for the test App ogema-admin
		ap = getOwnAppPermission();
		printGrantedPerms(ap);
		try {
			alwaysNull = acc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("SecurityException is expected.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}

		try {
			alwaysNull = rAcc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("SecurityException is always expected in restricted app.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}

		testAppPermissionType = ap.addPermission(AppPermissionType.ResourceAction.READ, "type=*,path=*");
		pMan.installPerms(ap);
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		try {
			alwaysNull = rAcc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("SecurityException is always expected in restricted app.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}

		try {
			acc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("!FAILED! SecurityException shouldn't be thrown now. Test failed!");
			throw se;
		}
		logger.info(testName + ": ...end");
	}

	AppPermissionType testAppPermissionType;

	public void testRemovePermission() throws Exception {
		Object alwaysNull = null;
		AppPermission ap = null;
		String testName = "SecurityTest-removePermission";
		logger.info(testName + ": Start...");

		/*
		 * After testAddPermission the ResourcePermission with the filter "type=*,path=*" is granted. This test removes
		 * it and checks the effect.
		 */
		try {
			acc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("!FAILED! SecurityException shouldn't be thrown now. Test failed!");
			throw se;
		}

		// remove Permission
		ap = getOwnAppPermission();
		ap.removePermission(testAppPermissionType.getName());
		pMan.installPerms(ap);

		try {
			alwaysNull = acc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("SecurityException is expected.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}
		logger.info(testName + ": ...end");
	}

	public void testAddException() throws Exception {
		String testName = "SecurityTest-addException";
		logger.info(testName + ": Start...");
		AppPermission ap = null;
		Object alwaysNull = null;
		/*
		 * To test the exceptions first we add AllPermission and than restrict the access by adding an exception.
		 */
		// Get AppPermission for the test App ogema-admin
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		logger.info("Remove all policies");
		ap.removeAllPolicies();

		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		// Expected not to be permitted
		// acc is permitted but this app not
		try {
			alwaysNull = acc.getResourcesOfType("org.ogema.core.model.array.BooleanArrayResource");
		} catch (SecurityException se) {
			logger.info("SecurityException is expected before adding of permissions.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}

		logger.info("Add AllPermission");
		ap.addPermission(AllPermission.class.getName(), null, null);

		pMan.installPerms(ap);
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		// Expected to be permitted
		// acc is permitted and this app has now the AllPermission and is allowed too
		try {
			acc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("!FAILED! SecurityException shouldn't be thrown now. Test failed!");
			throw se;
		}

		logger.info("Now add the Exception");
		// Now add the Exception
		testAppPermissionType = ap.addException(AppPermissionType.ResourceAction.READ, "type=*,path=*");
		pMan.installPerms(ap);
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		// Expected to be not permitted
		// RestrictedAccess is not allowed and this shouldn't be allowed too because of the added exception.
		try {
			alwaysNull = rAcc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("SecurityException is always expected in restricted app.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}

		logger.info(testName + ": ...end");
	}

	public void testRemoveException() throws Exception {
		Object alwaysNull = null;
		AppPermission ap = null;
		String testName = "SecurityTest-removeException";
		logger.info(testName + ": Start...");

		/*
		 * After testAddException the ResourcePermission with the filter "type=*,path=*" is not granted. This test
		 * removes the exception and checks the effect.
		 */
		// acc is allowed but this app is not allowed because the exception.
		try {
			alwaysNull = rAcc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("SecurityException is always expected in restricted app.");
		} finally {
			if (alwaysNull != null) {
				alwaysNull = null;
				throw new Exception("!FAILED! Expected SecurityException didn't occur.");
			}
		}

		// remove Exception
		logger.info("Before removing of exception!");
		ap = getOwnAppPermission();
		printGrantedPerms(ap);
		ap.removeException(testAppPermissionType.getName());
		pMan.installPerms(ap);
		logger.info("After removing of exception!");
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		// The exception is removed, so the access is permitted again.
		// acc is permitted and this app is it too after removing of the exception
		try {
			acc.getResource("type=*,path=*");
		} catch (SecurityException se) {
			logger.info("!FAILED! SecurityException shouldn't be thrown now. Test failed!");
			throw se;
		}

		logger.info(testName + ": ...end");
	}

	public void testGetExceptions() throws Exception {
		String testName = "AppPermissionTest-getExceptions";
		logger.info(testName + ": Start...");

		AppPermission ap = null;
		Object alwaysNull = null;
		/*
		 * To test the exceptions first we add AllPermission and than restrict the access by adding an exception.
		 */
		// Get AppPermission for the test App ogema-admin
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		logger.info("Now add some Exceptions");
		// Now add the Exception
		AppPermissionTypeImpl[] types = new AppPermissionTypeImpl[6];
		types[0] = (AppPermissionTypeImpl) ap.addException(AppPermissionType.ResourceAction.READ,
				"type=org.ogema.model.hvac.HeatPump");
		String[] args = { "/very/critical/path", "read,write" };
		types[1] = (AppPermissionTypeImpl) ap.addException("java.io.FilePermission", args, null);
		types[2] = (AppPermissionTypeImpl) ap.addException(AppPermissionType.ResourceAction.CREATE,
				"type=org.ogema.Resource,name=anyName,count=122,recursive=true", null);
		pMan.installPerms(ap);

		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		List<AppPermissionType> types2 = ap.getExceptions();
		for (AppPermissionType type : types2) {
			if (type.getName().equals(types[0].getName()))
				types2.remove(types[0]);
			if (type.getName().equals(types[1].getName()))
				types2.remove(types[1]);
			if (type.getName().equals(types[2].getName()))
				types2.remove(types[2]);
		}
		if (types2.size() > 0)
			throw new RuntimeException("Not all expected Exceptions are found.");
		else
			logger.info("Test passed.");
		logger.info(testName + ": ...end");
	}

	public void testGetTypes() throws Exception {
		String testName = "AppPermission Test - getTypes";
		logger.info(testName + ": Start...");
		AppPermission ap = null;

		// Get AppPermission for the test App ogema-admin
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		logger.info("Remove all policies");
		ap.removeAllPolicies();

		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		logger.info("Now add some Exceptions");
		// Now add the Exception
		AppPermissionTypeImpl[] exceptions = new AppPermissionTypeImpl[6];
		exceptions[0] = (AppPermissionTypeImpl) ap.addException(AppPermissionType.ResourceAction.READ,
				"type=org.ogema.model.hvac.HeatPump");
		String[] args = { "/very/critical/path", "read,write" };
		exceptions[1] = (AppPermissionTypeImpl) ap.addException("java.io.FilePermission", args, null);
		exceptions[2] = (AppPermissionTypeImpl) ap.addException(AppPermissionType.ResourceAction.CREATE,
				"type=org.ogema.Resource,name=anyName,count=122,recursive=true", null);

		logger.info("Now add some Permissions");
		// Now add the Exception
		AppPermissionTypeImpl[] perms = new AppPermissionTypeImpl[6];
		perms[0] = (AppPermissionTypeImpl) ap.addPermission(AppPermissionType.ResourceAction.READ,
				"type=org.ogema.model.hvac.HeatPump");
		perms[1] = (AppPermissionTypeImpl) ap.addPermission("java.io.FilePermission", args, null);
		perms[2] = (AppPermissionTypeImpl) ap.addPermission(AppPermissionType.ResourceAction.CREATE,
				"type=org.ogema.Resource,name=anyName,count=122,recursive=true", null);

		pMan.installPerms(ap);

		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		List<AppPermissionType> types = ap.getTypes();
		for (AppPermissionType type : types) {
			if (type.getName().equals(perms[0].getName()))
				types.remove(perms[0]);
			if (type.getName().equals(perms[1].getName()))
				types.remove(perms[1]);
			if (type.getName().equals(perms[2].getName()))
				types.remove(perms[2]);
			if (type.getName().equals(exceptions[0].getName()))
				types.remove(exceptions[0]);
			if (type.getName().equals(exceptions[1].getName()))
				types.remove(exceptions[1]);
			if (type.getName().equals(exceptions[2].getName()))
				types.remove(exceptions[2]);
		}
		if (types.size() > 0)
			throw new RuntimeException("Not all expected Exceptions are found.");
		else
			logger.info("Test passed.");

		logger.info(testName + ": ...end");
	}

	public void testInstallDefaultPlocies() throws Exception {
		String testName = "AppPermission Test - installDefaultPolicies";
		logger.info(testName + ": Start...");
		AppPermission ap = null;

		// Get AppPermission for the test App ogema-admin
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		logger.info("Remove all policies");
		ap.removeAllPolicies();

		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		logger.info("Now install default Permissions");
		// Now add the Exception
		Map<String, ConditionalPermissionInfo> defaults = pMan.getDefaultPolicies().getGrantedPerms();

		pMan.setDefaultPolicies();
		pMan.installPerms(ap);
		ap = getOwnAppPermission();
		printGrantedPerms(ap);

		// List<AppPermissionType> types = ap.getTypes();
		// for (AppPermissionType type : types) {
		// boolean success = false;
		// for (AppPermissionType aptimpl : types) {
		// for (AppPermissionType dpi : defaults) {
		// if (dpi.getDeclarationString().equals(aptimpl.getDeclarationString())) {
		// success = true;
		// break;
		// }
		// }
		// if (success)
		// break;
		// else
		// throw new RuntimeException("Not all expected default permissions are found.");
		// }
		// }
		logger.info("Test passed.");
		logger.info(testName + ": ...end");
	}

	public void testTemplate() throws Exception {
		String testName = "";
		logger.info(testName + ": Start...");

		logger.info(testName + ": ...end");
	}

	@Override
	public void start(ApplicationManager appManager) {
		web = appManager.getWebAccessManager();

		/*
		 * Get PermissionManager to delegate the permission checks
		 */
		pMan = (PermissionManager) bc.getService(bc.getServiceReference(PermissionManager.class.getName()));
		/*
		 * Log service
		 */
		ServiceReference<?> sRef = bc.getServiceReference(LogService.class.getName());
		log = (LogService) bc.getService(sRef);

		admin = (AdministrationManager) bc.getService(bc.getServiceReference(AdministrationManager.class.getName()));
		acc = (Access) bc.getService(bc.getServiceReference(Access.class.getName()));
		rAcc = (RestrictedAccess) bc.getService(bc.getServiceReference(RestrictedAccess.class.getName()));
		/*
		 * Wait until the app is registered in the app manager. Some tries are made
		 */
		int tries = 100;
		try {
			AppPermission ap = null;
			while (tries-- >= 0) {
				ap = getOwnAppPermission();
				if (ap == null) {
					if (tries == 0) {
						logger.info("Something goes wrong with the activation of the app.");
						break;
					}
					Thread.sleep(50);
				}
				else
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * For test web access via guest user
		 */
		AccessManager acc = pMan.getAccessManager();

		AppPermissionFilter filter = new AppPermissionFilter(testAppID.getBundle().getSymbolicName(), testAppID
				.getOwnerGroup(), testAppID.getOwnerUser(), testAppID.getVersion());
		acc.addPermission("guest", filter);
		List<String> allusers = acc.getAllUsers();
		System.out.print("Allusers: ");
		for (String str : allusers) {
			System.out.print(str);
			System.out.print(",\t");
		}
		System.out.println();

		List<AppID> apps = acc.getAppsPermitted("guest");
		System.out.print("guest is permitted for apps: ");
		for (AppID id : apps) {
			System.out.print(id.getIDString());
			System.out.print(",\t");
		}
		System.out.println();

		// Start tests
		testSuit();

	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub

	}

	/*
	 * quod erat demonstrandum
	 */
}
