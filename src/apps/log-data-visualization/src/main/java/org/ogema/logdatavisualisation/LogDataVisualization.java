/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.logdatavisualisation;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

@Component(specVersion = "1.1", immediate = true)
@Service(Application.class)
public class LogDataVisualization implements Application {

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

		String webResourcePackage = "org.ogema.logdatavisualisation";
		webResourcePackage = webResourcePackage.replace(".", "/");

		String appNameLowerCase = "LogDataVisualization";
		appNameLowerCase = appNameLowerCase.toLowerCase();

		//path to find the index.html /ogema/<this app name>/index.html
		webResourceBrowserPath = "/ogema/" + appNameLowerCase;
		//package/path to find the resources inside this application
		String webResourcePackagePath = webResourcePackage + "/gui";
		//path for the http servlet /apps/ogema/<this app name>
		servletPath = "/apps/ogema/" + appNameLowerCase;

		appManager.getWebAccessManager().registerWebResource(webResourceBrowserPath, webResourcePackagePath);
		appManager.getWebAccessManager().registerWebResource(servletPath, new LogDataVisualizationServlet(appManager));

	}

	@Override
	public void stop(AppStopReason reason) {
		appMan.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		appMan.getWebAccessManager().unregisterWebResource(servletPath);
	}

}
