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
package org.ogema.resourcemanager.addon.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.persistence.DBConstants;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

/**
 * Starts an external test app which exports a custom resource type, of which it also creates an instance. Then tests
 * the framework behaviour when the app is removed from the framework.
 *
 * TODO tests for resource list with custom element type TODO tests for custom subresources TODO tests for handling of
 * references
 * 
 * @author cnoelle
 */
// here we must restart the container for every test, since the test app is uninstalled in some
// of the tests, and the test resources appear and disappear
@ExamReactorStrategy(PerMethod.class)
public class ResourceTypeExportTest extends OsgiAppTestBase {

	/**
	 * This is the resource path for the top level custom type resource created by the test app. Do not change.
	 */
	private final static String RESOURCE_NAME_TOPLEVEL = "resTypeExpoTest1";
	/**
	 * This is the resource path for a top level resource list with custom element type created by the test app. Do not
	 * change.
	 */
	private final static String RESOURCE_NAME_LIST_TOPLEVEL = "resTypeExpoTest2";
	/**
	 * This is a resourc path for a standard resource, which has two decorators: a custom type resource, and a resource
	 * list with custom element type
	 */
	private final static String RESOURCE_NAME_SUB = "resTypeExpoTest3";
	private final static String TESTBUNDLE_SYMBOLIC_NAME = "org.ogema.tests.resourcetype-export-test";
    
