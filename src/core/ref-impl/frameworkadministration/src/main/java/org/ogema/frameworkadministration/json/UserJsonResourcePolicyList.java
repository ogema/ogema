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
public class UserJsonResourcePolicyList implements Serializable {

	private static final long serialVersionUID = 2159868959400253171L;

	private String user;
	private List<UserJsonResourcePolicy> resourcePermissions = new ArrayList<UserJsonResourcePolicy>();

	public UserJsonResourcePolicyList() {
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<UserJsonResourcePolicy> getResourcePermissions() {
		return resourcePermissions;
	}

	public void setResourcePermissions(List<UserJsonResourcePolicy> resourcePermissions) {
		this.resourcePermissions = resourcePermissions;
	}

}
