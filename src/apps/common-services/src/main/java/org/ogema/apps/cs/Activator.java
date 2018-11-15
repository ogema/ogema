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
package org.ogema.apps.cs;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.security.WebAccessManager;
import org.ogema.persistence.ResourceDB;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class Activator implements Application {

	@Reference
	ResourceDB db;

	@Reference
	private HttpService http;
	private ResourceAccess ra;

	private CommonServlet cs;

	private WebAccessManager wam;

	@Override
	public void start(ApplicationManager appManager) {
		this.wam = appManager.getWebAccessManager();
		cs = new CommonServlet(this.db, appManager.getResourceAccess());
		this.wam.registerWebResource("/service", cs);
		ra = appManager.getResourceAccess();
		ServletAndroid servlet = new ServletAndroid(ra);
		try {
			/*
			 * FIXME This registration avoids the security for web access by using the non-secure DefaultHttpContext.
			 * It's just an experimental facility and shouldn't be part of an official release.
			 */
			http.registerServlet("/servletAndroid", servlet, null, null);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (NamespaceException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop(AppStopReason reason) {
		http.unregister("/servletAndroid");
		this.wam.unregisterWebResource("/service");
	}
}