    @AfterClass
    public static void removeOldResourceData() {
        Path resdbpath = Paths.get("data", "persistence");
        if (Files.exists(resdbpath)) {
            try {
                for (Path f: Files.newDirectoryStream(resdbpath, "res*")) {
                    System.out.println("deleting resource file " + f);
                    Files.delete(f);
                }
            } catch (IOException ex) {
                Logger.getLogger(ResourceTypeExportTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

	@Override
	public Option[] frameworkBundles() {
		Option[] opt = super.frameworkBundles();
		Option[] options = new Option[opt.length + 3];
		System.arraycopy(opt, 0, options, 0, opt.length);
		// the test app
		options[opt.length] = CoreOptions.mavenBundle("org.ogema.tests", "resourcetype-export-test", ogemaVersion);
		options[opt.length + 1] = CoreOptions.systemProperty(DBConstants.PROP_NAME_PERSISTENCE_ACTIVE)
				.value(DBConstants.PROP_VALUE_PERSISTENCE_ACTIVE);
		options[opt.length + 2] = CoreOptions.systemProperty(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD).value("500");
		return options;
	}

	private AdminApplication getTestApp() {
		for (AdminApplication adminApp : getApplicationManager().getAdministrationManager().getAllApps()) {
			if (TESTBUNDLE_SYMBOLIC_NAME.equals(adminApp.getBundleRef().getSymbolicName()))
				return adminApp;
		}
		return null;
	}

	private Bundle getTestBundle() {
		for (Bundle b : ctx.getBundles()) {
			if (TESTBUNDLE_SYMBOLIC_NAME.equals(b.getSymbolicName()))
				return b;
		}
		return null;
	}

	private AdminApplication ensureTestAppStarted(long timeoutMillis) throws InterruptedException {
		AdminApplication admin = null;
		final long t0 = System.currentTimeMillis();
		while (true) {
			admin = getTestApp();
			if (admin != null)
				break;
			if (System.currentTimeMillis() - t0 > timeoutMillis)
				throw new AssertionError("Test app not started");
			Thread.sleep(200);
		}
		// we run the start method again with our own application manager, to be sure it has completed successfully
		admin.getID().getApplication().start(getApplicationManager());
		return admin;
	}
	
	private void waitForPersistence() throws InterruptedException {
		final Long period = Long.getLong(DBConstants.PROP_NAME_TIMEDPERSISTENCE_PERIOD);
		Assert.assertNotNull("Persistence period not set", period);
		Thread.sleep(3 * period + 1500); // here: 3s -> be generous to avoid timing issues on CI server
	}

	private FrameworkWiring getFrameworkWiring() {
		return ctx.getBundle(0).adapt(FrameworkWiring.class);
	}

	private Resource getTestResource() {
		return getApplicationManager().getResourceAccess().getResource(RESOURCE_NAME_TOPLEVEL);
	}

	private AdminApplication startTestApp() throws InterruptedException {
		final AdminApplication adminApp = ensureTestAppStarted(30000);
		final Resource r = getTestResource();
		Assert.assertNotNull("Test resource is null", r);
		ResourceAssertions.assertActive(r);
		return adminApp;
	}

	private void uninstallTestApp(AdminApplication adminApp) throws BundleException, InterruptedException {
		adminApp.getBundleRef().uninstall();
		final FrameworkWiring f = getFrameworkWiring();
		final Collection<Bundle> outdatedBundles = f.getDependencyClosure(f.getRemovalPendingBundles());
		// the if condition should always be satisfied, but like this we avoid relying on the implementation detail that the
		// framework imports the custom type package
		if (!outdatedBundles.isEmpty()) {
			final CountDownLatch refreshLatch = new CountDownLatch(1);
			f.refreshBundles(outdatedBundles, new FrameworkListener() {
	
				@Override
				public void frameworkEvent(FrameworkEvent event) {
					if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
						refreshLatch.countDown();
	
				}
			});
			Assert.assertTrue("Missing package refresh callback after removing custom resource type",
					refreshLatch.await(30, TimeUnit.SECONDS));
		}
		// the package refresh might cause a restart of the OsgiAppTestBase as well, so we better verify that it is
		// active,
		// respectively wait for its next start call.
		waitForOsgiTestBaseApp();
	}

	private void waitForOsgiTestBaseApp() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			if (getApplicationManager() != null)
				return;
			Thread.sleep(100);
		}
		throw new AssertionError("Test app not restarted after package refresh");
	}

	/**
	 * This is the essential part of the tests: ensure that the test app is started and creates its resource of custom
	 * type, then uninstall it.
	 */
	private void startAndRemoveTestApp() throws InterruptedException, BundleException {
		final AdminApplication adminApp = startTestApp();
		waitForPersistence();
		uninstallTestApp(adminApp);
	}

	@Test
	public void resourceAccessWorksAfterCustomTyeRemoval() throws InterruptedException, BundleException {
		startAndRemoveTestApp();
		for (Resource res : getApplicationManager().getResourceAccess().getResources(Resource.class)) {
			Assert.assertNotNull("Test resource is null", res);
			ResourceAssertions.assertActive(res);
		}
	}

	@Test
	public void resourcePathAccessWorksAfterCustomTypeRemoval() throws InterruptedException, BundleException {
		startAndRemoveTestApp();
		final Resource r = getTestResource();
		Assert.assertNull("Resource of removed type found non-null", r);
	}

	@Test
	public void resourceCreationWorksAfterCustomTypeRemoval() throws InterruptedException, BundleException {
		startAndRemoveTestApp();
		Class<? extends Resource> newType = TemperatureSensor.class;
		// We recreate the same resource, but with an incompatible type.
		// This should probably lead to a complete removal of the (internally possibly still known) custom type resource
		final Resource r = getApplicationManager().getResourceManagement().createResource(RESOURCE_NAME_TOPLEVEL,
				newType);
		Assert.assertNotNull("Resource creation failed", r);
		ResourceAssertions.assertExists(r);

		Assert.assertEquals("Newly created resource has wrong type", newType, r.getResourceType());

		// now also create a subresource that overwrites one of the previous optional elements
		newType = StringResource.class;
		final Resource sub = r.addDecorator("temperature", newType);
		Assert.assertNotNull("Resource creation failed", sub);
		ResourceAssertions.assertExists(sub);
		Assert.assertEquals("Newly created resource has wrong type", newType, sub.getResourceType());
	}

//	@Ignore("not implemented yet")
	@Test
	public void customResourceReappearsAfterAppRestart() throws InterruptedException, BundleException {
		final AdminApplication adminApp = startTestApp();
		final float targetValue = 20;
		// actually an optional element
		getTestResource().getSubResource("temperature", TemperatureResource.class).setCelsius(20);
		final String location = adminApp.getBundleRef().getLocation();
		Assert.assertNotNull(location);
		waitForPersistence();
		uninstallTestApp(adminApp);
		Assert.assertNull(getTestResource());
		final Bundle bundle = ctx.installBundle(location);
		Assert.assertNotNull(bundle);
		final FrameworkWiring fw = getFrameworkWiring();
		Assert.assertTrue("Test app bundle could not be resolved", fw.resolveBundles(Collections.singleton(bundle)));
		final Resource r = getTestResource();
		Assert.assertNotNull("Custom resource has disappeared", r);
		ResourceAssertions.assertExists(r);
		final TemperatureResource temp = r.getSubResource("temperature");
		Assert.assertNotNull(temp);
		ResourceAssertions.assertExists(temp);
		Assert.assertEquals(TemperatureResource.class, temp.getResourceType()); // type declared in test app
		Assert.assertEquals("Resource value got lost in app restart", targetValue, temp.getCelsius(), 0.00001);
	}

}
