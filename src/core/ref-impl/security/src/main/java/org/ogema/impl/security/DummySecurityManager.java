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
package org.ogema.impl.security;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.AppPermissionFilter;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourceAccessRights;
import org.ogema.accesscontrol.UserRightsProxy;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.useradmin.User;

/**
 * @author Zekeriya Mansuroglu
 *
 */
public class DummySecurityManager implements AccessManager, PermissionManager, WebAccessManager, HttpContext {

	private final HttpService http;
	private final ResourceAccessRights allAccess;
	private AdministrationManager admin;

	public DummySecurityManager(HttpService http, AdministrationManager admin) {
		this.http = http;
		this.admin = admin;
		this.allAccess = ResourceAccessRights.ALL_RIGHTS;
		this.contextRegs = new ConcurrentHashMap<>();

	}

	public DummySecurityManager(BundleContext bc) {
		this.http = (HttpService) bc.getService(bc.getServiceReference(HttpService.class.getName()));
		this.allAccess = ResourceAccessRights.ALL_RIGHTS;
		this.contextRegs = new ConcurrentHashMap<>();

	}

	@Override
	public AccessManager getAccessManager() {
		return this;
	}

	@Override
	public boolean installPerms(AppPermission perm) {
		return true;
	}

	@Override
	public AppPermission getPolicies(AppID app) {
		return null;
	}

	@Override
	public void setDefaultPolicies(AppPermission pInfos) {
	}

	@Override
	public boolean handleSecurity(Permission perm) {
		return true;
	}

	@Override
	public WebAccessManager getWebAccess() {
		return this;
	}

	@Override
	public Object getSystemPermissionAdmin() {
		return null;
	}

	@Override
	public AppPermission createAppPermission(String uri) {
		return null;
	}

	@Override
	public boolean checkCreateResource(Application app, Class<? extends Resource> type, String name, int count) {
		return true;
	}

	@Override
	public ResourceAccessRights getAccessRights(Application app, TreeElement el) {
		return allAccess;
	}

	@Override
	public boolean checkDeleteResource(Application app, TreeElement te) {
		return true;
	}

	@Override
	public boolean createUser(String userName, boolean natural) {
		return true;
	}

	@Override
	public void removeUser(String userName) {
	}

	@Override
	public User getUser(String userName) {
		return null;
	}

	@Override
	public List<String> getAllUsers() {
		return null;
	}

	@Override
	public void setNewPassword(String user, String newPassword) {
	}

	@Override
	public void setCredential(String user, String credential, String value) {
	}

	@Override
	public List<AppID> getAppsPermitted(String user) {
		return new ArrayList<AppID>();
	}

	@Override
	public boolean isAppPermitted(User user, AppID app) {
		return true;
	}

	@Override
	public boolean authenticate(String remoteUser, String remotePasswd, boolean isnatural) {
		return true;
	}

	@Override
	public void registerApp(AppID id) {
	}

	@Override
	public void unregisterApp(AppID id) {
	}

	@Override
	public void unregisterWebResource(String alias) {
		AppID app = admin.getContextApp(getClass());
		DefaultHttpContext ctx = contextRegs.get(app.getIDString());
		ctx.resources.remove(alias);
		ctx.servlets.remove(alias);
		http.unregister(alias);

		// If the list is empty the http context could be unregistered too
		if (ctx.resources.isEmpty() && ctx.servlets.isEmpty())
			contextRegs.remove(alias);
	}

	@Override
	public String registerWebResource(String alias, String name) {
		AppID app = admin.getContextApp(getClass());
		// If no App found, the registration couldn't be accomplished.
		if (app == null)
			return null;
		String appid = app.getIDString();
		DefaultHttpContext httpCon = this.contextRegs.get(appid);
		if (httpCon == null) {
			Bundle b = admin.getContextBundle(this.getClass());
			httpCon = new DefaultHttpContext(b);
		}
		try {
			http.registerResources(alias, name, httpCon);
		} catch (NamespaceException e) {
			throw new RuntimeException(e);
		}
		contextRegs.put(appid, httpCon);
		httpCon.resources.put(alias, name);
		return alias;
	}

