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
package org.ogema.accesscontrol;

import java.security.AccessControlContext;

import org.ogema.resourcetree.TreeElement;

public class ResourceAccessRights {

	public static final ResourceAccessRights ALL_RIGHTS;

	static {
		ALL_RIGHTS = new ResourceAccessRights(null, null, null);
		ALL_RIGHTS.checked = 0;
		ALL_RIGHTS.mask = -1;
	}

	int checked;
	/*
	 * accessMask is a bit mask consisting of the action bits defined in {@link
	 * org.ogema.accesscontrol.ResourcePermission}. These actions are _READ, _WRITE, _DELETE, _CREATE, _ADDSUB and
	 * _ACTIVITY.
	 */

	private int mask;
	final AccessControlContext acc;
	private final TreeElement resource;
	private final PermissionManager permMan;
	// may be null
	private final String user;

	/**
	 * Create a ResourceAccessRights instance which holds the actions of a ResourcePermission that is granted to an
	 * Application permitted to access to a particular resource.
	 * 
	 */
	public ResourceAccessRights(AccessControlContext context, TreeElement te, PermissionManager permman) {
		this(context, te, permman, null);
	}
	
	public ResourceAccessRights(AccessControlContext context, TreeElement te, PermissionManager permman, String user) {
		this.checked = 0;
		this.mask = 0;
		resetAccessRights();
		this.acc = context;
		this.resource = te;
		this.permMan = permman;
		this.user = AccessManager.SYSTEM_ID.equals(user) ? null : user;
	}
	
	/**
	 * Check if the access rights include read permission.
	 * 
	 * @return true if read is permitted false otherwise.
	 */
	public boolean isReadPermitted() {
		if (!permMan.isSecure())
			return true;
		if ((checked & ResourcePermission._READ) == 0) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.READ, resource, 0);
			// Check read permission
			if (check(perm))
				mask |= ResourcePermission._READ;
			checked |= ResourcePermission._READ;
		}
		return ((mask & ResourcePermission._READ) != 0);
	}
	
	private final boolean check(final ResourcePermission perm) {
		final boolean check1 = permMan.handleSecurity(perm, acc);
		if (user == null || !check1)
			return check1;
		return permMan.handleSecurity(user, perm);
	}

	/**
	 * Check if the access rights include write permission.
	 * 
	 * @return true if write is permitted false otherwise.
	 */
	public boolean isWritePermitted() {
		if (!permMan.isSecure())
			return true;
		if ((checked & ResourcePermission._WRITE) == 0) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.WRITE, resource, 0);
			// Check read permission
			if (check(perm))
				mask |= ResourcePermission._WRITE;
			checked |= ResourcePermission._WRITE;
		}
		return ((mask & ResourcePermission._WRITE) != 0);
	}

	/**
	 * Check if the access rights include delete permission.
	 * 
	 * @return true if delete is permitted false otherwise.
	 */
	public boolean isDeletePermitted() {
		if (!permMan.isSecure())
			return true;
		if ((checked & ResourcePermission._DELETE) == 0) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.DELETE, resource, 0);
			// Check read permission
			if (check(perm))
				mask |= ResourcePermission._DELETE;
			checked |= ResourcePermission._DELETE;
		}
		return ((mask & ResourcePermission._DELETE) != 0);
	}

	/**
	 * Check if the access rights include add sub resource permission.
	 * 
	 * @return true if add sub resource is permitted false otherwise.
	 */
	public boolean isAddsubPermitted() {
		if (!permMan.isSecure())
			return true;
		if ((checked & ResourcePermission._ADDSUB) == 0) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.ADDSUB, resource, 0);
			// Check read permission
			if (check(perm))
				mask |= ResourcePermission._ADDSUB;
			checked |= ResourcePermission._ADDSUB;
		}
		return ((mask & ResourcePermission._ADDSUB) != 0);
	}

	/**
	 * Check if the access rights include change activity permission.
	 * 
	 * @return true if change activity is permitted false otherwise.
	 */
	public boolean isActivityPermitted() {
		if (!permMan.isSecure())
			return true;
		if ((checked & ResourcePermission._ACTIVITY) == 0) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.ACTIVITY, resource, 0);
			// Check read permission
			if (check(perm))
				mask |= ResourcePermission._ACTIVITY;
			checked |= ResourcePermission._ACTIVITY;
		}
		return ((mask & ResourcePermission._ACTIVITY) != 0);
	}

	/**
	 * Check if the access rights include create permission.
	 * 
	 * @return true if create is permitted false otherwise.
	 */
	public boolean isCreatePermitted() {
		if (!permMan.isSecure())
			return true;
		if ((checked & ResourcePermission._CREATE) == 0) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.CREATE, resource, 0);
			// Check read permission
			if (check(perm))
				mask |= ResourcePermission._CREATE;
			checked |= ResourcePermission._CREATE;
		}
		return ((mask & ResourcePermission._CREATE) != 0);
	}

	public void resetAccessRights() {
		this.checked = 0;
	}
}
