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

import org.ogema.core.application.AppID;

/**
 *
 * @author tgries
 */
public class UserJsonPermittedApps implements Serializable {

	private static final long serialVersionUID = 4590072284091784714L;

	private String user;
	private List<AppID> permittedApps = new ArrayList<AppID>();

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<AppID> getPermittedApps() {
		return permittedApps;
	}

	public void setPermittedApps(List<AppID> permittedApps) {
		this.permittedApps = permittedApps;
	}

}