	@Override
	public String registerWebResource(String alias, Servlet servlet) {
		AppID app = admin.getContextApp(getClass());
		// If no App found, the registration couldn't be accomplished.
		if (app == null)
			return null;
		String appid = app.getIDString();
		DefaultHttpContext httpCon = this.contextRegs.get(appid);
		if (httpCon == null) {
			Bundle b = admin.getContextBundle(this.getClass());
			httpCon = new DefaultHttpContext(b);
		}
		try {
			http.registerServlet(alias, servlet, null, httpCon);
		} catch (ServletException | NamespaceException e) {
			throw new RuntimeException(e);
		}
		contextRegs.put(appid, httpCon);
		httpCon.servlets.put(alias, app);
		return alias;
	}

	@Override
	public UserRightsProxy getUrp(String usr) {
		// TODO This method should return a URP that have all Permissions
		return null;
	}

	@Override
	public boolean authenticate(HttpSession ses, String usr, String pwd) {
		return true;
	}

	@Override
	public AdministrationManager getAdminManager() {
		return admin;
	}

	@Override
	public AppPermission getDefaultPolicies() {
		return null;
	}

	@Override
	public void printPolicies(PrintStream os) {
	}

	@Override
	public boolean checkAddChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		return true;
	}

	@Override
	public boolean checkDeleteChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		return true;
	}

	ConcurrentHashMap<String, DefaultHttpContext> contextRegs;

	@Override
	public Map<String, String> getRegisteredResources(AppID appid) {
		DefaultHttpContext cntx = contextRegs.get(appid.getIDString());
		if (cntx == null)
			return Collections.emptyMap();
		else
			return cntx.resources;
	}

	@Override
	public AppPermission getPolicies(String user) {
		return null;
	}

	@Override
	public void setProperty(String user, String propName, String propValue) {
	}

	@Override
	public String getProperty(String user, String propName) {
		return null;
	}

	@Override
	public boolean isNatural(String user) {
		return false;
	}

	@Override
	public boolean isAppPermitted(String user, AppID app) {
		return true;
	}

	@Override
	public boolean handleSecurity(Permission perm, AccessControlContext acc) {
		return true;
	}

	@Override
	public AccessControlContext getBundleAccessControlContext(Class<?> cls) {
		ProtectionDomain[] pdArr = new ProtectionDomain[1];
		pdArr[0] = cls.getProtectionDomain();
		return new AccessControlContext(pdArr);
	}

	@Override
	public boolean removePermission(Bundle b, String webAccessPermissionClass, String filterString, String actions) {
		return false;
	}

	@Override
	public void addPermission(String user, AppPermissionFilter props) {
	}

	@Override
	public void addPermission(String user, List<AppPermissionFilter> props) {
	}

	@Override
	public void removePermission(String user, AppPermissionFilter properties) {
	}

	@Override
	public boolean isAllAppsPermitted(String user) {
		return true;
	}

	@Override
	public boolean isNoAppPermitted(String user) {
		return false;
	}

	@Override
	public void setAccessContext(AccessControlContext acc) {
	}

	@Override
	public void resetAccessContext() {
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return true;
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	@Override
	public String getMimeType(String name) {
		return null;
	}

	@Override
	public Map<String, ConditionalPermissionInfo> getGrantedPerms(Bundle b) {
		return new HashMap<String, ConditionalPermissionInfo>();
	}

	@Override
	public boolean isDefaultPolicy(String permtype, String permname, String actions) {
		return false;
	}

	public String registerWebResourcePath(String alias, Servlet servlet) {
		try {
			http.registerServlet(alias, servlet, null, null);
		} catch (ServletException | NamespaceException e) {
			throw new RuntimeException(e);
		}
		return alias;
	}

	@Override
	public String registerWebResourcePath(String alias, String name) {
		try {
			http.registerResources(alias, name, null);
		} catch (NamespaceException e) {
			throw new RuntimeException(e);
		}
		return alias;
	}

	@Override
	public boolean unregisterWebResourcePath(String alias) {
		return false;
	}
}
