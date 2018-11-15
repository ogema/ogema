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
package org.ogema.tests.security.testbase.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.administration.UserAccount;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.security.WebAccessManager;
import org.ogema.tests.security.testbase.SecurityTestBase;
import org.ogema.tests.security.testbase.SecurityTestUtils;

import org.junit.Assert;

/**
 * Mocks a user session in a browser. Proceed in the following order:
 * <ul>
 * 	<li>Step 1: create an instance; this will automatically create a natural user with all permissions
 *  <li>Step 2: call {@link #login()}; the login will be done for the associated natural user
 *  <li>Step 3: call {@link #init(URI)}; pass the url of a registered web resource -&gt; the client will 
 *  	inherit the app id of the app the web resource belongs to, using the one-time-password mechanism
 *  <li>Step 4: use {@link #sendGet(URI)} to access any web resources and servlets registered 
 *  	by apps via their {@link WebAccessManager}
 *  <li>Step 5: close the client; for instance, by instantiating it in a "try-with-resources" statement;
 *  	the associated user is logged out and will be deleted
 * </ul>
 */
public class TestClient implements AutoCloseable {
	
	private final static AtomicInteger userCnt = new AtomicInteger(0);
	private final static String loginServlet = "/ogema/login";
//	private final static String logoutServlet = "/apps/ogema/framework/gui?action=logout";
	private final UserAccount user;
	private final AdministrationManager admin;
	private volatile Header[] cookies;
	private volatile String[] userPw; // OTP

	public TestClient(ApplicationManager appMan) throws InterruptedException {
		this(appMan, true);
	}
	
	public TestClient(ApplicationManager appMan, boolean privileged) throws InterruptedException {
		this.admin = appMan.getAdministrationManager();
		this.user = privileged ? SecurityTestUtils.createPrivilegedNaturalUser("testclient" + userCnt.getAndIncrement(), appMan) :
				SecurityTestUtils.createUser("testclient" + userCnt.getAndIncrement(), appMan, true, false);
	}
	
	/**
	 * Send a GET request, verify that status code is 2XX, and return the response as a string
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException 
	 * @throws IllegalStateException if the client is not logged in (see {@link #login()}.
	 * @throws AssertionError if we get an unexpected status code
	 */
	public String sendGet(final URI url) throws ClientProtocolException, IOException, URISyntaxException {
		final HttpResponse resp = sendGetInternal(url);
		Assert.assertEquals("Unexpected status code " + resp.getStatusLine().getStatusCode(), 2, resp.getStatusLine().getStatusCode()/100);
		return EntityUtils.toString(resp.getEntity());
	}
	
	/**
	 * Send a GET request and return the response; typically {@link #sendGet(URI)} should be used instead.
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException 
	 * @throws IllegalStateException if the client is not logged in (see {@link #login()}.
	 */
	public HttpResponse sendGetInternal(final URI url) throws ClientProtocolException, IOException, URISyntaxException {
		final Request get = Request.Get(appendAppAuthentication(url));
		get.setHeaders();
		if (cookies != null) {
			for (Header cookie: cookies)
				get.addHeader(cookie);
		}
		return get.execute().returnResponse();
	}
	
	public HttpResponse sendRESTRequest(String resource, final int httpPort) throws URISyntaxException, ClientProtocolException, IOException {
		if (!resource.isEmpty() && resource.charAt(0) == '/')
			resource = resource.substring(1);
		final URI url = new URIBuilder()
				.setScheme("http")
				.setPort(httpPort)
				.setHost("localhost")
				.setPath("rest/resources/" + resource)
				.build();
		final Request get = Request.Get(appendAppAuthentication(url));
		get.setHeaders();
		if (cookies != null) {
			for (Header cookie: cookies)
				get.addHeader(cookie);
		}
		return get.execute().returnResponse();
	}
	
	private final URI appendAppAuthentication(final URI uri) throws URISyntaxException, UnsupportedEncodingException {
		if (userPw == null)
			return uri;
		return new URIBuilder(uri)
				.addParameter("user", userPw[0])
				.addParameter("pw", userPw[1])
				.build();
	}
	
	/**
	 * Send a GET request and extract the One-Time-User and One-Time-Password
	 * @param url
	 * 		must be a valid URL to a registered Html web resource, so that the one time user and pw 
	 * 		will be injected
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public boolean init(final URI url) throws ClientProtocolException, IOException, URISyntaxException {
		final HttpResponse resp = sendGetInternal(url);
		if (resp.getStatusLine().getStatusCode() / 100 != 2)
			return false;
		userPw = extractUserAndPw(EntityUtils.toString(resp.getEntity(), "UTF-8"));
		return userPw != null;
	}
	
	public boolean isLoggedIn() {
		return cookies != null;
	}
	
	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AssertionError if login failed
	 */
	public void login() throws ClientProtocolException, IOException {
		final Request login = Request.Post(SecurityTestBase.BASE_URL + loginServlet)
			.body(new StringEntity("user="+user.getName() + "&pw="+user.getName(), "UTF-8"))
			.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		final HttpResponse response = login.execute().returnResponse(); 
		Assert.assertEquals("Login failed", 2, response.getStatusLine().getStatusCode() / 100);
		cookies = response.getHeaders("Set-Cookie");
		Assert.assertNotNull("Missing cookies header in login response", cookies);
		Assert.assertTrue("Cookies header is empty in login response", cookies.length > 0);
	}
	
	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException 
	 * @throws RuntimeException if login failed
	 */
	// FIXME client will need permission to access the logout servlet!
	public void logout() throws ClientProtocolException, IOException, URISyntaxException {
		final HttpResponse response = sendGetInternal(new URI(SecurityTestBase.BASE_URL + SecurityTestBase.LOGOUT_SERVLET));
		if (response.getStatusLine().getStatusCode()/100 != 2)
			throw new RuntimeException("Logout failed: " + EntityUtils.toString(response.getEntity()));
	}
	
	public UserAccount getUser() {
		return user;
	}
	
	// we assume here an input string of the form TestWebresource.WEB_RESOURCE
	private final static String[] extractUserAndPw(final String html) {
		final int idx = html.indexOf("<script");
		if (idx < 0 || idx == html.length()-7)
			return null;
		final int idxClosed = html.indexOf('>', idx);
		if (idxClosed < 0 || idxClosed == html.length()-1)
			return null;
		final int idxEnd = html.indexOf("</script>", idxClosed);
		if (idxEnd < 0)
			return null;
		final String javascript = html.substring(idxClosed+1, idxEnd);
		final String user = extractVar(javascript, "usr");
		if (user == null)
			return null;
		final String pw = extractVar(javascript, "pwd");
		if (pw == null)
			return null;		
		return new String[] {user,pw};
	}
	
	private final static String extractVar(String javascript, final String varName) {
		final int idx = javascript.indexOf(varName + "=");
		if (idx < 0)
			return null;
		final int idx0 = javascript.indexOf('\'',idx+ varName.length());
		final int idx1 = javascript.indexOf('\"',idx+ varName.length());
		if (idx0 <0 && idx1 <0)
			return null;
		final boolean swtch = idx0 > 0 && idx0 < idx1;
		final int idxEnd = javascript.indexOf(swtch? '\'' : '\"', swtch ? idx0+1 : idx1+1);
		if (idxEnd < 0)
			return null;
		return javascript.substring( swtch ? idx0+1 : idx1+1, idxEnd);
	}
	
	@Override
	public void close() {
		try {
			logout();
		} catch (Exception ignore) {} // might not even have logged in properly 
		try {
			admin.removeUserAccount(user.getName());
		} catch (Exception ignore) {}
	}
	
}
