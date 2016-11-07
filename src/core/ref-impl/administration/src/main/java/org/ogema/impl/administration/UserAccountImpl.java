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
package org.ogema.impl.administration;

import java.util.Dictionary;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.UserAccount;
import org.osgi.service.useradmin.User;

public class UserAccountImpl implements UserAccount {

	private String name;
	private AccessManager accMngr;
	User usr;
	// private PermissionManager permMngr;

	public UserAccountImpl(String name, boolean isnatural, PermissionManager pMan) {
		this.name = name;
		this.accMngr = pMan.getAccessManager();
		this.accMngr.createUser(name, null, isnatural);
		this.usr = (User) accMngr.getRole(name);
		// this.permMngr = pMan;
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
		if (!accMngr.authenticate(name, oldPassword, isNatural))
			throw new SecurityException("Authorization to change password failed!");
		accMngr.setNewPassword(name, oldPassword, newPassword);
	}

	public static UserAccount createinstance(String name, boolean natural, PermissionManager pm) {
		UserAccountImpl res;
		AccessManager accm = pm.getAccessManager();
		User user = (User) accm.getRole(name);
		if (user == null) {
			res = new UserAccountImpl(name, natural, pm);
		}
		else {
			res = new UserAccountImpl();
		}
		return res;
	}

	@Override
	public String getStoreUserName(String storeName) {
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = usr.getCredentials();
		return (String) dict.get("user_" + storeName);
	}
}
