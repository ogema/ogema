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
package org.ogema.resourcemanager.tests;

import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.Application;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.resourcemanager.tests.app.impl.TestApp;
import org.ogema.resourcemanager.tests.custom.TypeTestResource;
import org.ogema.resourcemanager.tests.custom.TypeTestResource2;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

/**
 * This class contains a set of tests where resource type packages are refreshed,
 * causing an update of the persistence bundle, and hence a restart of the database. 
 * 
 * In the tests a bundle is created on the fly (hence the tinybundles dependency), which exports the 
 * interfaces {@link TypeTestResource} and {@link TypeTestResource2}. Another bundle imports these interfaces
 * and creates a resource of type {@link TypeTestResource} (see class {@link TestApp}).
 * 
 * @author cnoelle
 */
@ExamReactorStrategy(PerClass.class)
public class CustomTypeRefreshTest extends TestBase {
	
	private final static AtomicInteger testAppCnt = new AtomicInteger(0);
	
	@Before
	public void deleteResources() throws InterruptedException {
		boolean found = false;
		for (Resource r : getApplicationManager().getResourceAccess().getToplevelResources(Resource.class)) {
			found = true;
			r.delete();
		}
		if (found)
			Thread.sleep(2 * PERSISTENCE_PERIOD_MS);
		Assert.assertTrue(getApplicationManager().getResourceAccess().getResources(Resource.class).isEmpty());
	}
	
	private static Bundle installTestBundle(final BundleContext ctx, final InputStream in) throws BundleException {
		final String id = "ogema.typetest.app" + testAppCnt.getAndIncrement();
		final Bundle bundle = ctx.installBundle("testBundle:" + id, in);
		return bundle;
	}
	
	private static InputStream createTestAppBundle() {
		final String id = "ogema.typetest.app" + testAppCnt.getAndIncrement();
		final TinyBundle tb = TinyBundles.bundle().add(TestApp.class);
		return tb
			.set(Constants.BUNDLE_SYMBOLICNAME, id)
			.set(Constants.BUNDLE_ACTIVATOR, TestApp.class.getName())
			.set(Constants.EXPORT_PACKAGE, "!" + TestApp.class.getPackage().getName())
			.set(Constants.IMPORT_PACKAGE, 
					Bundle.class.getPackage().getName() + ";" +
					Application.class.getPackage().getName() + ";" + 
					ResourceManagement.class.getPackage().getName() + ";" +
					Resource.class.getPackage().getName() + ";" + 
					TypeTestResource.class.getPackage().getName()) 
			.build();
	}
	
	private static InputStream createTestTypeBundle() {
		final String id = "ogema.typetest.app" + testAppCnt.getAndIncrement();
		final TinyBundle tb = TinyBundles.bundle().add(TypeTestResource.class).add(TypeTestResource2.class);
		return tb
			.set(Constants.BUNDLE_SYMBOLICNAME, id)
			.set(Constants.EXPORT_PACKAGE, TypeTestResource.class.getPackage().getName())
			.set(Constants.IMPORT_PACKAGE, 
					Bundle.class.getPackage().getName() + ";" +
					Resource.class.getPackage().getName() + ";" + 
					FloatResource.class.getPackage().getName() + ";" + 
					TemperatureSensor.class.getPackage().getName()) 
			.build();
	}
	
