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
package org.ogema.apps.swtch;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;

@Component(specVersion = "1.2")
@Service(Application.class)
public class BasicSwitchGui implements Application {

	protected ApplicationManager am;

	private String webResourceBrowserPath;
	private String servletPath;
	private TestResourceManagement testResources;

	@Override
	public void start(ApplicationManager am) {
		this.am = am;

		am.getLogger().debug("BasicSwitchGui started");
        String webResourcePackage = "org.ogema.apps.swtch";
        webResourcePackage = webResourcePackage.replace(".", "/");
        String appNameLowerCase = "BasicSwitchGui";
        appNameLowerCase = appNameLowerCase.toLowerCase();
        //path to find the index.html /ogema/<this app name>/index.html
        webResourceBrowserPath = "/ogema/" + appNameLowerCase;
        //package/path to find the resources inside this application
        String webResourcePackagePath = webResourcePackage + "/gui";
        //path for the http servlet /apps/ogema/<this app name>
        servletPath = "/apps/ogema/" + appNameLowerCase;
        am.getWebAccessManager().registerWebResource(webResourceBrowserPath,webResourcePackagePath);
        am.getWebAccessManager().registerWebResource(servletPath, new BasicSwitchGuiServlet(am));
        try {
        	if (Boolean.getBoolean("org.ogema.gui.usecdn")) {
        		am.getWebAccessManager().registerStartUrl(webResourceBrowserPath + "/index2.html");
        	}
        } catch (SecurityException ok) {}
        try {
	        Boolean testRes = Boolean.getBoolean("org.ogema.apps.createtestresources");
	        this.testResources = testRes ? new TestResourceManagement(am) : null;
        } catch (SecurityException ok) {}
	}

	@Override
	public void stop(AppStopReason reason) {
		if (am != null) {
			am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
			am.getWebAccessManager().unregisterWebResource(servletPath);
		}
		if (testResources != null) {
			testResources.close();
			testResources = null;
		}
		am = null;
	}

}
