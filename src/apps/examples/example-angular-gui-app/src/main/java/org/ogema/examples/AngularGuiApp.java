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
package org.ogema.examples;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

@Component(specVersion = "1.2", immediate=true)
@Service(Application.class)
public class AngularGuiApp implements Application {

	protected OgemaLogger logger;
	protected ApplicationManager appMan;
	protected ResourceManagement resMan;
	protected ResourceAccess resAcc;
	private String webResourceBrowserPath;
	private String servletPath;

	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		this.logger = appManager.getLogger();
		this.resMan = appManager.getResourceManagement();
		this.resAcc = appManager.getResourceAccess();

		logger.debug("{} started", getClass().getName());

		String webResourcePackage = "org.ogema.examples";
		webResourcePackage = webResourcePackage.replace(".", "/");
		String appNameLowerCase = "AngularGuiApp";
		appNameLowerCase = appNameLowerCase.toLowerCase();
		//path where to find the index.html /ogema/<this app name>/index.html
		webResourceBrowserPath = "/ogema/" + appNameLowerCase;
		//package/path where to find the resources inside this application
		String webResourcePackagePath = webResourcePackage + "/gui";
		//path for the http servlet /apps/ogema/<this app name>
		servletPath = "/apps/ogema/" + appNameLowerCase;

		// register GUI and servlet used by GUI
		appManager.getWebAccessManager().registerWebResource(webResourceBrowserPath,webResourcePackagePath);
		appManager.getWebAccessManager().registerWebResource(servletPath,new AngularGuiAppServlet(appMan));
		
		
	}

    @Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		appMan.getWebAccessManager().unregisterWebResource(servletPath);
	}

}