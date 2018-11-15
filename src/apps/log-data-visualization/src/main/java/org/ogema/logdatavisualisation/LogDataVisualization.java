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
