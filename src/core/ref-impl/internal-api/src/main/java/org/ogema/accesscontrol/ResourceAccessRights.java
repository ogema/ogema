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
		boolean[] tmp = { true, true, true, true, true, true };
		ALL_RIGHTS.checked = tmp;
		ALL_RIGHTS.mask = -1;
	}

	boolean checked[];
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
		this.checked = new boolean[6];
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
		if (!checked[0]) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.READ, resource, 0);
			// Check read permission
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._READ;
			checked[0] = true;
		}
		return ((mask & ResourcePermission._READ) != 0);
	}

	/**
	 * Check if the access rights include write permission.
	 * 
	 * @return true if write is permitted false otherwise.
	 */
	public boolean isWritePermitted() {
		if (!checked[1]) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.WRITE, resource, 0);
			// Check read permission
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._WRITE;
			checked[1] = true;
		}
		return ((mask & ResourcePermission._WRITE) != 0);
	}

	/**
	 * Check if the access rights include delete permission.
	 * 
	 * @return true if delete is permitted false otherwise.
	 */
	public boolean isDeletePermitted() {
		if (!checked[2]) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.DELETE, resource, 0);
			// Check read permission
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._DELETE;
			checked[2] = true;
		}
		return ((mask & ResourcePermission._DELETE) != 0);
	}

	/**
	 * Check if the access rights include add sub resource permission.
	 * 
	 * @return true if add sub resource is permitted false otherwise.
	 */
	public boolean isAddsubPermitted() {
		if (!checked[3]) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.ADDSUB, resource, 0);
			// Check read permission
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._ADDSUB;
			checked[3] = true;
		}
		return ((mask & ResourcePermission._ADDSUB) != 0);
	}

	/**
	 * Check if the access rights include change activity permission.
	 * 
	 * @return true if change activity is permitted false otherwise.
	 */
	public boolean isActivityPermitted() {
		if (!checked[4]) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.ACTIVITY, resource, 0);
			// Check read permission
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._ACTIVITY;
			checked[4] = true;
		}
		return ((mask & ResourcePermission._ACTIVITY) != 0);
	}

	/**
	 * Check if the access rights include create permission.
	 * 
	 * @return true if create is permitted false otherwise.
	 */
	public boolean isCreatePermitted() {
		if (!checked[5]) {
			ResourcePermission perm = new ResourcePermission(ResourcePermission.CREATE, resource, 0);
			// Check read permission
			if (permMan.handleSecurity(perm, acc))
				mask |= ResourcePermission._CREATE;
			checked[5] = true;
		}
		return ((mask & ResourcePermission._CREATE) != 0);
	}

	public void resetAccessRights() {
		this.checked[0] = false;
		this.checked[1] = false;
		this.checked[2] = false;
		this.checked[3] = false;
		this.checked[4] = false;
		this.checked[5] = false;
	}
}
