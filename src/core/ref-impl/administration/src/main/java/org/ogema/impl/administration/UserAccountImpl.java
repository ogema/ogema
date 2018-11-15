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
package org.ogema.impl.administration;

import java.util.Dictionary;
import java.util.Objects;

import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.UserAccount;
import org.osgi.service.useradmin.User;

class UserAccountImpl implements UserAccount {

	private final String name;
	private final AccessManager accMngr;
	final User usr;
	// private PermissionManager permMngr;

	UserAccountImpl(String name, boolean isnatural, PermissionManager pMan) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(pMan);
		this.name = name;
		this.accMngr = pMan.getAccessManager();
		this.accMngr.createUser(name, null, isnatural);
		this.usr = (User) accMngr.getRole(name);
		// this.permMngr = pMan;
	}

	@Deprecated
	UserAccountImpl() {
		this.name = null;
		this.accMngr = null;
		this.usr = null;
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

	static UserAccount createinstance(String name, boolean natural, PermissionManager pm) {
		UserAccountImpl res;
		AccessManager accm = pm.getAccessManager();
		User user = (User) accm.getRole(name);
		if (user != null) {
			res = new UserAccountImpl(name, natural, pm);
		}
		else {
			throw new RuntimeException("No UserAccount could be created!");
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
