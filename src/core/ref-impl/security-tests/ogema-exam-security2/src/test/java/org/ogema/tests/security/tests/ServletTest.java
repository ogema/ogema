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
package org.ogema.tests.security.tests;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tests.security.testbase.SecurityTestBase;
import org.ogema.tests.security.testbase.SecurityTestUtils;
import org.ogema.tests.security.testbase.servlet.TestClient;
import org.ogema.tests.security.testbase.servlet.TestServlet;
import org.ogema.tests.security.testbase.servlet.TestWebresource;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

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
		try (final TestWebresource webresource = new TestWebresource(getApplicationManager().getWebAccessManager());
				final TestServlet servlet = new TestServlet(getApplicationManager().getWebAccessManager());
				final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.init(new URI(BASE_URL + webresource.getPath()));
			final HttpResponse resp = client.sendGetInternal(new URI(BASE_URL + servlet.getPath()));
			Assert.assertNotEquals(servlet.getSecretPassword(), EntityUtils.toString(resp.getEntity()));
		}
	}
	
	@Test
	public void servletAccessWithoutOneTimePwFails()  throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
		final ApplicationManager appMan = getUnrestrictedAppManager();
		try (final TestServlet servlet = new TestServlet(getApplicationManager().getWebAccessManager());
				final TestClient client = new TestClient(appMan)) {
			client.login();
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + servlet.getPath()));
			Assert.assertNotEquals("Servlet access succeeded despite missing one time password",2,response.getStatusLine().getStatusCode() / 100);
		}
	}
	
	// tests one time passwords
	@Test
	public void servletSecurityWorks0() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager restrictedAppMan = getApplicationManager();
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestServlet servlet = new TestServlet(restrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(getUnrestrictedAppManager())) {
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
	@Test
	public void servletSecurityWorks1() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager unrestrictedAppMan = getUnrestrictedAppManager();
		final ApplicationManager restrictedAppMan = getApplicationManager();
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestServlet servlet = new TestServlet(unrestrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(unrestrictedAppMan)) {
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			// must not succeed, since we are not permitted to access the other app's servlet
			final HttpResponse resp = client.sendGetInternal(new URI(BASE_URL + servlet.getPath()));
			Assert.assertNotEquals("Unexpected status code " + resp.getStatusLine().getStatusCode(), 2, resp.getStatusLine().getStatusCode()/100);
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
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestServlet servlet = new TestServlet(unrestrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(unrestrictedAppMan)) {
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			final String response = client.sendGet(new URI(BASE_URL + servlet.getPath()));
			Assert.assertEquals("Unexpected servlet response", servlet.getSecretPassword(), response);
		}
	}
	
	/**
	 * Very similar to {@link #servletSecurityWorks2()}, but here the logged-in user does not have the permission to
	 * access a resource in the servlet's doGet method
	 * @throws InterruptedException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void servletSecurityWorks3() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager unrestrictedAppMan = getUnrestrictedAppManager();
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final Resource testResource = unrestrictedAppMan.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		SecurityTestUtils.addWebResourcePermission(restrictedAppMan, unrestrictedAppMan.getAppID().getBundle().getSymbolicName(), ctx);
		@SuppressWarnings("serial")
		final HttpServlet testServlet = new HttpServlet() {
			
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
				// throws SecurityException if logged-in user does not have permission to access the resource
				final Resource r = unrestrictedAppMan.getResourceAccess().getResource(testResource.getPath());
				resp.setContentType("text/plain");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(r.getPath());
			}
			
		};
		final String servletAddress = "/servletsectest_abc";
		unrestrictedAppMan.getWebAccessManager().registerWebResource(servletAddress, testServlet);
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(unrestrictedAppMan, false)) {
			SecurityTestUtils.addWebResourcePermission(client.getUser().getName(), null, ctx);
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			final HttpResponse resp = client.sendGetInternal(new URI(BASE_URL + servletAddress));
			Assert.assertNotEquals("Unexpected status code " + resp.getStatusLine().getStatusCode(), 2, resp.getStatusLine().getStatusCode()/100);
		} finally {
			unrestrictedAppMan.getWebAccessManager().unregisterWebResource(servletAddress);
			testResource.delete();
		}
	} 
	
	/**
	 * Very similar to {@link #servletSecurityWorks3()}, but here the logged-in user explicitly gets the permission to
	 * access a resource in the servlet's doGet method
	 * @throws InterruptedException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void servletSecurityWorks4() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager unrestrictedAppMan = getUnrestrictedAppManager();
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final Resource testResource = unrestrictedAppMan.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		SecurityTestUtils.addWebResourcePermission(restrictedAppMan, unrestrictedAppMan.getAppID().getBundle().getSymbolicName(), ctx);
		@SuppressWarnings("serial")
		final HttpServlet testServlet = new HttpServlet() {
			
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
				// throws SecurityException if logged-in user does not have permission to access the resource
				final Resource r = unrestrictedAppMan.getResourceAccess().getResource(testResource.getPath());
				resp.setContentType("text/plain");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(r.getPath());
			}
			
		};
		final String servletAddress = "/servletsectest_abc2";
		unrestrictedAppMan.getWebAccessManager().registerWebResource(servletAddress, testServlet);
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(unrestrictedAppMan, false)) {
			SecurityTestUtils.addWebResourcePermission(client.getUser().getName(), null, ctx);
			SecurityTestUtils.addResourcePermissions(client.getUser().getName(), ctx, testResource.getPath(), null, "read");
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			
			final String response = client.sendGet(new URI(BASE_URL + servletAddress));
			Assert.assertEquals("Unexpected servlet response", testResource.getPath(), response);
		} finally {
			unrestrictedAppMan.getWebAccessManager().unregisterWebResource(servletAddress);
			testResource.delete();
		}
	} 
	
	@Test
	public void restAccessFailsWithoutResourcePermission() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Room.class);
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource.getPath())));
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + "/rest/resources/" + r.getPath()));
			Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		}
		r.delete();
	}
	
	@Test
	public void restAccessSucceedsWithAppResourcePermission() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Room.class);
		SecurityTestUtils.addResourcePermission(ctx, r.getPath(), null, restrictedAppMan, ResourcePermission.READ);
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource.getPath())));
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + "/rest/resources/" + r.getPath()));
			Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
		}
		r.delete();
	}
	
	/**
	 * Like {@link #restAccessSucceedsWithAppResourcePermission()}, but here the logged-in user does not have the permission to access the resource
	 */
	@Test
	public void restAccessFailsWithMissingUserPermission() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final ApplicationManager unrestrictedAppMan = getUnrestrictedAppManager();
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final TemperatureSensor resource = unrestrictedAppMan.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
		SecurityTestUtils.addWebResourcePermission(restrictedAppMan, unrestrictedAppMan.getAppID().getBundle().getSymbolicName(), ctx);
		SecurityTestUtils.addResourcePermission(ctx, null, TemperatureSensor.class.getName(), restrictedAppMan, "*");
		try (final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
				final TestClient client = new TestClient(unrestrictedAppMan, false)) {
			SecurityTestUtils.addWebResourcePermission(client.getUser().getName(), null, ctx);
			client.login();
			Assert.assertTrue("Client initialization failed",client.init(new URI(BASE_URL + webresource.getPath())));
			final HttpResponse resp = client.sendRESTRequest(resource.getPath(), HTTP_PORT);
			Assert.assertNotEquals("Unexpected status code " + resp.getStatusLine().getStatusCode(), 2, resp.getStatusLine().getStatusCode()/100);
		} finally {
			resource.delete();
		}
	}
	
	@Test
	public void restAccessPermissionsDoNotInterfere0() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException, InvalidSyntaxException, BundleException {
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Room.class);
		SecurityTestUtils.addResourcePermission(ctx, r.getPath(), null, restrictedAppMan, ResourcePermission.READ);
		final ApplicationManager restrictedAppMan2 = SecurityTestUtils.installAppAndGetAppManager(ctx, 5, TimeUnit.SECONDS);
		final TestWebresource webresource2 = new TestWebresource(restrictedAppMan2.getWebAccessManager());
		try (final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource.getPath())));
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + "/rest/resources/" + r.getPath()));
			// restrictedAppMan has the permission to access the resource
			Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
		}
		try (final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource2.getPath())));
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + "/rest/resources/" + r.getPath()));
			// restrictedAppMan2 does not have permission to access the resource
			Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		}
		r.delete();
		SecurityTestUtils.uninstallApp(restrictedAppMan2);
	}
	
	@Test
	public void restAccessPermissionsDoNotInterfere1() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException, InvalidSyntaxException, BundleException {
		final ApplicationManager restrictedAppMan = getApplicationManager();
		final TestWebresource webresource = new TestWebresource(restrictedAppMan.getWebAccessManager());
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(newResourceName(), Room.class);
		SecurityTestUtils.addResourcePermission(ctx, r.getPath(), null, restrictedAppMan, ResourcePermission.READ);
		final ApplicationManager restrictedAppMan2 = SecurityTestUtils.installAppAndGetAppManager(ctx, 5, TimeUnit.SECONDS);
		final TestWebresource webresource2 = new TestWebresource(restrictedAppMan2.getWebAccessManager());
		try (final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource2.getPath())));
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + "/rest/resources/" + r.getPath()));
			System.out.println("  response: " + response.getStatusLine().getStatusCode());
			// restrictedAppMan2 does not have permission to access the resource
			Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		}
		try (final TestClient client = new TestClient(getUnrestrictedAppManager())) {
			client.login();
			Assert.assertTrue("Client initialization failed", client.init(new URI(BASE_URL + webresource.getPath())));
			final HttpResponse response = client.sendGetInternal(new URI(BASE_URL + "/rest/resources/" + r.getPath()));
			System.out.println("  response: " + response.getStatusLine().getStatusCode());
			// restrictedAppMan has the permission to access the resource
			Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
		}
		r.delete();
		SecurityTestUtils.uninstallApp(restrictedAppMan2);
	}
	
	
}
