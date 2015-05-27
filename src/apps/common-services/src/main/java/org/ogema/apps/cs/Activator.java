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

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.persistence.ResourceDB;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class Activator implements Application, BundleActivator {

	ResourceDB db;
	private HttpService http;
	private ResourceAccess ra;

	public void start(BundleContext bc) throws BundleException {
		db = (ResourceDB) bc.getService(bc.getServiceReference(ResourceDB.class.getName()));
		http = (HttpService) bc.getService(bc.getServiceReference(HttpService.class.getName()));
		bc.registerService(Application.class, this, null);
	}

	public void stop(BundleContext context) {
	}

	@Override
	public void start(ApplicationManager appManager) {

		new CommonServlet(appManager.getWebAccessManager(), this.db, appManager.getResourceAccess());
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
	}
}
