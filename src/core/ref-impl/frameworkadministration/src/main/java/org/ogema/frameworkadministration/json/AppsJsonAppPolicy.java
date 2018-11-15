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
