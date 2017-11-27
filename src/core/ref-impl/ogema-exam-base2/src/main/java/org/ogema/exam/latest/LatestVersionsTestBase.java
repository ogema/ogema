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
package org.ogema.exam.latest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * Similar to {@link OsgiAppTestBase}, but uses different dependency version. 
 * It tries to use the respectively latest available version of a dependency which is still compatible with Java 7
 */
@RunWith(PaxExam.class)
public abstract class LatestVersionsTestBase {
	@Inject
	protected BundleContext ctx;
	@Inject
	protected AdministrationManager adminManager;
	private CountDownLatch startLatch = new CountDownLatch(1);
	private CountDownLatch stopLatch = new CountDownLatch(1);
	private volatile ApplicationManager appMan;
	private ServiceRegistration<Application> registration;

	protected final static String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");

	protected static int HTTP_PORT = 4712;
	protected final boolean includeTestBundle;

	static final AtomicInteger resourceCounter = new AtomicInteger(0);

	public LatestVersionsTestBase() {
		this(false);
	}

	/**
	 * @param includeTestBundle
	 *            include the bundle containing the test directly (default is true)?
	 */
	public LatestVersionsTestBase(boolean includeTestBundle) {
		this.includeTestBundle = includeTestBundle;
	}

	@Configuration
	public Option[] config() {
		return new Option[] { CoreOptions.systemProperty("ogema.resources.useByteCodeGeneration").value("true"),
				CoreOptions.frameworkProperty("osgi.console").value("true"),
				CoreOptions.frameworkProperty("osgi.console.enable.builtin").value("true"),
				CoreOptions.frameworkProperty("org.osgi.service.http.port").value(Integer.toString(HTTP_PORT)),
				CoreOptions.frameworkProperty("org.osgi.framework.bsnversion").value("multiple"),
				CoreOptions.frameworkProperty(Constants.FRAMEWORK_SYSTEMCAPABILITIES_EXTRA).value("osgi.contract;osgi.contract=\"JavaServlet\";version:Version=\"3.1\""),
				// CoreOptions.systemProperty("org.ogema.security").value("on"),
				CoreOptions.junitBundles(),
				// load the bundle of the extending class directly from maven build dir:
				CoreOptions.when(includeTestBundle)
						.useOptions(CoreOptions.bundle("reference:file:target/classes/").start()),
				CoreOptions.composite(frameworkBundles()),
				// ogemaWebFrontentOption(),
				// wicketGuiOption(),
				// webConsoleOption(),
				// felixGogoShellOption(),
		};
	}

	public Option[] frameworkBundles() {
		return new Option[] {
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.6.0").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "permission-admin").version(ogemaVersion).startLevel(1)
						.start(),

				CoreOptions.mavenBundle("org.ops4j.pax.exam", "pax-exam-junit4", "4.11.0").start(),

				CoreOptions.mavenBundle("org.ow2.asm", "asm-all", "5.2").start(),

				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.scr", "2.0.12").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.4.8").start(),
//				CoreOptions.mavenBundle("org.ogema.external", "org.apache.felix.useradmin.filestore", "1.0.2").start(),
//				CoreOptions.mavenBundle("org.ogema.external", "org.apache.felix.useradmin", "1.0.3").start(),
				// we use knopflerfish here, and felix useradmin in exam-base1
				CoreOptions.mavenBundle("org.osgi", "org.knopflerfish.bundle.useradmin", "4.1.1").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.14").start(), 
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "3.1.6").start(),
                CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-servlets", "9.2.14.v20151106"),
				CoreOptions.mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),

				CoreOptions.mavenBundle("org.slf4j", "slf4j-api", "1.7.25"),
