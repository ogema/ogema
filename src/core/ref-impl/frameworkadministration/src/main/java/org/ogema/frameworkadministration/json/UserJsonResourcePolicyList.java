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
