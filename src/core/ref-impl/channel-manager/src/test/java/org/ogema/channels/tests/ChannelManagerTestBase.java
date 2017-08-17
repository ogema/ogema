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
package org.ogema.channels.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ogema.channels.tests.utils.TestDriver;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@RunWith(PaxExam.class)
public class ChannelManagerTestBase implements Application {

	protected static final AtomicInteger deviceCounter  = new AtomicInteger(0);
	protected static final AtomicInteger channelCounter = new AtomicInteger(0); 

	protected String ogemaVersion = MavenUtils.asInProject().getVersion("org.ogema.core", "api");

	@Inject
	protected BundleContext ctx;
	
	@Inject
	protected ChannelAccess channelAccess;

	@Before
	public void setup() {
		
		System.out.println("setup");
		
		assertNotNull(ctx);
		assertNotNull(channelAccess);
		ctx.registerService(Application.class, this, null);
	}
	
	@After
	public void stop() {
		for (ServiceRegistration<ChannelDriver> reg: driverRegistrations) {
			try {
				ChannelDriver d = ctx.getService(reg.getReference());
				d.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				reg.unregister();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		driverRegistrations.clear();
		
		// clean up channels
		// FIXME does not capture all channels... impossible to get access to all of them, since channelAccess.getAllConfiguredChannels() returns ChannelLocators -> why?
		// pau: it does not return the ChannelConfigurations for security reasons. 
		// The ChannelConfigurations are private to the calling Apps.
		// After deallocating the drivers there should be no channels left anyway, 
		// because the channel manager closes all channels for deallocated drivers.
		List<ChannelLocator> list = channelAccess.getAllConfiguredChannels();
		Assert.assertTrue(list.isEmpty());
		
		for (ChannelConfiguration c:configs.values()) {
			try {
				channelAccess.deleteChannel(c);
			} catch (Exception e) {
			}
		}
		
		System.out.println("stop");
	}

	@Configuration
	public Option[] configure() {
		return options(
				// excludeDefaultRepositories(),
				// repositories("file:.m2/"),
				CoreOptions.systemProperty("ogema.resources.useByteCodeGeneration").value("true"),
				CoreOptions.frameworkProperty("osgi.console").value("true"),
				CoreOptions.frameworkProperty("osgi.console.enable.builtin").value("true"),
				CoreOptions.frameworkProperty("org.osgi.service.http.port").value(Integer.toString(4712)),
				CoreOptions.frameworkProperty("org.osgi.framework.bsnversion").value("multiple"),junitBundles(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.framework.security", "2.2.0").start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "permission-admin", ogemaVersion).start(),
				CoreOptions.mavenBundle("org.slf4j", "slf4j-api", "1.7.21"),
				CoreOptions.mavenBundle("joda-time", "joda-time", "2.2"),
				CoreOptions.mavenBundle("org.json", "json", "20170516"),
				
				CoreOptions.mavenBundle("org.ops4j.pax.exam", "pax-exam-junit4", "4.6.0").start(),
				CoreOptions.mavenBundle("org.ow2.asm", "asm-all", "5.1").start(),
				CoreOptions.mavenBundle("org.apache.commons", "commons-math3", "3.6.1"),
				CoreOptions.mavenBundle("commons-io", "commons-io", "2.5"),
				CoreOptions.mavenBundle("commons-codec", "commons-codec", "1.10"),

				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.8.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.4.6").start(),
				CoreOptions.mavenBundle("org.ogema.external", "org.apache.felix.useradmin.filestore", "1.0.2").start(),
				CoreOptions.mavenBundle("org.ogema.external", "org.apache.felix.useradmin", "1.0.3").start(),
				// not using latest version 1.8.8, since it causes problems with security
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.6.0").start(), 
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.api", "2.3.2").start(),
				CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.http.jetty", "2.3.2").start(),
				CoreOptions.mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
				CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-servlets", "9.2.9.v20150224"),
				CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-http", "9.2.9.v20150224"),
				CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-util", "9.2.9.v20150224"),
				CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-io", "9.2.9.v20150224"),
				CoreOptions.mavenBundle("org.eclipse.jetty", "jetty-server", "9.2.9.v20150224"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-core", "2.7.4"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-annotations", "2.7.4"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.core", "jackson-databind", "2.7.4"),
				CoreOptions.mavenBundle("com.fasterxml.jackson.module", "jackson-module-jaxb-annotations", "2.7.4"),

                CoreOptions.mavenBundle("com.google.guava", "guava", "19.0").startLevel(1).start(),
                
				CoreOptions.mavenBundle("org.ogema.core", "models").version(ogemaVersion).startLevel(1).start(),
				CoreOptions.mavenBundle("org.ogema.core", "api").version(ogemaVersion).startLevel(1).start(),
				CoreOptions.mavenBundle("org.ogema.tools", "memory-timeseries").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "administration").version(ogemaVersion).start(),
				CoreOptions.mavenBundle("org.ogema.ref-impl", "ogema-exam-base").version(ogemaVersion).start(),
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
				CoreOptions.mavenBundle("org.ogema.tools", "resource-utils").version(ogemaVersion).start(),
				
				CoreOptions.composite(config())
		);

	}
	
//	@Override
//	@Configuration
	public Option[] config() {
		return new Option[] {
				CoreOptions.systemProperty("org.ogema.security").value("off"),
//				CoreOptions.composite(super.config()),
//				CoreOptions.mavenBundle("org.ogema.driver.coap","coap-driver", ogemaVersion).start(), // where is this?
				CoreOptions.mavenBundle("org.easymock","easymock", "3.4"),
				CoreOptions.mavenBundle("org.jmock","jmock-junit4", "2.8.2"),
				CoreOptions.mavenBundle("org.ops4j.pax.tinybundles","tinybundles", "2.1.1"),
				CoreOptions.mavenBundle("biz.aQute.bnd","biz.aQute.bndlib", "3.2.0")
				
			};
	}

	protected ChannelConfiguration addMbusChannel(int samplingPeriod) throws ChannelAccessException {
		return addAndConfigureWritableChannel("mbus", "/dev/ttyUSB0", "p" + deviceCounter.incrementAndGet(), 
					"02/" + channelCounter.incrementAndGet(), samplingPeriod);
	}

	private final Map<String,ChannelConfiguration> configs = new ConcurrentHashMap<>();
	
	protected ChannelConfiguration addAndConfigureWritableChannel(String driverId, String interfaceName, 
			String deviceAddress, String channelAddress, long samplingPeriod) throws ChannelAccessException {

		DeviceLocator deviceLocator = new DeviceLocator(driverId, interfaceName, deviceAddress, null);
		ChannelLocator channelLocator = new ChannelLocator(channelAddress, deviceLocator);
		ChannelConfiguration channelConfiguration = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, samplingPeriod);
		configs.put(channelConfiguration.toString(),channelConfiguration);

		return channelConfiguration;
	}

	
	protected TestDriver startModbus() {
		TestDriver d = new TestDriver("modbus", "modbus test driver");
		startDriverBundle(d);
		return d;
	}
	
	protected TestDriver startMbus() {
		TestDriver d = new TestDriver("mbus", "wired mbus test driver");
		startDriverBundle(d);
		return d;
	}
	
	private final List<ServiceRegistration<ChannelDriver>> driverRegistrations = new ArrayList<>();
	
	protected void startDriverBundle(ChannelDriver testDriver) {
		driverRegistrations.add(ctx.registerService(ChannelDriver.class,testDriver, null));
		// wait for max 4s for driver to be available
		for (int counter = 0; counter<200; counter++) {
			if (channelAccess.getDriverIds().contains(testDriver.getDriverId()))
				break;
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertTrue("Driver registration failed",channelAccess.getDriverIds().contains(testDriver.getDriverId()));
	}

	@Override
	public void start(ApplicationManager appManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub
		
	}
	

}
