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
/**
 * 
 */
package org.ogema.accesscontrol;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * @author mns
 * 
 */
public class ResourcePermissionCollection extends PermissionCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7423524417909731092L;

	HashSet<Permission> perms;

	public ResourcePermissionCollection() {
		perms = new HashSet<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.PermissionCollection#add(java.security.Permission)
	 */
	@Override
	public void add(Permission permission) {
		perms.add((ResourcePermission) permission);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.PermissionCollection#implies(java.security.Permission)
	 */
	@Override
	public boolean implies(Permission permission) {
		for (Permission p : perms) {
			if (p.implies(permission))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.PermissionCollection#elements()
	 */
	@Override
	public Enumeration<Permission> elements() {
		return Collections.enumeration(perms);
	}

}