//				CoreOptions.mavenBundle("joda-time", "joda-time", "2.9.3"), // not required any more

				// jackson (for serialization manager) -->
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-core", "2.9.0"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-annotations", "2.9.0"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-databind", "2.9.0"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.module", "jackson-module-jaxb-annotations", "2.9.0"),
				// <-- jackson

				// apache commons (for recordeddata-storage and framework-administration)-->
				CoreOptions.mavenBundle("org.apache.commons", "commons-math3", "3.6.1"),
				CoreOptions.mavenBundle("commons-io", "commons-io", "2.5"),
				CoreOptions.mavenBundle("commons-codec", "commons-codec", "1.10"),
				CoreOptions.mavenBundle("org.apache.commons", "commons-lang3", "3.6"),
				CoreOptions.mavenBundle("org.json", "json", "20170516"),
				// <-- apache commons
                
                CoreOptions.mavenBundle("com.google.guava", "guava", "20.0"),

				CoreOptions.mavenBundle("org.ogema.core", "models").version(ogemaVersion).startLevel(1).start(),
				CoreOptions.mavenBundle("org.ogema.core", "api").version(ogemaVersion).startLevel(1).start(),
				CoreOptions.mavenBundle("org.ogema.tools", "memory-timeseries").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "administration").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-exam-base2").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "internal-api").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "non-secure-apploader").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-logger").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "app-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "resource-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "resource-access-advanced").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "security").version(ogemaVersion).startLevel(4).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-security-manager").version(ogemaVersion).startLevel(4).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "persistence").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "channel-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "hardware-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "recordeddata-slotsdb").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "util").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "rest").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.tools", "resource-utils").version(ogemaVersion).start(), };
	}

	public Option wicketGuiOption() {
		return CoreOptions.composite(
				CoreOptions.mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib", "2.2_2")
						.start(),
				CoreOptions.mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.javax-inject",
						"1_1").start(),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-util", "6.23.0"),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-request", "6.23.0"),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-core", "6.23.0"),
				CoreOptions.mavenBundle("org.ogema.tools", "wicket-gui").version(ogemaVersion),
				CoreOptions.mavenBundle("org.ogema.tools", "wicket-gui-impl").version(ogemaVersion).start());
	}

	/**
	 * return composite Option containing bundles for felix webconsole
	 */
	public Option webConsoleOption() {
		return CoreOptions.composite(
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.webconsole", "4.3.4"),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.event", "1.1.6"),
				// CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.inventory", "1.0.4"), // required with
				// newer version of plugins.ds
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.ds", "2.0.6"),
				CoreOptions.mavenBundle("commons-fileupload", "commons-fileupload", "1.3.3"),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.obr", "1.0.4"),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.memoryusage", "1.0.6"));
	}

	public Option felixGogoShellOption() {
		return CoreOptions.composite(
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime", "1.0.4"),
				// the shell causes ugly VM crashes in tests -> "the forked JVM terminated without properly saying goodbye"
//				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell", "1.0.0"),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.gogo.command", "1.0.2"));
	}

	public Option ogemaWebFrontentOption() {
		return CoreOptions.composite(
				CoreOptions.mavenBundle("commons-fileupload", "commons-fileupload", "1.3.3"),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "framework-administration").version(ogemaVersion).start());
	}

	protected Application app = new Application() {
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
			appMan = null;
		}
	};

	/** Returns a new resource name for use in test cases. */
	public String newResourceName() {
		return getClass().getSimpleName() + "_" + resourceCounter.incrementAndGet();
	}

	public ApplicationManager getApplicationManager() {
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
		// create standard users, if not existing yet
		try {
			adminManager.getUser("master");
		} catch (RuntimeException e) {
			adminManager.createUserAccount("master", true);
		}
		try {
			adminManager.getUser("rest");
		} catch (RuntimeException e) {
			adminManager.createUserAccount("rest", false);
		}
		registration = ctx.registerService(Application.class, app, null);
		assertTrue("app not started", startLatch.await(3, TimeUnit.SECONDS));
		assertNotNull(appMan);
        appMan.getLogger().setMaximumLogLevel(LogOutput.CONSOLE, LogLevel.TRACE);
		doBefore();
	}

	@After
	public void after() throws InterruptedException {
		doAfter();
		registration.unregister();
		registration = null;
		assertTrue("app not stopped", stopLatch.await(3, TimeUnit.SECONDS));
	}
}
