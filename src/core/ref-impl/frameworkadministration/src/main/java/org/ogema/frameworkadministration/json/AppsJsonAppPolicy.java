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
package org.ogema.frameworkadministration.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tgries
 */
public class AppsJsonAppPolicy implements Serializable {

	private static final long serialVersionUID = -185370017731452405L;

	private String mode;
	private String uniqueName;
	private boolean delete;
	private boolean change;
	private List<AppsJsonAppPermissions> permissions = new ArrayList<AppsJsonAppPermissions>();
	private List<AppsJsonAppConditions> conditions = new ArrayList<AppsJsonAppConditions>();

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
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

	public List<AppsJsonAppPermissions> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<AppsJsonAppPermissions> permissions) {
		this.permissions = permissions;
	}

	public List<AppsJsonAppConditions> getConditions() {
		return conditions;
	}

	public void setConditions(List<AppsJsonAppConditions> conditions) {
		this.conditions = conditions;
	}

	public boolean isChange() {
		return change;
	}

	public void setChange(boolean change) {
		this.change = change;
	}

}
