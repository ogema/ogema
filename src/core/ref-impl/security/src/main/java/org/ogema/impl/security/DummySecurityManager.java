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
package org.ogema.impl.security;

import java.io.PrintStream;
import java.security.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.ogema.accesscontrol.AccessManager;
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
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.useradmin.User;

public class DummySecurityManager implements AccessManager, PermissionManager, WebAccessManager {

	private final HttpService http;
	private final ResourceAccessRights allAccess;
	private AdministrationManager admin;

	public DummySecurityManager(HttpService http, AdministrationManager admin) {
		this.http = http;
		this.admin = admin;
		this.allAccess = new ResourceAccessRights(-1);
	}

	public DummySecurityManager(BundleContext bc) {
		this.http = (HttpService) bc.getService(bc.getServiceReference(HttpService.class.getName()));
		this.allAccess = new ResourceAccessRights(-1);
	}

	@Override
	public AccessManager getAccessManager() {
		return this;
	}

	@Override
	public void installPerms(AppPermission perm) {
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
	public void addPermission(String user, String app) {
	}

	@Override
	public void removePermission(String user, AppID app) {
	}

	@Override
	public void setNewPassword(String user, String newPassword) {
	}

	@Override
	public void setCredetials(String user, String credential, String value) {
	}

	@Override
	public List<String> getAppsPermitted(String user) {
		return null;
	}

	@Override
	public boolean isAppPermitted(User user, AppID app) {
		return true;
	}

	// @Override
	// public boolean isAppPermitted(String user, Bundle bundle) {
	// return true;
	// }

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
	public boolean checkPermission(String userName, String roleName) {
		return true;
	}

	@Override
	public void unregisterWebResource(String alias) {
		http.unregister(alias);
	}

	@Override
	public String registerWebResource(String alias, String name) {
		try {
			http.registerResources(alias, name, null);
		} catch (NamespaceException e) {
			throw new RuntimeException(e);
		}
		return alias;
	}

	@Override
	public String registerWebResource(String alias, Servlet servlet) {
		try {
			http.registerServlet(alias, servlet, null, null);
		} catch (ServletException | NamespaceException e) {
			throw new RuntimeException(e);
		}
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

	@Override
	public Map<String, String> getRegisteredResources(AppID appid) {
		return Collections.emptyMap();
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
}
