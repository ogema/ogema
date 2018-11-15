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
package org.ogema.webresourcemanager.impl.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.ogema.webresourcemanager.impl.internal.bookmark.Bookmark;
import org.ogema.webresourcemanager.impl.internal.layout.Basepage;
import org.ogema.webresourcemanager.impl.internal.websession.OgemaAuthentificatedWebsession;
import org.ops4j.pax.wicket.internal.BundleDelegatingClassResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class OgemaWicketApplication extends AuthenticatedWebApplication {

	public OgemaWicketApplication() {
		System.out.println("    OGEMA Wicket app initialised....!!");
	}	
	
	private final void setClassResolver() {
		BundleContext ctx = AccessController.doPrivileged(new PrivilegedAction<BundleContext>() {

			@Override
			public BundleContext run() {
				return FrameworkUtil.getBundle(OgemaWicketApplication.class).getBundleContext();
			}
			
		});
		getApplicationSettings().setClassResolver(new BundleDelegatingClassResolver(ctx, getName()));

	}
	
	@Override
	protected void internalInit() {
		super.internalInit();
		setClassResolver(); 
	}

	@Override
	protected void init() {
		super.init();
		System.setProperty("Wicket_HeaderRenderStrategy",
				"org.apache.wicket.markup.renderStrategy.ParentFirstHeaderRenderStrategy");

		// useSSL();
		getDebugSettings().setAjaxDebugModeEnabled(false);
		getApplicationSettings().setPageExpiredErrorPage(getHomePage());
		getApplicationSettings().setUploadProgressUpdatesEnabled(true);
		mountPage("deeplink", Bookmark.class);
		allowFiles();

	}

	private void allowFiles() {
		SecurePackageResourceGuard guard = new SecurePackageResourceGuard();
		guard.addPattern("+*.html");
		guard.addPattern("+*.map");
		guard.addPattern("+*.css");
		guard.addPattern("+*.json");
		guard.addPattern("+*.svg");
		guard.addPattern("+*.png");
		guard.addPattern("+*.xml");
	}

/*	private void useSSL() {
		try {
			final String http = System.getProperty("org.osgi.service.http.port", "8080");
			final String https = System.getProperty("org.osgi.service.http.port.secure", "443");
			final int port = Integer.valueOf(http);
			final int sslPort = Integer.valueOf(https);
			HttpsConfig config = new HttpsConfig(port, sslPort);
			IRequestMapper ipRequestMapper = getRootRequestMapper();
			HttpsMapper rootRequestMapper = new HttpsMapper(ipRequestMapper, config);
			setRootRequestMapper(rootRequestMapper);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
*/

	@Override
	public RuntimeConfigurationType getConfigurationType() {
		String type = System.getProperty("org.ogema.webresourcemanager.RuntimeConfigurationType", "unknown");

		if ("DEVELOPMENT".equals(type)) {
			return RuntimeConfigurationType.DEVELOPMENT;
		}

		return RuntimeConfigurationType.DEPLOYMENT;
	}

	@Override
	public Class<? extends WebPage> getHomePage() {
		return Basepage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
		OgemaAuthentificatedWebsession session = new OgemaAuthentificatedWebsession(request);
		return session;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return Basepage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return OgemaAuthentificatedWebsession.class;
	}

}
