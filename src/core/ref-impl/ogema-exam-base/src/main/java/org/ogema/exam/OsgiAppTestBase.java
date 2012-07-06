/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.exam;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Abstract base class for tests that need an ApplicationManager. An ApplicationManager instance is available inside
 * test methods through {@link #getApplicationManager()}. The before() and after() methods also test that an Application
 * is started and stopped correctly.
 * 
 * @author jlapp
 */
@RunWith(PaxExam.class)
public abstract class OsgiAppTestBase {

	@Inject
	protected BundleContext ctx;
	private CountDownLatch startLatch = new CountDownLatch(1);
	private CountDownLatch stopLatch = new CountDownLatch(1);
	private volatile ApplicationManager appMan;
	private ServiceRegistration<Application> registration;

	String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");

	protected int HTTP_PORT = 4712;
	protected final boolean includeTestBundle;

	static final AtomicInteger resourceCounter = new AtomicInteger(0);

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
				CoreOptions.systemProperty("ogema.resources.useByteCodeGeneration").value("true"),
				CoreOptions.frameworkProperty("osgi.console").value("true"),
				CoreOptions.frameworkProperty("osgi.console.enable.builtin").value("true"),
				CoreOptions.frameworkProperty("org.osgi.service.http.port").value(Integer.toString(HTTP_PORT)),
				CoreOptions.frameworkProperty("org.osgi.framework.bsnversion").value("multiple"),
				CoreOptions.systemProperty("org.ogema.security").value("off"),
				CoreOptions.junitBundles(),
				// load the bundle of the extending class directly from maven build dir:
				CoreOptions.when(includeTestBundle).useOptions(
						CoreOptions.bundle("reference:file:target/classes/").start()),
				CoreOptions.composite(frameworkBundles()),
		//ogemaWebFrontentOption(),
		//wicketGuiOption(),
		//webConsoleOption(),
		//felixGogoShellOption(),
		};
	}

	public Option[] frameworkBundles() {
		return new Option[] {
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.2.0").noStart(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "permission-admin").version(ogemaVersion).startLevel(1)
						.start(),

				CoreOptions.mavenBundle("org.ops4j.pax.exam", "pax-exam-junit4", "3.5.0").start(),

				CoreOptions.mavenBundle("org.ow2.asm", "asm-all", "5.0.3").start(),

				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.3.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.useradmin.filestore", "1.0.2")
						.startLevel(2).start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.useradmin", "1.0.3").startLevel(3)
						.start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.6.0").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.api", "2.3.0").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "2.3.0").start(),
				CoreOptions.mavenBundle("javax.servlet", "javax.servlet-api", "3.0.1"),
				CoreOptions.mavenBundle("org.slf4j", "slf4j-api", "1.7.2"),
				CoreOptions.mavenBundle("joda-time", "joda-time", "2.2"),

				// jackson (for serialization manager) -->
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-core-lgpl", "1.9.13"),
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-mapper-lgpl", "1.9.13"),
				CoreOptions.mavenBundle("org.codehaus.jackson", "jackson-xc", "1.9.11"),
				// <-- jackson

				// apache commons (for recordeddata-storage and framework-administration)-->
				CoreOptions.mavenBundle("org.apache.commons", "commons-math3", "3.3"),
				CoreOptions.mavenBundle("commons-io", "commons-io", "2.4"),
				CoreOptions.mavenBundle("commons-codec", "commons-codec", "1.9"),
				// <-- apache commons

				CoreOptions.mavenBundle("org.ogema.core", "models").version(ogemaVersion).startLevel(1).start(),
				CoreOptions.mavenBundle("org.ogema.core", "api").version(ogemaVersion).startLevel(1).start(),
				CoreOptions.mavenBundle("org.ogema.tools", "memory-timeseries").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "administration").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-exam-base").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "internal-api").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-logger").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "app-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "resource-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "resource-access-advanced").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "security").version(ogemaVersion).startLevel(4).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "persistence").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "channel-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "hardware-manager").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "recordeddata-slotsdb").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "util").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "rest").version(ogemaVersion).start(), };
	}

	public Option wicketGuiOption() {
		return CoreOptions.composite(CoreOptions.mavenBundle("org.apache.servicemix.bundles",
				"org.apache.servicemix.bundles.cglib", "2.2_2").start(), CoreOptions.mavenBundle(
				"org.apache.servicemix.bundles", "org.apache.servicemix.bundles.javax-inject", "1_1").start(),
				CoreOptions.mavenBundle("org.apache.wicket", "wicket-util", "6.11.0"), CoreOptions.mavenBundle(
						"org.apache.wicket", "wicket-request", "6.11.0"), CoreOptions.mavenBundle("org.apache.wicket",
						"wicket-core", "6.11.0"), CoreOptions.mavenBundle("org.ogema.service", "ogema-gui").version(
						ogemaVersion).start());
	}

	/**
	 * return composite Option containing bundles for felix webconsole
	 */
	public Option webConsoleOption() {
		return CoreOptions.composite(CoreOptions
				.mavenBundle("org.apache.felix", "org.apache.felix.webconsole", "4.2.0"), CoreOptions.mavenBundle(
				"org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-io", "1.4_3"), CoreOptions
				.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.event", "1.0.2"), CoreOptions
				.mavenBundle("org.apache.felix", "org.apache.felix.webconsole.plugins.ds", "1.0.0"), CoreOptions
				.mavenBundle("de.twentyeleven.skysail", "org.json-osgi", "20080701"), CoreOptions.mavenBundle(
				"commons-fileupload", "commons-fileupload", "1.3.1"), CoreOptions.mavenBundle("org.apache.felix",
				"org.apache.felix.webconsole.plugins.obr", "1.0.0"), CoreOptions.mavenBundle("org.apache.felix",
				"org.apache.felix.webconsole.plugins.memoryusage", "1.0.4"));
	}

	public Option felixGogoShellOption() {
		return CoreOptions.composite(CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime",
				"0.10.0"), CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell", "0.10.0"),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.gogo.command", "0.12.0"));
	}

	public Option ogemaWebFrontentOption() {
		return CoreOptions.composite(CoreOptions.mavenBundle("de.twentyeleven.skysail", "org.json-osgi", "20080701"),
				CoreOptions.mavenBundle("commons-fileupload", "commons-fileupload", "1.3.1"), CoreOptions.mavenBundle(
						"org.ogema.ref-impl", "framework-administration").version(ogemaVersion).start(), CoreOptions
						.mavenBundle("commons-codec", "commons-codec").version("1.9").start());
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
		}
	};

	/** Returns a new resource name for use in test cases. */
	public String newResourceName() {
		return getClass().getSimpleName() + "_" + resourceCounter.incrementAndGet();
	}

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
		registration = ctx.registerService(Application.class, app, null);
		assertTrue("app not started", startLatch.await(3, TimeUnit.SECONDS));
		assertNotNull(appMan);
		doBefore();
	}

	@After
	public void after() throws InterruptedException {
		doAfter();
		registration.unregister();
		assertTrue("app not stopped", stopLatch.await(3, TimeUnit.SECONDS));
	}
}
