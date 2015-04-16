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
package org.ogema.channelmapper.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Abstract base class for tests that need an ApplicationManager. An ApplicationManager instance is available inside
 * test methods through {@link #getApplicationManager()}. The before() and after() methods also test that an Application
 * is started and stopped correctly.
 * 
 * @author jlapp
 */
@RunWith(JUnit4TestRunner.class)
public abstract class OsgiAppTestBase {

	@Inject
	protected BundleContext ctx;
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch stopLatch = new CountDownLatch(1);
	private volatile ApplicationManager appMan;
	private ServiceRegistration<Application> registration;

	protected int HTTP_PORT = 4712;
	protected final boolean includeTestBundle;

	public OsgiAppTestBase() {
		this(false);
	}

	/**
	 * @param includeTestBundle
	 *            include the bundle containing the test directly (default is true)?
	 */
	public OsgiAppTestBase(boolean includeTestBundle) {
		this.includeTestBundle = includeTestBundle;
	}

	@Configuration
	public Option[] config() {
		return new Option[] {
				CoreOptions.frameworkProperty("osgi.console").value("true"),
				CoreOptions.frameworkProperty("osgi.console.enable.builtin").value("true"),
				CoreOptions.frameworkProperty("org.osgi.service.http.port").value(Integer.toString(HTTP_PORT)),
				CoreOptions.junitBundles(),
				// load the bundle of the extending class directly from maven build dir:
				CoreOptions.when(includeTestBundle).useOptions(
						CoreOptions.bundle("reference:file:target/classes/").start()),
				CoreOptions.composite(frameworkBundles()), webConsoleOption() };
	}

	public Option[] frameworkBundles() {
		return new Option[] {
				CoreOptions.mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "3.0.1").start(),
				CoreOptions.mavenBundle("org.ops4j.pax.exam", "pax-exam-junit4", "2.6.0").start(),
				// jersey -->
				CoreOptions.mavenBundle("com.google.guava", "guava-jdk5", "14.0.1").start(),
				CoreOptions.mavenBundle("javax.annotation", "javax.annotation-api", "1.2").start(),
				CoreOptions.mavenBundle("javax.ws.rs", "javax.ws.rs-api", "2.0").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2", "hk2-api", "2.2.0-b14").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2", "hk2-utils", "2.2.0-b14").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2.external", "cglib", "2.2.0-b14").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2.external", "asm-all-repackaged", "2.2.0-b14").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2", "hk2-locator", "2.2.0-b14").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2", "osgi-resource-locator", "1.0.1").start(),
				CoreOptions.mavenBundle("org.glassfish.hk2.external", "javax.inject", "2.2.0-b14").start(),
				CoreOptions.mavenBundle("org.glassfish.jersey.containers", "jersey-container-servlet-core", "2.0")
						.start(),
				CoreOptions.mavenBundle("org.glassfish.jersey.core", "jersey-common", "2.0").start(),
				CoreOptions.mavenBundle("org.glassfish.jersey.core", "jersey-client", "2.0").start(),
				CoreOptions.mavenBundle("org.glassfish.jersey.core", "jersey-server", "2.0").start(),
				CoreOptions.mavenBundle("org.glassfish.jersey.media", "jersey-media-json-jackson", "2.0"),
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-jaxrs", "1.9.13"),
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-core-lgpl", "1.9.13"),
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-mapper-lgpl", "1.9.13"),
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-xc", "1.9.11"),
				CoreOptions.mavenBundle("javax.validation", "validation-api", "1.1.0.Final").start(),
				CoreOptions.mavenBundle("com.sun.mail", "javax.mail", "1.5.0").start(),
				// <-- jersey
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.2.0").start(),
				CoreOptions.mavenBundle("javax.servlet", "javax.servlet-api", "3.0.1"),
				CoreOptions.mavenBundle("org.slf4j", "slf4j-api", "1.7.2"),
				CoreOptions.mavenBundle("joda-time", "joda-time", "2.2"),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-util", "6.9.1"),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-request", "6.9.1"),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-core", "6.9.1"),
				CoreOptions.mavenBundle("org.ogema.core", "models", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.core", "api", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-exam-base", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "internal-api", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-logger", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "app-manager", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "resource-manager", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "permission-admin", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "security", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "persistence", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "channel-manager", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.services", "channel-mapper", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "hardware-manager", "2.0-SNAPSHOT").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "util", "2.0-SNAPSHOT").start() };
	}

	/**
	 * return composite Option containing bundles for felix webconsole
	 */
	public Option webConsoleOption() {
		return CoreOptions.composite(CoreOptions
				.mavenBundle("org.apache.felix", "org.apache.felix.webconsole", "4.2.0"), CoreOptions.mavenBundle(
				"commons-fileupload", "commons-fileupload", "1.2.2"), CoreOptions.mavenBundle(
				"org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-io", "1.4_3"), CoreOptions
				.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.event", "1.0.2"), CoreOptions
				.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.ds", "1.0.0"), CoreOptions
				.mavenBundle("de.twentyeleven.skysail", "org.json-osgi", "20080701"), CoreOptions.mavenBundle(
				"org.apache.felix", "org.apache.felix.webconsole.plugins.obr", "1.0.0"), CoreOptions.mavenBundle(
				"org.apache.felix", "org.apache.felix.webconsole.plugins.memoryusage", "1.0.4"));
	}

	private final Application app = new Application() {
		@Override
		public void start(ApplicationManager appManager) {
			assertNotNull(appManager);
			appMan = appManager;
			doStart(appMan);
			startLatch.countDown();
		}

		@Override
		public void stop(AppStopReason whatever) {
			doStop();
			stopLatch.countDown();
		}
	};

	public final ApplicationManager getApplicationManager() {
		return appMan;
	}

	/**
	 * called at the end of the test application's start method
	 */
	public void doStart(ApplicationManager appMan) {
	}

	/**
	 * called at the start of the test application's stop method
	 */
	public void doStop() {
	}

	/**
	 * called at the end of {@link #before()}
	 */
	public void doBefore() {
	}

	/**
	 * called at the start of {@link #after()}
	 */
	public void doAfter() {
	}

	@Before
	public void before() throws InterruptedException {
		doBefore();
		registration = ctx.registerService(Application.class, app, null);
		assertTrue("app not started", startLatch.await(3, TimeUnit.SECONDS));
		assertNotNull(appMan);
	}

	@After
	public void after() throws InterruptedException {
		registration.unregister();
		assertTrue("app not stopped", stopLatch.await(3, TimeUnit.SECONDS));
		doAfter();
	}
}
