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
package org.ogema.accesscontrol;

public class ResourceAccessRights {

	private int mask;

	/**
	 * Create a ResourceAccessRights instance which holds the actions of a ResourcePermission that is granted to an
	 * Application permitted to access to a particular resource.
	 * 
	 * @param accessMask
	 *            is a bit mask consisting of the action bits defined in
	 *            {@link org.ogema.accesscontrol.ResourcePermission}. These actions are _READ, _WRITE, _DELETE, _CREATE,
	 *            _ADDSUB and _ACTIVITY.
	 */
	public ResourceAccessRights(int accessMask) {
		this.mask = accessMask;
	}

	/**
	 * Check if the access rights include read permission.
	 * 
	 * @return true if read is permitted false otherwise.
	 */
	public boolean isReadPermitted() {
		return ((mask & ResourcePermission._READ) != 0);
	}

	/**
	 * Check if the access rights include write permission.
	 * 
	 * @return true if write is permitted false otherwise.
	 */
	public boolean isWritePermitted() {
		return ((mask & ResourcePermission._WRITE) != 0);
	}

	/**
	 * Check if the access rights include delete permission.
	 * 
	 * @return true if delete is permitted false otherwise.
	 */
	public boolean isDeletePermitted() {
		return ((mask & ResourcePermission._DELETE) != 0);
	}

	/**
	 * Check if the access rights include add sub resource permission.
	 * 
	 * @return true if add sub resource is permitted false otherwise.
	 */
	public boolean isAddsubPermitted() {
		return ((mask & ResourcePermission._ADDSUB) != 0);
	}

	/**
	 * Check if the access rights include change activity permission.
	 * 
	 * @return true if change activity is permitted false otherwise.
	 */
	public boolean isActivityPermitted() {
		return ((mask & ResourcePermission._ACTIVITY) != 0);
	}

	/**
	 * Check if the access rights include create permission.
	 * 
	 * @return true if create is permitted false otherwise.
	 */
	public boolean isCreatePermitted() {
		return ((mask & ResourcePermission._CREATE) != 0);
	}
}
