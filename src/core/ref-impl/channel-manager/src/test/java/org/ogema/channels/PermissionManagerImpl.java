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
package org.ogema.channels;

import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.Permission;
import java.util.Map;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.ResourceAccessRights;
import org.ogema.applicationregistry.ApplicationRegistry;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.resourcetree.TreeElement;
import org.osgi.framework.Bundle;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;

public class PermissionManagerImpl implements PermissionManager {

	@Override
	public AccessManager getAccessManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean closeWebAccess(AppID app) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean installPerms(AppPermission perm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AppPermission getPolicies(AppID app) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppPermission setDefaultPolicies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean handleSecurity(Permission perm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleSecurity(Permission perm, AccessControlContext acc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WebAccessManager getWebAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebAccessManager getWebAccess(AppID app) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSystemPermissionAdmin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApplicationRegistry getApplicationRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppPermission createAppPermission(String location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkCreateResource(Application app, Class<? extends Resource> type, String name, int count) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResourceAccessRights getAccessRights(Application app, TreeElement el) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkDeleteResource(Application app, TreeElement te) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AppPermission getDefaultPolicies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void printPolicies(PrintStream os) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean checkAddChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean checkDeleteChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AccessControlContext getBundleAccessControlContext(Class<?> class1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removePermission(Bundle bundle, String permissionClassName, String filterString, String actions) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAccessContext(AccessControlContext acc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetAccessContext() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, ConditionalPermissionInfo> getGrantedPerms(String bLoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDefaultPolicy(String permType, String filter, String actions) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleSecurity(String user, Permission perm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removePermissionManual(ConditionalPermissionUpdate cpu, Bundle bundle, String permissionClassName,
			String filterString, String actions) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkWebAccess(AppID accessor, AppID access) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public ResourceAccessRights getAccessRights(Application app, TreeElement el, String user) {
		// TODO Auto-generated method stub
		return null;
	}

}
