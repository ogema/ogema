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
package org.ogema.tests.security.testbase;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.AllPermission;
import java.security.Permission;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.accesscontrol.WebAccessPermission;
import org.ogema.applicationregistry.ApplicationListener;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.tests.security.testbase.app.impl.TestApp;
import org.ogema.tests.security.testbase.servlet.TestWebresource;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.PackagePermission;
import org.osgi.framework.ServicePermission;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.BundleLocationCondition;
import org.osgi.service.condpermadmin.Condition;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;
import org.osgi.service.permissionadmin.PermissionInfo;

/**
 * Utility methods for permissions creation, app/bundle creation, user creation, etc.
 */
public class SecurityTestUtils {
	
	private final static AtomicInteger testAppCnt = new AtomicInteger(0);
	private final static AtomicInteger permissionCnt = new AtomicInteger(0);
	
	/**
	 * Creates a new test app in a bundle, and installs it. No permissions are set for the bundle.
	 * @param ctx
	 * @param locationPrefix
	 * @return
	 * @throws BundleException
	 */
	public static Bundle installTestAppAsBundle(final BundleContext ctx, final String locationPrefix) throws BundleException {
		final String id = "ogema.test.app" + testAppCnt.getAndIncrement();
		final TinyBundle tb = TinyBundles.bundle().add(TestApp.class);
			tb.add(TestApp.WEBRESOURCE_PATH.substring(1) + "/index.html", TestWebresource.getWebResource());
		final InputStream in = tb
			.set(Constants.BUNDLE_SYMBOLICNAME, id)
			.set(Constants.BUNDLE_ACTIVATOR, TestApp.class.getName())
			.set(Constants.EXPORT_PACKAGE, "!" +TestApp.class.getPackage().getName()) // required?
			.set(Constants.IMPORT_PACKAGE, 
					Bundle.class.getPackage().getName() + ";" +
					Application.class.getPackage().getName()) 
			.build();
		final Bundle bundle = ctx.installBundle(locationPrefix + ":" + id, in);
		return bundle;
	}
	
