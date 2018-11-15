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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.ogema.core.application.ApplicationManager;
import org.ogema.exam.latest.LatestVersionsTestBase;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Before execution of the tests, this class registers a new test app
 * with all import permissions and a service permission to register an 
 * Application. The corresponding {@link ApplicationManager} is available
 * via {@link #getApplicationManager()}. Another application manager with
 * unrestricted permissions is available via {@link #getUnrestrictedAppManager()}. 
 * <br>
 * Further permissions can be created using the methods in {@link SecurityTestUtils}.  
 */
public class SecurityTestBase extends LatestVersionsTestBase {
	
	// there is no general logout servlet in OGEMA; if needed, register one in a derived class,
	// using the registerLogoutService method below
	public final static String LOGOUT_SERVLET = "/servlettestlogout";
	public final static String BASE_URL = "http://localhost:" + HTTP_PORT;
	private final static HttpServlet logoutServlet = new HttpServlet() {

		private static final long serialVersionUID = 1L;
		
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			req.getSession().invalidate();
		}
		
	};
	private ApplicationManager appMan;

	public SecurityTestBase() {
		this(false);
	}
	
	public SecurityTestBase(boolean includeTestBundle) {
		super(includeTestBundle);
	}

	
	/**
	 * Use in inherited class' Before method
	 */
	protected void registerLogoutServlet() {
		getUnrestrictedAppManager().getWebAccessManager().registerWebResource(LOGOUT_SERVLET, logoutServlet);
	}
	
	/**
	 * Use in inherited class' After method
	 */
	protected void unregisterLogoutServlet() {
		try {
			getUnrestrictedAppManager().getWebAccessManager().unregisterWebResource(LOGOUT_SERVLET);
		} catch (Exception ignore) {}
	}
	
	@Before
	public void installAppAndGetAppManager() throws InvalidSyntaxException, BundleException, InterruptedException {
		appMan = SecurityTestUtils.installAppAndGetAppManager(ctx, 5, TimeUnit.SECONDS);
	}
	
	@After
	public void uninstallApp() throws BundleException {
		SecurityTestUtils.uninstallApp(appMan);
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
	 * @return
	 */
	public ApplicationManager getUnrestrictedAppManager() {
		return super.getApplicationManager();
	}

	@Configuration
	@Override
	public Option[] config() {
		Option[] sup = super.config();
		Option[] newOpt = new Option[sup.length+8];
		System.arraycopy(sup, 0, newOpt, 0, sup.length);
		newOpt[sup.length] = CoreOptions.frameworkProperty("org.osgi.framework.security").value("osgi");
		newOpt[sup.length+1] = CoreOptions.mavenBundle("org.ops4j.pax.tinybundles", "tinybundles", "3.0.0");
		newOpt[sup.length+2] = CoreOptions.mavenBundle("biz.aQute.bnd", "biz.aQute.bndlib", "3.5.0");
		newOpt[sup.length+3] = CoreOptions.mavenBundle("org.ogema.tests", "ogema-exam-security2", ogemaVersion);
		newOpt[sup.length+4] = CoreOptions.mavenBundle("org.apache.httpcomponents", "httpclient-osgi", "4.5.3");
		newOpt[sup.length+5] = CoreOptions.mavenBundle("org.apache.httpcomponents", "httpcore-osgi", "4.4.6");
		newOpt[sup.length+6] = CoreOptions.mavenBundle("commons-logging", "commons-logging", "1.1.3");
		newOpt[sup.length+7] = CoreOptions.systemProperty("org.ogema.xservletacces.enable").value("true");
//		newOpt[sup.length+8] = CoreOptions.mavenBundle("org.ogema.ref-impl", "framework-gui", ogemaVersion); // needed for the logout servlet
		return newOpt;
	}
	
}
