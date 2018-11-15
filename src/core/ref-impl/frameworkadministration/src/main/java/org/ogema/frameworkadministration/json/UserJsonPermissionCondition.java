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
public class UserJsonPermissionCondition implements Serializable {

	private static final long serialVersionUID = 6625360065961582803L;

	private String name;
	private String mode;
	private UserJsonCondition condition;
	private List<UserJsonPermission> permissions = new ArrayList<UserJsonPermission>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public UserJsonCondition getCondition() {
		return condition;
	}

	public void setCondition(UserJsonCondition condition) {
		this.condition = condition;
	}

	public List<UserJsonPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<UserJsonPermission> permissions) {
		this.permissions = permissions;
	}

}
