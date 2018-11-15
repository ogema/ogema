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
package org.ogema.tools;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;

@Component(specVersion = "1.2", immediate=true)
@Service(Application.class)
public class ResourceFlash implements Application {

	protected ApplicationManager am;
    private String webResourceBrowserPath;
    private String servletPath;

	@Override
	public void start(ApplicationManager am) {
		this.am = am;
		 am.getLogger().debug("ResourceFlashApp started");
        String webResourcePackage = "org.ogema.tools";
        webResourcePackage = webResourcePackage.replace(".", "/");
        String appNameLowerCase = "ResourceFlash";
        appNameLowerCase = appNameLowerCase.toLowerCase();
        //path to find the index.html /ogema/<this app name>/index.html
        webResourceBrowserPath = "/org/ogema/tests/" + appNameLowerCase;
        String webResourcePackagePath = webResourcePackage + "/gui";
        servletPath = "/org/ogema/tests/servlets/" + appNameLowerCase;

        am.getWebAccessManager().registerWebResource(webResourceBrowserPath,webResourcePackagePath);
		am.getWebAccessManager().registerWebResource(servletPath,new ResourceFlashServlet(am));
	}

    @Override
	public void stop(AppStopReason reason) {
        if (am != null) {
        	am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
        	am.getWebAccessManager().unregisterWebResource(servletPath);
        }
        am = null;
	}

}