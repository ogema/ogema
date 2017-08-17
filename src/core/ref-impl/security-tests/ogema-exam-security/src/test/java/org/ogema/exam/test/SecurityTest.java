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
package org.ogema.exam.test;

import java.security.AccessControlContext;
import java.security.AllPermission;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.ExceptionListener;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.OsgiAppTestBase;
import static org.ogema.exam.ResourceAssertions.assertExists;
import static org.ogema.exam.ResourceAssertions.assertIsVirtual;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.locations.Room;
import org.ogema.persistence.ResourceDB;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;

import aQute.bnd.osgi.Constants;

/**
 * Integration tests for OGEMA resource security. Note that the application registered by the super class runs with the
 * permissions of the ogema-exam-base bundle, while the application registered by this class has the permissions of this
 * bundle (bundle location is 'local').
 * 
 * @author jlapp
 */
// @Ignore
@RunWith(PaxExam.class)
// @ExamReactorStrategy(PerClass.class)
@ExamReactorStrategy(PerMethod.class)
public class SecurityTest extends OsgiAppTestBase implements Application, ExceptionListener {

	CountDownLatch startLatch = new CountDownLatch(1);
	@Inject
	BundleContext probeContext;
	ApplicationManager securityTestApp;
	ApplicationManager unrestrictedApp;

	@Inject
	ConditionalPermissionAdmin cpa;
	@Inject
	PermissionManager permMan;
	@Inject
	ApplicationSource appstore;
	@Inject
	ResourceDB db;
	@Inject
	org.ogema.recordeddata.DataRecorder recorder;

	private boolean exception;

	public SecurityTest() {
		super(false);
	}

	@Before
	public void registerApp() throws InterruptedException {
		probeContext.registerService(Application.class, this, null);
		assertTrue(startLatch.await(5, TimeUnit.SECONDS));
		unrestrictedApp = getApplicationManager();
		assertNotNull(unrestrictedApp);
	}

	@Configuration
	@Override
	public Option[] config() {
		String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");
		List<Option> options = new ArrayList<>();
		// java policy has to be set on the command line (surefire plugin <argLine>)
		// options.add(CoreOptions.systemProperty("java.security.policy").value("all.policy"));
		// options.add(CoreOptions.systemProperty("org.osgi.framework.security").value("osgi"));
		options.add(CoreOptions.frameworkProperty("org.osgi.framework.security").value("osgi"));
		// options.add(CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security",
		// "2.4.0").startLevel(1));
		// options.add(CoreOptions.systemProperty("felix.config.properties").value("file:./config/config.properties"));
		options.addAll(Arrays.asList(super.config()));
		options.add(CoreOptions.systemProperty("org.ogema.security").value("on"));
		options.add(CoreOptions.systemProperty("osgi.console").value(""));
		options.add(CoreOptions.systemTimeout(30 * 60 * 1000));
		return options.toArray(new Option[0]);
	}

	@Test
	public void securityIsEnabled() {
		assertEquals("osgi", ctx.getProperty("org.osgi.framework.security"));
		SecurityManager sm = System.getSecurityManager();
		assertNotNull("no security!", sm);
		assertNotNull("no conditional permission admin", cpa);
		assertFalse("no permissions in system",
				cpa.newConditionalPermissionUpdate().getConditionalPermissionInfos().isEmpty());
	}

