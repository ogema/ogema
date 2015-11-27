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
