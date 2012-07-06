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
package org.ogema.frameworkadministration.json.get;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author tgries
 */
public class UserInformationJsonGet implements Serializable {

	private static final long serialVersionUID = -8725777055903096839L;

	private String name;
	private boolean isAdmin;
	private Map<Object, Object> credentials;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Map<Object, Object> getCredentials() {
		return credentials;
	}

	public void setCredentials(Map<Object, Object> credentials) {
		this.credentials = credentials;
	}

}
