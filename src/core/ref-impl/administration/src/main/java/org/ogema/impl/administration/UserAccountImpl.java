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
package org.ogema.impl.administration;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.UserAccount;

public class UserAccountImpl implements UserAccount {

	private String name;
	private AccessManager accMngr;

	public UserAccountImpl(String name, boolean isnatural, PermissionManager pMan) {
		this.name = name;
		this.accMngr = pMan.getAccessManager();
		pMan.getAccessManager().createUser(name, isnatural);
	}

	public UserAccountImpl() {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setNewPassword(String oldPassword, String newPassword) {
		boolean isNatural = accMngr.isNatural(name);
		accMngr.authenticate(name, oldPassword, isNatural);
		accMngr.setNewPassword(name, newPassword);
	}

	public static UserAccount createinstance(String user, boolean natural, PermissionManager pm) {
		UserAccountImpl acc = new UserAccountImpl();
		acc.name = user;
		acc.accMngr = pm.getAccessManager();
		return acc;
	}
}
