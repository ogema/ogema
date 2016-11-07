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
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.exam.OsgiAppTestBase;
import static org.ogema.exam.ResourceAssertions.assertExists;
import static org.ogema.exam.ResourceAssertions.assertIsVirtual;
import org.ogema.model.locations.Room;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;

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
public class SecurityTest extends OsgiAppTestBase implements Application {

	CountDownLatch startLatch = new CountDownLatch(1);
	@Inject
	BundleContext probeContext;
	ApplicationManager securityTestApp;
	ApplicationManager unrestrictedApp;

	@Inject
	ConditionalPermissionAdmin cpa;

	public SecurityTest() {
		super(true);
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
//		 options.add(CoreOptions.systemProperty("java.security.policy").value("all.policy"));
		// options.add(CoreOptions.systemProperty("org.osgi.framework.security").value("osgi"));
		 options.add(CoreOptions.frameworkProperty("org.osgi.framework.security").value("osgi"));
		// options.add(CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security",
		// "2.4.0").startLevel(1));
		// options.add(CoreOptions.systemProperty("felix.config.properties").value("file:./config/config.properties"));
		options.addAll(Arrays.asList(super.config()));
		options.add(CoreOptions.systemProperty("org.ogema.security").value("on"));
		options.add(CoreOptions.systemProperty("osgi.console").value(""));
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

	@Test(expected = SecurityException.class)
	public void createWithoutPermissionFails() {
		Room r = securityTestApp.getResourceManagement().createResource(newResourceName(), Room.class);
	}

	@Test(expected = SecurityException.class)
	public void createWithPermittedPathButDifferentTypeFails() {
		StringResource r = securityTestApp.getResourceManagement().createResource("ExamProbe", StringResource.class);
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

	@Override
	public void start(ApplicationManager appManager) {
		securityTestApp = appManager;
		startLatch.countDown();
	}

	@Override
	public void stop(AppStopReason reason) {

	}

}