	private final static Resource waitForTestResource(final ResourceAccess ra, final String path, final long timeout) throws InterruptedException {
		final Resource initial = ra.getResource(path);
		if (initial != null)
			return initial;
		final long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < timeout) {
			Thread.sleep(50);
			final Resource res = ra.getResource(path);
			if (res != null)
				return res;
		}
		return null;
	}
	
	private final static void refresh(final Bundle bundle, final BundleContext ctx) throws InterruptedException {
		final FrameworkWiring fw = ctx.getBundle(0).adapt(FrameworkWiring.class);
		final CountDownLatch latch = new CountDownLatch(1);
		fw.refreshBundles(fw.getDependencyClosure(Collections.singleton(bundle)), new FrameworkListener() {
			
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
					latch.countDown();
			}
		});
		Assert.assertTrue("Gave up waiting for package refresh",latch.await(1, TimeUnit.MINUTES));
	}
	
	@Test
	public void restartCustomTypeAppWorks() throws BundleException, InterruptedException {
		final Bundle bTypes = installTestBundle(ctx, createTestTypeBundle());
		Assert.assertNotNull(bTypes);
		bTypes.start();
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bTypes.getState());	
		final Bundle bApp = installTestBundle(ctx, createTestAppBundle());
		Assert.assertNotNull(bApp);
		// the app creates a resource of the custom type defined in bTypes
		bApp.start();
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bApp.getState());
		final String path = TestApp.RESOURCE_NAME;
		final Resource testResource = waitForTestResource(getApplicationManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Test resource not found",testResource);
		bApp.uninstall();
		Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
		// refresh packages of the custom resource type; the resource should still exist afterwards
		bTypes.update(createTestTypeBundle());
		refresh(bTypes, ctx);
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bTypes.getState());	
		final Resource res = waitForTestResource(waitForAppManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Resource not found", res);
		res.delete();
		bTypes.uninstall();
	}
	
	// like the above, but here we do not start the bTypes bundle, just resolve it (implicitly)
	@Test
	public void restartCustomTypeAppWorks2() throws BundleException, InterruptedException {
		final Bundle bTypes = installTestBundle(ctx, createTestTypeBundle());
		Assert.assertNotNull(bTypes);
		final Bundle bApp = installTestBundle(ctx, createTestAppBundle());
		Assert.assertNotNull(bApp);
		bApp.start();
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bApp.getState());
		final String path = TestApp.RESOURCE_NAME;
		final Resource testResource = waitForTestResource(getApplicationManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Test resource not found",testResource);
		bApp.uninstall();
		Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
		bTypes.update(createTestTypeBundle());
		refresh(bTypes, ctx);
		final Resource res = waitForTestResource(waitForAppManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Resource not found", res);
		res.delete();
		bTypes.uninstall();
	}

	// here the list does not have any subresources
	@Test
	public void listTypeRemainsSetAfterCustomTypeUpdate() throws BundleException, InterruptedException {
		final Bundle bTypes = installTestBundle(ctx, createTestTypeBundle());
		Assert.assertNotNull(bTypes);
		final Bundle bApp = installTestBundle(ctx, createTestAppBundle());
		Assert.assertNotNull(bApp);
		bApp.start();
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bApp.getState());
		final String path = TestApp.RESOURCE_NAME;
		final Resource testResource = waitForTestResource(getApplicationManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Test resource not found",testResource);
		final ResourceList<?> list = testResource.getSubResource("list");
		Assert.assertNotNull("Optional element not found",list);
		Assert.assertFalse("Optional element exists before it is created",list.exists());
		list.create();
		Assert.assertTrue(list.exists());
		Assert.assertFalse("Optional element classified as decorator",list.isDecorator());
		Assert.assertNotNull("Element type missing in optional resource list",list.getElementType());
		final String elementType = list.getElementType().getName();
		Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
		bApp.uninstall(); // not needed any more
		bTypes.update(createTestTypeBundle());
		refresh(bTypes, ctx);
		final Resource res = waitForTestResource(waitForAppManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Resource not found", res);
		final ResourceList<?> list2 = res.getSubResource("list");
		Assert.assertNotNull("Optional element not found",list2);
		Assert.assertNotNull("Element type missing in optional resource list",list2.getElementType());
		Assert.assertEquals("ResourceList element type has changed after a package refresh",elementType, list2.getElementType().getName());
		res.delete();
		bTypes.uninstall();
	}
	
	// contrary to the above, here the list contains elements
	@Test
	public void listTypeRemainsSetAfterCustomTypeUpdate2() throws BundleException, InterruptedException {
		final Bundle bTypes = installTestBundle(ctx, createTestTypeBundle());
		Assert.assertNotNull(bTypes);
		final Bundle bApp = installTestBundle(ctx, createTestAppBundle());
		Assert.assertNotNull(bApp);
		bApp.start();
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bApp.getState());
		final String path = TestApp.RESOURCE_NAME;
		final Resource testResource = waitForTestResource(getApplicationManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Test resource not found",testResource);
		final ResourceList<?> list = testResource.getSubResource("list");
		Assert.assertNotNull("Optional element not found",list);
		Assert.assertFalse("Optional element exists before it is created",list.exists());
		list.create();
		Assert.assertTrue(list.exists());
		Assert.assertFalse("Optional element classified as decorator",list.isDecorator());
		Assert.assertNotNull("Element type missing in optional resource list",list.getElementType());
		final String elementType = list.getElementType().getName();
		list.add();
		Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
		bApp.uninstall(); // not needed any more
		bTypes.update(createTestTypeBundle());
		refresh(bTypes, ctx);
		final Resource res = waitForTestResource(waitForAppManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Resource not found", res);
		final ResourceList<?> list2 = res.getSubResource("list");
		Assert.assertNotNull("Optional element not found",list2);
		Assert.assertNotNull("Element type missing in optional resource list",list2.getElementType());
		Assert.assertEquals("ResourceList element type has changed after a package refresh",elementType, list2.getElementType().getName());
		res.delete();
		bTypes.uninstall();
	}
	
	// here the list contains subresources of a different type than the element type
	@Test
	public void listTypeRemainsSetAfterCustomTypeUpdate3() throws BundleException, InterruptedException {
		final Bundle bTypes = installTestBundle(ctx, createTestTypeBundle());
		Assert.assertNotNull(bTypes);
		final Bundle bApp = installTestBundle(ctx, createTestAppBundle());
		Assert.assertNotNull(bApp);
		bApp.start();
		Assert.assertEquals("Test bundle not active", Bundle.ACTIVE, bApp.getState());
		final String path = TestApp.RESOURCE_NAME;
		final Resource testResource = waitForTestResource(getApplicationManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Test resource not found",testResource);
		final ResourceList<?> list = testResource.getSubResource("list");
		Assert.assertNotNull("Optional element not found",list);
		Assert.assertFalse("Optional element exists before it is created",list.exists());
		list.create();
		Assert.assertTrue(list.exists());
		Assert.assertFalse("Optional element classified as decorator",list.isDecorator());
		Assert.assertNotNull("Element type missing in optional resource list",list.getElementType());
		final String elementType = list.getElementType().getName();
		list.addDecorator("test", TemperatureSensor.class);
		Thread.sleep(3 * PERSISTENCE_PERIOD_MS);
		bApp.uninstall(); // not needed any more
		bTypes.update(createTestTypeBundle());
		refresh(bTypes, ctx);
		final Resource res = waitForTestResource(waitForAppManager().getResourceAccess(), path, 5000);
		Assert.assertNotNull("Resource not found", res);
		final ResourceList<?> list2 = res.getSubResource("list");
		Assert.assertNotNull("Optional element not found",list2);
		Assert.assertNotNull("Element type missing in optional resource list",list2.getElementType());
		Assert.assertEquals("ResourceList element type has changed after a package refresh",elementType, list2.getElementType().getName());
		res.delete();
		bTypes.uninstall();
	}
	
}
