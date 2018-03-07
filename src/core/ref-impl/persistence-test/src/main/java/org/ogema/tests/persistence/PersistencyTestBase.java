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
package org.ogema.tests.persistence;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.ogema.applicationregistry.ApplicationListener;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.persistence.DBConstants;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;

/**
 * Before execution of the tests, this class registers a new test app with all import permissions and a service
 * permission to register an Application. The corresponding {@link ApplicationManager} is available via
 * {@link #getApplicationManager()}. Another application manager with unrestricted permissions is available via
 * {@link #getUnrestrictedAppManager()}. <br>
 * Further permissions can be created using the methods in {@link SecurityTestUtils}.
 */
public class PersistencyTestBase extends OsgiAppTestBase {

	private ApplicationManager appMan;

	private final static AtomicInteger testAppCnt = new AtomicInteger(0);

	public PersistencyTestBase() {
		this(false);
	}

	public PersistencyTestBase(boolean includeTestBundle) {
		super(includeTestBundle);
	}

	@Before
	public void installAppAndGetAppManager() throws InvalidSyntaxException, BundleException, InterruptedException {
		appMan = installAppAndGetAppManager(ctx, 5, TimeUnit.SECONDS);
	}

	@After
	public void uninstallApp() throws BundleException {
		uninstallApp(appMan);
		appMan = null;
	}

	/**
	 * Get an app manager for the test app with restricted permissions set
	 */
	@Override
	public ApplicationManager getApplicationManager() {
		return appMan;
	}

	/**
	 * Get an app manager with all permissions
	 * 
	 * @return
	 */
	public ApplicationManager getUnrestrictedAppManager() {
		return super.getApplicationManager();
	}

	protected Integer storagePeriod = 1000000;

	@Configuration
	@Override
	public Option[] config() {
		Option[] sup = super.config();
		Option[] newOpt = new Option[sup.length + 10];
		System.arraycopy(sup, 0, newOpt, 0, sup.length);
		newOpt[sup.length + 0] = CoreOptions.bundle("file:lib/tinybundles-3.0.0-SNAPSHOT.jar");
		newOpt[sup.length + 1] = CoreOptions.mavenBundle("biz.aQute.bnd", "biz.aQute.bndlib", "3.4.0");
		newOpt[sup.length + 2] = CoreOptions.mavenBundle("org.ogema.tests", "persistence-tests", ogemaVersion);
		newOpt[sup.length + 3] = CoreOptions.mavenBundle("commons-logging", "commons-logging", "1.1.3");
		newOpt[sup.length + 4] = CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_ACTIVE)
				.value(DBConstants.PROP_VALUE_PERSISTENCE_ACTIVE);
		newOpt[sup.length + 5] = CoreOptions.systemProperty(DBConstants.DB_PATH_PROP).value("data/persistence");
		newOpt[sup.length + 6] = CoreOptions.systemProperty(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD)
				.value(storagePeriod.toString());
		newOpt[sup.length + 7] = CoreOptions
				.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE_GARBAGE).value("75");
		newOpt[sup.length + 8] = CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_COMPACTION_START_SIZE)
				.value("1024");
		newOpt[sup.length + 9] = CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_DEBUG).value("true");
		return newOpt;
	}

	/**
	 * Installs a test app, sets all import permissions for it, starts it, and returns the corresponding application
	 * manager.
	 * 
	 * @param ctx
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InvalidSyntaxException
	 * @throws BundleException
	 * @throws InterruptedException
	 */
	public final static ApplicationManager installAppAndGetAppManager(final BundleContext ctx, final long timeout,
			final TimeUnit unit) throws InvalidSyntaxException, BundleException, InterruptedException {
		final ApplicationRegistry registry = getService(ctx, ApplicationRegistry.class);
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Application> appRef = new AtomicReference<Application>(null);
		final ApplicationListener listener = new ApplicationListener() {

			@Override
			public void appRemoved(AppID app) {
			}

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
		// addImportPermissions(b, "*", cpa, cpu);
		// addServicePermission(b, Application.class, false, cpa, cpu);
		cpu.commit();
		b.start();
		Assert.assertEquals("Test app bundle did not start", Bundle.ACTIVE, b.getState());
		Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
		registry.unregisterAppListener(listener);
		final Application app = appRef.get();
		Assert.assertNotNull(app);
		final ApplicationManager newAppMan = TestApp.getAppManager(app, timeout, unit);
		Assert.assertNotNull("Application manager is null", newAppMan);
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
	 * Creates a new test app in a bundle, and installs it. No permissions are set for the bundle.
	 * 
	 * @param ctx
	 * @param locationPrefix
	 * @return
	 * @throws BundleException
	 */
	public static Bundle installTestAppAsBundle(final BundleContext ctx, final String locationPrefix)
			throws BundleException {
		final String id = "ogema.test.app" + testAppCnt.getAndIncrement();
		final TinyBundle tb = TinyBundles.bundle().add(TestApp.class);
		// tb.add(TestApp.WEBRESOURCE_PATH.substring(1) + "/index.html", TestWebresource.getWebResource());
		final InputStream in = tb.set(Constants.BUNDLE_SYMBOLICNAME, id)
				.set(Constants.BUNDLE_ACTIVATOR, TestApp.class.getName())
				.set(Constants.EXPORT_PACKAGE, "!" + TestApp.class.getPackage().getName()) // required?
				.set(Constants.IMPORT_PACKAGE,
						Bundle.class.getPackage().getName() + ";" + Application.class.getPackage().getName())
				.build();
		final Bundle bundle = ctx.installBundle(locationPrefix + ":" + id, in);
		return bundle;
	}

}
