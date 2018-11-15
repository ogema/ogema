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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.Authenticator;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourcePermission;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.model.Resource;
import org.ogema.model.locations.Room;
import org.ogema.tests.security.testbase.SecurityTestBase;
import org.ogema.tests.security.testbase.SecurityTestUtils;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.junit.Assert;

/**
 * Tests OGEMA REST security mechanisms.
 * Note: One-time-password access to the REST interface for apps is tested in the ServletTest class.
 */
@ExamReactorStrategy(PerClass.class)
public class RestTest extends SecurityTestBase {
	
	public RestTest() {
		super(false);
	}
	
	private HttpResponse sendRequest(String resource, final String user) throws ClientProtocolException, IOException, URISyntaxException {
		if (!resource.isEmpty() && resource.charAt(0) == '/')
			resource = resource.substring(1);
		final URI url = new URIBuilder()
				.setScheme("http")
				.setPort(HTTP_PORT)
				.setHost("localhost")
				.setPath("rest/resources/" + resource)
				.addParameter("user", user)
				.addParameter("pw", user)
				.build();
		final Request get = Request.Get(url);
		return get.execute().returnResponse();
	}
	
	private HttpResponse sendRequest(String resource, final Map<String,String> params) throws ClientProtocolException, IOException, URISyntaxException {
		if (!resource.isEmpty() && resource.charAt(0) == '/')
			resource = resource.substring(1);
		final URIBuilder builder = new URIBuilder()
				.setScheme("http")
				.setPort(HTTP_PORT)
				.setHost("localhost")
				.setPath("rest/resources/" + resource);
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				builder.addParameter(entry.getKey(), entry.getValue());
			}
		}
		final Request get = Request.Get(builder.build());
		return get.execute().returnResponse();
	}
	
	@Test
	public void restAccessWithoutResourcePermissionFails() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final String res = newResourceName();
		String userName = newResourceName();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(res, Room.class);
		// unprivileged natural user
		final UserAccount user = SecurityTestUtils.createUser(userName, getUnrestrictedAppManager(), false, false);
		final HttpResponse response = sendRequest(res, user.getName());
		System.out.println("   Response: " + response.getStatusLine().getStatusCode());
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		r.delete();
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(user.getName());
	}
	
	@Test
	public void restAccessRequiresResourcePermission() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final String res0 = newResourceName();
		final String res1 = newResourceName();
		String userName = newResourceName();
		final Resource r0 = getUnrestrictedAppManager().getResourceManagement().createResource(res0, Room.class);
		final Resource r1 = getUnrestrictedAppManager().getResourceManagement().createResource(res1, Room.class);
		// unprivileged natural user
		final UserAccount user = SecurityTestUtils.createUser(userName, getUnrestrictedAppManager(), false, false);
		SecurityTestUtils.addResourcePermissions(user.getName(), ctx, res1, null, ResourcePermission.READ);
		final HttpResponse response0 = sendRequest(res0, user.getName());
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response0.getStatusLine().getStatusCode());
		final HttpResponse response1 = sendRequest(res1, user.getName());
		Assert.assertEquals(HttpServletResponse.SC_OK, response1.getStatusLine().getStatusCode());
		r0.delete();
		r1.delete();
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(user.getName());
	}
	
	@Test
	public void restAccessForSubresourceWorks() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final String res = newResourceName();
		String userName = newResourceName();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().<Room> createResource(res, Room.class).name().create();
		// unprivileged natural user
		final UserAccount user = SecurityTestUtils.createUser(userName, getUnrestrictedAppManager(), false, false);
		SecurityTestUtils.addResourcePermissions(user.getName(), ctx, r.getParent().getPath(), null, ResourcePermission.READ);
		final HttpResponse response0 = sendRequest(r.getPath(), user.getName());
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response0.getStatusLine().getStatusCode());
		r.getParent().delete();
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(user.getName());
	}
	
	@Test
	public void usersDontInterfere0() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final String res = newResourceName();
		String userName0 = newResourceName();
		String userName1 = newResourceName();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(res, Room.class);
		// unprivileged natural user
		final UserAccount userPriv = SecurityTestUtils.createUser(userName0, getUnrestrictedAppManager(), false, true);
		final UserAccount userUnpriv = SecurityTestUtils.createUser(userName1, getUnrestrictedAppManager(), false, false);

		final HttpResponse response = sendRequest(res, userPriv.getName());
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
		
		final HttpResponse response2 = sendRequest(res, userUnpriv.getName());
		Assert.assertEquals("REST access should have been denied (403 = Forbidden)", 
				HttpServletResponse.SC_FORBIDDEN, response2.getStatusLine().getStatusCode());
		r.delete();
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(userPriv.getName());
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(userUnpriv.getName());
	}
	
	@Test
	public void usersDontInterfere1() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final String res = newResourceName();
		String userName0 = newResourceName();
		String userName1 = newResourceName();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(res, Room.class);
		// unprivileged natural user
		final UserAccount userPriv = SecurityTestUtils.createUser(userName0, getUnrestrictedAppManager(), false, true);
		final UserAccount userUnpriv = SecurityTestUtils.createUser(userName1, getUnrestrictedAppManager(), false, false);
			
		final HttpResponse response = sendRequest(res, userUnpriv.getName());
		Assert.assertEquals("REST access should have been denied (403 = Forbidden)", 
				HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		
		final HttpResponse response2 = sendRequest(res, userPriv.getName());
		Assert.assertEquals("REST access should have been granted", 
				HttpServletResponse.SC_OK, response2.getStatusLine().getStatusCode());
		r.delete();
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(userPriv.getName());
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(userUnpriv.getName());
	}
	
	/**
	 * Tests an internal-api functionality
	 */
	@Test
	public void customAuthenticatorWorks() throws InterruptedException, ClientProtocolException, IOException, URISyntaxException {
		final String paramKey = "customAuthKey";
		final String res = newResourceName();
		String userName0 = newResourceName();
		String userName1 = newResourceName();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(res, Room.class);
		final UserAccount user0 = SecurityTestUtils.createUser(userName0, getUnrestrictedAppManager(), false, false);
		final UserAccount user1 = SecurityTestUtils.createUser(userName1, getUnrestrictedAppManager(), false, false);
		SecurityTestUtils.addResourcePermissions(userName1, ctx, res, null, "*");
		final Authenticator customAuth = new Authenticator() {
			
			@Override
			public String authenticate(HttpServletRequest req) {
				return req.getParameter(paramKey);
			}
		};
		final Dictionary<String, Object> props = new Hashtable<>(4);
		props.put(Authenticator.AUTHENTICATOR_ID, "test");
		ctx.registerService(Authenticator.class, customAuth, props);

		boolean success = false;
		// service might not be available immediately, hence we retry a few times
		for (int i=0; i < 5; i++) {
			final HttpResponse resp = sendRequest(res, Collections.singletonMap(paramKey, userName1));
			success = resp.getStatusLine().getStatusCode() / 100 == 2;
			if (success)
				break;
			Thread.sleep(1000);
		}
		Assert.assertTrue("Custom authenticator failed", success);
		final HttpResponse resp = sendRequest(res, Collections.singletonMap(paramKey, userName0));
		// user0 does not have permission to access res
		Assert.assertNotEquals("Unexpectedly permitted access", 2, resp.getStatusLine().getStatusCode() / 100);
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(user0.getName());
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(user1.getName());
		r.delete();
	}

	/**
	 * Tests an internal-api functionality
	 */
	@Test
	public void authenticatorDisablingWorks() throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
		final String res = newResourceName();
		String userName1 = newResourceName();
		final Resource r = getUnrestrictedAppManager().getResourceManagement().createResource(res, Room.class);
		final UserAccount user1 = SecurityTestUtils.createUser(userName1, getUnrestrictedAppManager(), false, false);
		SecurityTestUtils.addResourcePermissions(userName1, ctx, res, null, "*");
		final AccessManager accMan = SecurityTestUtils.getService(ctx, PermissionManager.class).getAccessManager();;
		accMan.removeSupportedAuthenticator(userName1, Authenticator.DEFAULT_USER_PW_ID);
		final HttpResponse response = sendRequest(res, userName1);
		Assert.assertEquals("REST access should have been denied (403 = Forbidden)", 
				HttpServletResponse.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		accMan.setSupportedAuthenticators(userName1, null);
		final HttpResponse response2 = sendRequest(res, userName1);
		Assert.assertEquals("REST access should have been granted (200 = OK)", 
				HttpServletResponse.SC_OK, response2.getStatusLine().getStatusCode());
		getUnrestrictedAppManager().getAdministrationManager().removeUserAccount(user1.getName());
		r.delete();
	}
	
}
