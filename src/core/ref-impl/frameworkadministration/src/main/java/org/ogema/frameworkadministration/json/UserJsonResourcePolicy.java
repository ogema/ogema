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
package org.ogema.frameworkadministration.json;

import java.io.Serializable;

/**
 *
 * @author tgries
 */
public class UserJsonResourcePolicy implements Serializable {

	private static final long serialVersionUID = 4330092303597446986L;

	private String accessDecision;
	private String resourcePath;
	private String permissionActions;
	private String permissionName;
	private String resourceType;
	private String uniqueName;
	private boolean delete = false;
	private boolean change = false;

	public String getPermissionName() {
		return permissionName;
	}

	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	public String getPermissionActions() {
		return permissionActions;
	}

	public void setPermissionActions(String permissionActions) {
		this.permissionActions = permissionActions;
	}

	public String getAccessDecision() {
		return accessDecision;
	}

	public void setAccessDecision(String accessDecision) {
		this.accessDecision = accessDecision;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isChange() {
		return change;
	}

	public void setChange(boolean change) {
		this.change = change;
	}

}
