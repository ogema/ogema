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
package org.ogema.rest.servlet;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

// TODO RecordedDataServlet
@Component(specVersion = "1.2")
@Service(Application.class)
public class RestApp implements Application {

	@Reference
	HttpService http;
	@Reference
	private PermissionManager permMan;
	@Reference
	private AdministrationManager adminMan;

	private RestAccess restAcc;

	protected ApplicationManager appman;
	private boolean SECURITY_ENABLED;// "on".equalsIgnoreCase(System.getProperty("org.ogema.security", "off"));
	
	@Override
	public void start(ApplicationManager appManager) {
		appman = appManager;
		restAcc = new RestAccess(permMan, adminMan);
		SECURITY_ENABLED = permMan.isSecure();
		if (SECURITY_ENABLED && System.getSecurityManager() == null) {
			throw new Error("org.ogema.security=on, but security manager is null!");
		}
		RestServlet restServlet = new RestServlet(appManager, permMan, restAcc, SECURITY_ENABLED);
		try {
			http.registerServlet(RestServlet.alias, restServlet, null, null);
			appman.getLogger().info("REST servlet registered, security enabled: {}", SECURITY_ENABLED);
		} catch (ServletException | NamespaceException ex) {
			appman.getLogger().error("could not register servlet");
		}
		RestTypesServlet typesServlet  =new RestTypesServlet(appManager, permMan, restAcc, SECURITY_ENABLED);
		try {
			http.registerServlet(RestTypesServlet.alias, typesServlet, null, null);
			appman.getLogger().info("REST types servlet registered, security enabled: {}", SECURITY_ENABLED);
		} catch (ServletException | NamespaceException ex) {
			appman.getLogger().error("could not register servlet");
		}
		RestPatternServlet patternServlet = new RestPatternServlet(appManager, permMan, restAcc, SECURITY_ENABLED);
		try {
			http.registerServlet(RestPatternServlet.alias, patternServlet, null, null);
			appman.getLogger().info("REST pattern servlet registered, security enabled: {}", SECURITY_ENABLED);
		} catch (ServletException | NamespaceException ex) {
			appman.getLogger().error("could not register servlet");
		}
		String url = appman.getWebAccessManager().registerWebResourcePath("/rest-gui", "rest/gui");
		appManager.getLogger().info("Pattern debug page registered under url {}", url);
	}
	
	@Override
	public void stop(AppStopReason reason) {
		if (appman != null) 
			try {
				appman.getWebAccessManager().unregisterWebResourcePath("/rest-gui");
			} catch (Exception e) {/*ignore*/}
		appman = null;
		restAcc =null;
		if (http != null) {
			try {
				http.unregister(RestServlet.alias);
			} catch (Exception e) {/*ignore*/}
			try {
				http.unregister(RestTypesServlet.alias);
			} catch (Exception e) {/*ignore*/}
			try {
				http.unregister(RestPatternServlet.alias);
			} catch (Exception e) {/*ignore*/}
		}
	}
	
}
