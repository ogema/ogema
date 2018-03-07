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
package org.ogema.tests.security.tests;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.tests.security.testbase.SecurityTestBase;
import org.ogema.tests.security.testbase.SecurityTestUtils;
import org.ogema.tests.security.testbase.servlet.TestClient;
import org.ogema.tests.security.testbase.servlet.TestServlet;
import org.ogema.tests.security.testbase.servlet.TestWebresource;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests OGEMA web security mechanisms
 */
@ExamReactorStrategy(PerClass.class)
public class ServletTest extends SecurityTestBase {
	
	public ServletTest() {
		super(false);
	}
	
	@Before
	public void registerLogoutServlet() {
		super.registerLogoutServlet();
	}
	
	@After
	public void unregisterLogoutServlet() {
		super.unregisterLogoutServlet();
	}
	
	@Test
	public void loginRequiredForWebAccess() throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
		final TestServlet servlet = new TestServlet(getApplicationManager().getWebAccessManager());
		final TestWebresource webresource = new TestWebresource(getApplicationManager().getWebAccessManager());
		try (final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.init(new URI(BASE_URL + webresource.getPath()));
			final HttpResponse resp = client.sendGetInternal(new URI(BASE_URL + servlet.getPath()));
			Assert.assertNotEquals(servlet.getSecretPassword(), EntityUtils.toString(resp.getEntity()));
		} finally {
			servlet.close();
		}
	}
	
	@Test
	public void servletAccessWithoutOneTimePwFails()  throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
		final ApplicationManager appMan = getUnrestrictedAppManager();
		final TestServlet servlet = new TestServlet(getApplicationManager().getWebAccessManager());
		try (final TestClient client = new TestClient(appMan)) {
			client.login();
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + servlet.getPath()));
			Assert.assertNotEquals("Servlet access succeeded despite missing one time password",2,response.getStatusLine().getStatusCode() / 100);
		} finally {
			servlet.close();
		}
	}
	
	// tests one time passwords
	@Test
	public void servletSecurityWorks0() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final TestServlet servlet = new TestServlet(restrictedAppMan.getWebAccessManager());
		// FIXME no permission required to register a web resource?
		final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
		try (final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource.getPath())));
			final String response = client.sendGet(new URI(BASE_URL + servlet.getPath()));
			Assert.assertEquals("Unexpected servlet response", servlet.getSecretPassword(), response);
		} 
	}
	
	/**
	 * Very similar to {@link #servletSecurityWorks0()}, but here the servlet is registered by another 
	 * app; the request is sent with the One-time-user/password of the restricted app, which should not
	 * be allowed to access the servlet.
	 * @throws InterruptedException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	//@Ignore("not implemented yet")
	@Test
	public void servletSecurityWorks1() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager unrestrictedAppMan = getUnrestrictedAppManager();
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final TestServlet servlet = new TestServlet(unrestrictedAppMan.getWebAccessManager());
		final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
		try (final TestClient client = new TestClient(unrestrictedAppMan)) {
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			// must not succeed, since we are not permitted to access the other app's servlet
			final HttpResponse resp = client.sendGetInternal(new URI(BASE_URL + servlet.getPath()));
			Assert.assertNotEquals("Unexpected status code " + resp.getStatusLine().getStatusCode(), 2, resp.getStatusLine().getStatusCode()/100);
		} finally {
			servlet.close();
		}
	}
	
	/**
	 * Very similar to {@link #servletSecurityWorks1()}, but here the restricted app explicitly gets the permission
	 * to access web resources from the unrestricted app.  
	 * @throws InterruptedException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void servletSecurityWorks2() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager unrestrictedAppMan = getUnrestrictedAppManager();
		final ApplicationManager restrictedAppMan = getApplicationManager();
		SecurityTestUtils.addWebResourcePermission(restrictedAppMan, unrestrictedAppMan.getAppID().getBundle().getSymbolicName(), ctx);
		final TestServlet servlet = new TestServlet(unrestrictedAppMan.getWebAccessManager());
		final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
		try (final TestClient client = new TestClient(unrestrictedAppMan)) {
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			final String response = client.sendGet(new URI(BASE_URL + servlet.getPath()));
			Assert.assertEquals("Unexpected servlet response", servlet.getSecretPassword(), response);
		} finally {
			servlet.close();
		}
	}
	

}