	@Test
	public void createWithoutPermissionFails() {
		exception = false;
		try {
			Room r = securityTestApp.getResourceManagement().createResource(newResourceName(), Room.class);
		} catch (SecurityException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	public void createWithPermittedPathButDifferentTypeFails() {
		exception = false;
		try {
			StringResource r = securityTestApp.getResourceManagement().createResource("ExamProbe",
					StringResource.class);
		} catch (SecurityException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	public void createWithPermissionSucceeds() {
		Room r = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
		assertNotNull(r);
	}

	@Test(expected = SecurityException.class)
	public void readOfLinkedResourceFailsForUnreadableLinkTarget() {
		Room r = null;
		try {
			r = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);

			Room rNoAccess = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
			assertFalse(r.equals(rNoAccess));
			rNoAccess.co2Sensor().create();
			unrestrictedApp.getResourceAccess().<Room> getResource("/ExamProbe").co2Sensor()
					.setAsReference(rNoAccess.co2Sensor());

			// assertTrue(r.co2Sensor().equalsLocation(rNoAccess.co2Sensor()));
		} catch (SecurityException se) {
			Assert.fail("SecurityException at wrong point: " + se);
		}
		assertNotNull(r);
		assertTrue(r.co2Sensor().exists());
	}

	/*
	 * when an app holds a virtual resource and that resource is realized as a reference to an unreadable resource,
	 * subsequend reads on that resource must fail.
	 */
	@Test(expected = SecurityException.class)
	public void readOfUnreadableLinkedResourceFailsForPreexistingVirtualResource() {
		Room r = null;
		FloatResource co2concentration = null;
		try {
			r = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
			co2concentration = r.co2Sensor().reading();
			assertIsVirtual(co2concentration);

			Room rNoAccess = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
			rNoAccess.co2Sensor().reading().create();
			unrestrictedApp.getResourceAccess().<Room> getResource("/ExamProbe").co2Sensor()
					.setAsReference(rNoAccess.co2Sensor());
		} catch (SecurityException se) {
			se.printStackTrace(System.err);
			Assert.fail("SecurityException at wrong point: " + se);
		}
		assertNotNull(r);
		assertExists(co2concentration);
		co2concentration.getValue();
	}

	// @ProbeBuilder
	// public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
	// System.out.println("TestProbeBuilder gets called");
	// probe.setHeader(Constants.EXPORT_PACKAGE, "org.ogema.impl.administration");
	// probe.setHeader(Constants.IMPORT_PACKAGE, "org.ogema.impl.administration");
	// return probe;
	// }

	/*
	 * TOP-SEC 1: The core framework components shall run without any restrictions on the maximum possible Permissions.
	 */
	@Test
	public void topSEC1() {
		ProtectionDomain[] pda = new ProtectionDomain[1];
		checkAllPermission(adminManager.getClass(), pda);
		checkAllPermission(org.ogema.core.application.Application.class, pda); // ogema-api
		checkAllPermission(permMan.getClass(), pda);
		checkAllPermission(securityTestApp.getClass(), pda);
		checkAllPermission(appstore.getClass(), pda);
		checkAllPermission(securityTestApp.getChannelAccess().getClass(), pda);
		checkAllPermission(securityTestApp.getHardwareManager().getClass(), pda);
		checkAllPermission(org.ogema.accesscontrol.ResourcePermission.class, pda); // ogema-internal-api
		checkAllPermission(org.ogema.model.actors.OnOffSwitch.class, pda);
		checkAllPermission(securityTestApp.getLogger().getClass(), pda);
		checkAllPermission(org.ogema.staticpolicy.StaticPolicies.class, pda);
		checkAllPermission(db.getClass(), pda);
		checkAllPermission(securityTestApp.getResourceAccess().getClass(), pda);
		checkAllPermission(securityTestApp.getResourceManagement().getClass(), pda);
		checkAllPermission(securityTestApp.getResourcePatternAccess().getClass(), pda);
		checkAllPermission(recorder.getClass(), pda);
	}

	private void checkAllPermission(Class<?> cls, ProtectionDomain[] pda) {
		pda[0] = cls.getProtectionDomain();
		AccessControlContext acc = new AccessControlContext(pda);
		assertTrue(permMan.handleSecurity(new AllPermission(), acc));
	}

	/*
	 * PERM-SEC 2: The queried path of a resource is translated into a path free of OGEMA 2.0 references (location)
	 * before the check. OGEMA 2.0 references may point to any position of the tree and do not forward any permission.
	 */
	@Test(expected = SecurityException.class)
	public void permSEC2_deny() {
		OnOffSwitch sw = unrestrictedApp.getResourceManagement().createResource("Switch", OnOffSwitch.class);
		sw.stateControl().setAsReference(
				unrestrictedApp.getResourceManagement().createResource("anyBoolean", BooleanResource.class));
		securityTestApp.getResourceAccess().<OnOffSwitch> getResource("/Switch/stateControl"); // has to fail, even the
																								// app has permission to
																								// access to
																								// /Switch/stateControl
	}

	@Test
	public void permSEC2_allow() {
		OnOffSwitch sw = unrestrictedApp.getResourceManagement().createResource("AnotherSwitch", OnOffSwitch.class);
		sw.stateControl().setAsReference(
				unrestrictedApp.getResourceManagement().createResource("anyOtherBoolean", BooleanResource.class));
		BooleanResource oos = securityTestApp.getResourceAccess()
				.<BooleanResource> getResource("/AnotherSwitch/stateControl");
		// it should work,
		// because the
		// app has permission to
		// access to
		// /anyOtherBoolean
		assertExists(oos);
	}

	/*
	 * PERM-SEC7: Permission is needed to get a channel from the channel manager. This permission holds information
	 * about the bus Id, the bus type, the address and the registers. It is checked by the channel manager before
	 * returning the channel to the device driver.
	 */
	@Test
	public void permSEC7_allow() {
		DeviceLocator deviceLocator = new DeviceLocator(DummyChannelDriver.DRIVER_ID, "IF0", "10.11.12.13:8080", null);
		ChannelLocator chLoc = new ChannelLocator("s11", deviceLocator);
		try {
			ChannelConfiguration channelConfig = securityTestApp.getChannelAccess().addChannel(chLoc,
					Direction.DIRECTION_INPUT, -1);
		} catch (ChannelAccessException e) {
		}
	}
	
	@Test
	public void structureListenerCallbackOnDeniedResourceWorks1() throws InterruptedException {
		final Room r1 = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
		r1.temperatureSensor().create();
		final Room r2 = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
		r2.temperatureSensor().setAsReference(unrestrictedApp.getResourceAccess().getResource(r1.temperatureSensor().getPath()));
		try {
			securityTestApp.getResourceAccess().getResource(r2.getPath());
			throw new AssertionError("Expected SecurityException missing: accessed a resource without permission");
		} catch (SecurityException e) {
			// expected
		}
		final CountDownLatch latch = new CountDownLatch(1);
		final ResourceStructureListener listener = new ResourceStructureListener() {
			
			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				if (event.getType() == EventType.RESOURCE_DELETED)
					latch.countDown();
			}
		};
		r2.temperatureSensor().addStructureListener(listener);
		r1.temperatureSensor().delete();
		Assert.assertTrue("Resource structure callback missing", latch.await(5, TimeUnit.SECONDS));
		r2.temperatureSensor().removeStructureListener(listener);
		r1.delete();
		r2.delete();
	}
	
	@Test
	public void structureListenerCallbackOnDeniedResourceWorks2() throws InterruptedException {
		final Room r1 = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
		r1.temperatureSensor().create();
		final Room r2 = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
		r2.temperatureSensor().setAsReference(unrestrictedApp.getResourceAccess().getResource(r1.temperatureSensor().getPath()));
		try {
			securityTestApp.getResourceAccess().getResource(r2.getPath());
			throw new AssertionError("Expected SecurityException missing: accessed a resource without permission");
		} catch (SecurityException e) {
			// expected
		}
		final CountDownLatch latch = new CountDownLatch(1);
		final ResourceStructureListener listener = new ResourceStructureListener() {
			
			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				if (event.getType() == EventType.SUBRESOURCE_ADDED)
					latch.countDown();
			}
		};
		r2.temperatureSensor().addStructureListener(listener);
		r1.temperatureSensor().reading().create();
		Assert.assertTrue("Resource structure callback missing", latch.await(5, TimeUnit.SECONDS));
		r2.temperatureSensor().removeStructureListener(listener);
		r1.delete();
		r2.delete();
	}
	
	@Test
	public void structureListenerCallbackOnDeniedResourceWorks3() throws InterruptedException {
		final Room r1 = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
		r1.temperatureSensor().create();
		final Room r2 = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
		r2.temperatureSensor().setAsReference(unrestrictedApp.getResourceAccess().getResource(r1.temperatureSensor().getPath()));
		try {
			securityTestApp.getResourceAccess().getResource(r2.getPath());
			throw new AssertionError("Expected SecurityException missing: accessed a resource without permission");
		} catch (SecurityException e) {
			// expected
		}
		final CountDownLatch latch = new CountDownLatch(1);
		final ResourceStructureListener listener = new ResourceStructureListener() {
			
			@Override
			public void resourceStructureChanged(ResourceStructureEvent event) {
				if (event.getType() == EventType.SUBRESOURCE_REMOVED)
					latch.countDown();
			}
		};
		r2.addStructureListener(listener);
		r1.temperatureSensor().delete();
		Assert.assertTrue("Resource structure callback missing", latch.await(5, TimeUnit.SECONDS));
		r2.removeStructureListener(listener);
		r1.delete();
		r2.delete();
	}
	
	
	@Test
	public void valueListenerCallbackOnDeniedResourceWorks() throws InterruptedException {
		final Room r1 = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
		r1.temperatureSensor().reading().create().activate(false);;
		final Room r2 = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
		r2.temperatureSensor().reading().setAsReference(unrestrictedApp.getResourceAccess().getResource(r1.temperatureSensor().reading().getPath()));
		try {
			securityTestApp.getResourceAccess().getResource(r2.getPath());
			throw new AssertionError("Expected SecurityException missing: accessed a resource without permission");
		} catch (SecurityException e) {
			// expected
		}
		final CountDownLatch latch = new CountDownLatch(1);
		final ResourceValueListener<FloatResource> listener = new ResourceValueListener<FloatResource>() {
			
			@Override
			public void resourceChanged(FloatResource resource) {
				latch.countDown();
			}
		};
		r2.temperatureSensor().reading().addValueListener(listener);
		r1.temperatureSensor().reading().setValue(30F);
		Assert.assertTrue("Resource value callback missing", latch.await(5, TimeUnit.SECONDS));
		r2.temperatureSensor().reading().removeValueListener(listener);
		r1.delete();
		r2.delete();
	}
	
	@Test
	public void recursiveReferencesWork1() throws InterruptedException {
		final Room r1 = securityTestApp.getResourceManagement().createResource("ExamProbe", Room.class);
		r1.temperatureSensor().create();
		final Room r2 = unrestrictedApp.getResourceManagement().createResource(newResourceName(), Room.class);
		r2.temperatureSensor().setAsReference(unrestrictedApp.getResourceAccess().getResource(r1.temperatureSensor().getPath()));
		try {
			securityTestApp.getResourceAccess().getResource(r2.getPath());
			throw new AssertionError("Expected SecurityException missing: accessed a resource without permission");
		} catch (SecurityException e) {
			// expected
		}
		final Room r3 = securityTestApp.getResourceManagement().createResource("ExamProbe2", Room.class);
		r3.temperatureSensor().create();
		r1.temperatureSensor().setAsReference(r3.temperatureSensor()); // now r2.temperaureSensor points to r3.temperatureSensor
		Assert.assertEquals(r2.temperatureSensor().getLocation(), r3.temperatureSensor().getPath());
		r1.delete();
		r2.delete();
		r3.delete();
	}

	@Override
	public void start(ApplicationManager appManager) {
		securityTestApp = appManager;
		appManager.addExceptionListener(this);
		startLatch.countDown();
	}

	@Override
	public void stop(AppStopReason reason) {
	}

	@Override
	public void exceptionOccured(Throwable e) {
		exception = true;
	}

}
