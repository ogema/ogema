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