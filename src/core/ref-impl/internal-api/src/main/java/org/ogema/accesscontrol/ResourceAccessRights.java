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
	AccessControlContext acc;
	private TreeElement resource;
	private PermissionManager permMan;

	/**
	 * Create a ResourceAccessRights instance which holds the actions of a ResourcePermission that is granted to an
	 * Application permitted to access to a particular resource.
	 * 
	 */
	public ResourceAccessRights(AccessControlContext context, TreeElement te, PermissionManager permman) {
		this.checked = 0;
		this.mask = 0;
		resetAccessRights();
		this.acc = context;
		this.resource = te;
		this.permMan = permman;
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
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._READ;
			checked |= ResourcePermission._READ;
		}
		return ((mask & ResourcePermission._READ) != 0);
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
			if (permMan.handleSecurity(perm, acc))
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
			if (permMan.handleSecurity(perm, acc))
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
			if (permMan.handleSecurity(perm, acc))
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
			if (permMan.handleSecurity(perm, acc))
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
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._CREATE;
			checked |= ResourcePermission._CREATE;
		}
		return ((mask & ResourcePermission._CREATE) != 0);
	}

	public void resetAccessRights() {
		this.checked = 0;
	}
}