	/**
	 * Installs a test app, sets all import permissions for it, starts it, and returns 
	 * the corresponding application manager.
	 * @param ctx
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InvalidSyntaxException
	 * @throws BundleException
	 * @throws InterruptedException
	 */
	public final static ApplicationManager installAppAndGetAppManager(final BundleContext ctx, final long timeout, final TimeUnit unit) 
			throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationRegistry registry = getService(ctx, ApplicationRegistry.class);
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Application> appRef = new AtomicReference<Application>(null);
		final ApplicationListener listener = new ApplicationListener() {
			
			@Override
			public void appRemoved(AppID app) {}
			
			@Override
			public void appInstalled(AppID app) {
				if (!app.getApplication().getClass().getName().equals(TestApp.class.getName()))
					return;
				appRef.set(app.getApplication());
				latch.countDown();
			}
		};
		registry.registerAppListener(listener);
		final Bundle b = installTestAppAsBundle(ctx, "testlocation");
		final ConditionalPermissionAdmin cpa = getService(ctx, ConditionalPermissionAdmin.class);
		final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		addImportPermissions(b, "*", cpa, cpu);
		addServicePermission(b, Application.class, false, cpa, cpu);
		cpu.commit();
		b.start();
		Assert.assertEquals("Test app bundle did not start", Bundle.ACTIVE, b.getState());
		Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
		registry.unregisterAppListener(listener);
		final Application app = appRef.get();
		Assert.assertNotNull(app);
		final ApplicationManager newAppMan = TestApp.getAppManager(app, timeout, unit);
		Assert.assertNotNull("Application manager is null",newAppMan);
		return newAppMan;
	}
	
	public final static void uninstallApp(final ApplicationManager appMan) throws BundleException {
		appMan.getAppID().getBundle().uninstall();
	}

	public static <S> S getService(final BundleContext ctx, final Class<S> clazz) {
		final ServiceReference<S> ref = ctx.getServiceReference(clazz);
		return ref == null ? null : ctx.getService(ref);
	}
	
	/**
	 * Adds a resource permission for the bundle belonging to the passed application manager.
	 * @param ctx
	 * @param path
	 * @param resourceType
	 * @param appMan
	 * @param actions
	 * 		not null; wildcard "*" for all actions
	 */
	public final static void addResourcePermission(final BundleContext ctx, final String path, final String resourceType, final ApplicationManager appMan,
			final String actions) {
		final ConditionalPermissionAdmin cpa = getService(ctx, ConditionalPermissionAdmin.class);
		final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		addResourcePermission(cpa, cpu, path, resourceType, appMan.getAppID().getBundle(), actions, true, -1);
		cpu.commit();
	}
	
	/**
	 * Denies a resource permission for the bundle belonging to the passed application manager.
	 * @param ctx
	 * @param path
	 * @param resourceType
	 * @param appMan
	 * @param actions
	 * 		not null; wildcard "*" for all actions
	 */
	public final static void denyResourcePermission(final BundleContext ctx, final String path, final String resourceType, final ApplicationManager appMan,
			final String actions) {
		final ConditionalPermissionAdmin cpa = getService(ctx, ConditionalPermissionAdmin.class);
		final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		addResourcePermission(cpa, cpu, path, resourceType, appMan.getAppID().getBundle(), actions, false, 0);
		cpu.commit();
	}
    
    public static void printBundlePermissions(Bundle b, PrintStream out) {
        final ConditionalPermissionAdmin cpa = getService(b.getBundleContext(), ConditionalPermissionAdmin.class);
        final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        for (ConditionalPermissionInfo cpi: cpu.getConditionalPermissionInfos()) {
            ConditionInfo[] cis = cpi.getConditionInfos();
            for (ConditionInfo ci: cis) {
                if (ci.getType().equals("org.osgi.service.condpermadmin.BundleLocationCondition")) {
                    Condition blc = BundleLocationCondition.getCondition(b, ci);
                    if (blc.isSatisfied()) {
                        out.println(cpi);
                    }
                }
            }
            if (cis.length == 0) {
                out.println(cpi);
            }
        }
    }
	
	/**
	 * @param cpAdmin
	 * @param update
	 * @param path
	 * @param resourceType
	 * @param bundle
	 * @param actions
	 * 		not null; wildcard "*" for all actions
	 * @param allowOrDeny
	 * @param index
	 */
	public final static void addResourcePermission(final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update, 
			final String path, final String resourceType, final Bundle bundle, final String actions, final boolean allowOrDeny, int index) {
		Assert.assertFalse("Path and type conditions must not both be null",path == null && resourceType == null);
		Objects.requireNonNull(actions);
		final StringBuilder sb = new StringBuilder();
		if (resourceType != null) 
			sb.append("type=").append(resourceType);
		if (path != null) {
			if (resourceType != null)
				sb.append(',');
			sb.append("path=").append(path);
		}
		addPermission(bundle, ResourcePermission.class, sb.toString(), actions, cpAdmin, update, allowOrDeny, index);
	}
	
	public final static void addServicePermission(final Bundle bundle, final Class<?> service, boolean getOrRegister,
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update) {
		addServicePermission(bundle, service.getName(), getOrRegister, cpAdmin, update);
	}
	
	public final static void addServicePermission(final Bundle bundle, final String service, boolean getOrRegister,
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update) {
		addPermission(bundle, ServicePermission.class, service, getOrRegister ? ServicePermission.GET : ServicePermission.REGISTER, cpAdmin, update, true, -1);
	}
	
	/**
	 * Note: it is required to call update.commit() before the added permission is applied.
	 * @param bundle
	 * @param packageImport
	 * 		Wildcard allowed at the end, e.g. "org.osgi.*", or "*" for all packages
	 * @param cpAdmin
	 * @param update
	 */
	public static void addImportPermissions(final Bundle bundle, final String packageImport, 
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update) {
		addImportPermissions(bundle, packageImport, cpAdmin, update, true);
	}
	
	/**
	 * Note: it is required to call update.commit() before the added permission is applied.
	 * @param bundle
	 * @param packageImport
	 * 		Wildcard allowed at the end, e.g. "org.osgi.*", or "*" for all packages
	 * @param cpAdmin
	 * @param update
	 */
	public static void denyImportPermissions(final Bundle bundle, final String packageImport, 
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update) {
		addImportPermissions(bundle, packageImport, cpAdmin, update, false);
	}
	
	public static void addImportPermissions(final Bundle bundle, final String packageImport, 
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update, final boolean allowOrDeny) {
		addPermission(bundle, PackagePermission.class, packageImport, PackagePermission.IMPORT, cpAdmin, update, allowOrDeny, -1);
	}
	
	/**
	 * Note: it is required to call update.commit() before the added permission is applied.
	 * @param bundle
	 * @param cpAdmin
	 * @param update
	 */
	public static void addAllPermissions(final Bundle bundle, final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update) {
		addPermission(bundle, AllPermission.class, null, null, cpAdmin, update, true, -1);
	}

	public static void addAllPermissions(final Bundle bundle, final BundleContext ctx) {
		addPermission(bundle, AllPermission.class, null, null, ctx, true);
	}
	
	/**
	 * @param user
	 * @param app
	 * @param ctx
	 * @throws InterruptedException 
	 * @throws IllegalStateException
	 */
	public static void addWebResourcePermission(final String user, final String app, final BundleContext ctx) throws InterruptedException {
		final Bundle urp = waitForUser(user, ctx);
		if (urp == null)
			throw new IllegalStateException("User bundle for " + user + " not found");
		addPermission(urp, WebAccessPermission.class, "name=" + (app == null ? "*" : app), null, ctx, true);
	}
	
	public static void addWebResourcePermission(final ApplicationManager appMan, final String app, final BundleContext ctx) { 
		addPermission(appMan.getAppID().getBundle(), WebAccessPermission.class, "name=" + (app == null ? "*" : app), null, ctx, true);
	}
	
	public static void addWebResourcePermission(final Bundle bundle, final String app, 
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update, final boolean allowOrDeny) {
		addPermission(bundle, WebAccessPermission.class, "name=" + (app == null ? "*" : app), null, cpAdmin, update, allowOrDeny, -1);
	}
	
	/**
	 * Add permission and commit the permissions update
	 * @param bundle
	 * @param type
	 * @param name
	 * 		may be null
	 * @param actions
	 * 		may be null
	 * @param ctx
	 * @param allowOrDeny
	 * @return
	 * 		whether or not the permission update was successful. See {@link ConditionalPermissionUpdate#commit()}.
	 */
	public static boolean addPermission(final Bundle bundle, final Class<? extends Permission> type, final String name, final String actions, 
			final BundleContext ctx, final boolean allowOrDeny) {
		final ConditionalPermissionAdmin cpa = getService(ctx, ConditionalPermissionAdmin.class);
		final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		addPermission(bundle, type, name, actions, cpa, cpu, allowOrDeny, -1);
		return cpu.commit();
	}
	
	/**
	 * Add permission but do not commit the permissions update yet
	 * @param bundle
	 * @param type
	 * @param name
	 * 		may be null
	 * @param actions
	 * 		may be null
	 * @param cpAdmin
	 * @param update
	 * @param allowOrDeny
     * @param index position at which to insert new permission, use -1 to append.
	 */
	public static void addPermission(final Bundle bundle, final Class<? extends Permission> type, final String name, final String actions, 
			final ConditionalPermissionAdmin cpAdmin, final ConditionalPermissionUpdate update, final boolean allowOrDeny, int index) {
        List<ConditionalPermissionInfo> permissions = update.getConditionalPermissionInfos();
        if (index == -1) {
            index = permissions.size();
        }
		permissions.add(index,
				cpAdmin.newConditionalPermissionInfo(
						"testCond" + permissionCnt.getAndIncrement(), 
						new ConditionInfo[] {
			 					new ConditionInfo("org.osgi.service.condpermadmin.BundleLocationCondition", new String[]{bundle.getLocation()}) }, 
						new PermissionInfo[] {
							 new PermissionInfo(type.getName(), name, actions)}, 
						allowOrDeny ? "allow" : "deny"));
	}
	
	public static UserAccount createPrivilegedNaturalUser(final String user, final ApplicationManager appMan) throws InterruptedException {
		return createUser(user, appMan, true, true);
	}

	public static UserAccount createUser(final String user, final ApplicationManager appMan, 
			final boolean naturalOrMachineUser, final boolean privileged) throws InterruptedException {
		final BundleContext ctx = appMan.getAppID().getBundle().getBundleContext();
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Bundle> bundle = new AtomicReference<Bundle>(null);
		// FIXME relies on implementation details
		final BundleListener listener = new BundleListener() {
			
			@Override
			public void bundleChanged(BundleEvent event) {
				if (event.getType() == BundleEvent.INSTALLED) {
					final Bundle b = event.getBundle();
					if (("urp:" + user).equals(b.getLocation())) {
						bundle.set(b);
						latch.countDown();
					}
				}
			}
		};
		ctx.addBundleListener(listener);
		final UserAccount userAcc = appMan.getAdministrationManager().createUserAccount(user, naturalOrMachineUser);
		final Bundle target;
		if (!latch.await(3, TimeUnit.SECONDS))
			target = ctx.getBundle("urp:" + user);
		else
			target = bundle.get();
		ctx.removeBundleListener(listener);
		Assert.assertNotNull("User bundle not registered",target);
		if (privileged)
			addAllPermissions(target, ctx);
		return userAcc;
	}
	
	public static boolean addResourcePermissions(final String user, final BundleContext ctx, final String path,
			final String resourceType, String actions) {
		final Bundle b = ctx.getBundle("urp:" + user);
		if (b == null)
			return false;
		final ConditionalPermissionAdmin cpa = getService(ctx, ConditionalPermissionAdmin.class);
		final ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
		addResourcePermission(cpa, cpu, path, resourceType, b, actions, true, -1);
		cpu.commit();
		return true;
	}
	
	private static Bundle waitForUser(final String user, final BundleContext ctx) throws InterruptedException {
		Bundle b = ctx.getBundle("urp:" + user);
		if (b != null)
			return b;
		final AtomicReference<Bundle> bundle = new AtomicReference<Bundle>(null);
		final CountDownLatch latch = new CountDownLatch(1);
		final BundleListener listener = new BundleListener() {
			
			@Override
			public void bundleChanged(BundleEvent event) {
				if (event.getType() == BundleEvent.INSTALLED) {
					final Bundle b = event.getBundle();
					if (("urp:" + user).equals(b.getLocation())) {
						bundle.set(b);
						latch.countDown();
					}
				}
			}
		};
		ctx.addBundleListener(listener);
		try {
			b = ctx.getBundle("urp:" + user);
			if (b != null)
				return b;
			latch.await(3, TimeUnit.SECONDS);
			return ctx.getBundle("urp:" + user);
		} finally {
			ctx.removeBundleListener(listener);
		}
	}
	
}
