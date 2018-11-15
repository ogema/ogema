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
package org.ogema.impl.security.gui;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.json.JSONArray;
import org.ogema.accesscontrol.AccessManager;
import org.ogema.accesscontrol.PermissionManager;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

public class Users {

	private AccessManager acc;

	private UserAdmin ua;

	Users(PermissionManager pman, UserAdmin usrAd) {
		this.acc = pman.getAccessManager();
		this.ua = usrAd;
	}

	List<String> userList() {
		List<String> allUsers = acc.getAllUsers();
		List<String> users = new ArrayList<String>();

		for (String User : allUsers) {
			if (acc.isNatural(User)) {
				users.add(User);
			}
		}

		return users;
	}

	String addCredentials(String pwd, String appstore, String user) {
		User thisUser = (User) acc.getRole(user);
		String groupName = "appstore_" + appstore;
		String pwdCred = "pwd_" + appstore;
		String userCred = "user_" + appstore;
		Group appstoreAccessRole;

		if (!hasAccess(appstore, user)) {

			appstoreAccessRole = getAppstoreGroup(groupName);

			// Set credentials and assign user to role
			acc.setCredential(user, pwdCred, pwd);
			acc.setCredential(user, userCred, user);
			appstoreAccessRole.addMember(thisUser);
			return "The user " + user + " was successfully assigned to the appstore " + appstore;
		}
		else {
			acc.setCredential(user, pwdCred, pwd);
			return "The password has successfully been changed";
		}
	}

	JSONArray getAssignedUsers(String appstore) {
		JSONArray userArr = new JSONArray();
		Group accessGroup = getAppstoreGroup("appstore_" + appstore);
		Role[] members = accessGroup.getMembers();
		if (members != null && members.length > 0) {
			for (Role member : members) {
				userArr.put(member.getName());
			}
		}
		else {
			userArr.put(false);
		}
		return userArr;
	}

	JSONArray getAssignedStores(String user) {
		JSONArray storeArr = new JSONArray();
		User thisUser = (User) acc.getRole(user);
		Authorization auth = ua.getAuthorization(thisUser);
		for (String role : auth.getRoles()) {
			if (role.contains("appstore_")) {
				storeArr.put(role);
			}
		}
		if (storeArr.length() == 0) {
			storeArr.put(false);
		}
		return storeArr;
	}

	Boolean hasAccess(String appstore, String user) {
		User thisUser = (User) acc.getRole(user);

		return thisUser.hasCredential("user_" + appstore, user);
	}

	String checkLogin(String user, String pwd, String appstore) {
		User thisUser = (User) acc.getRole(user);

		if (thisUser == null) {
			return "User does not exist";
		}

		if (!thisUser.hasCredential("user_" + appstore, user)) {
			return "User does not exist for this Appstore";
		}
        @SuppressWarnings("unchecked")
		Dictionary<String, String> cred = thisUser.getCredentials();
		String thispwd = cred.get("pwd_" + appstore);
		if (pwd.contentEquals(thispwd)) {
			return "Success";
		}
		else {
			return "Wrong password";
		}
	}

	private Group getAppstoreGroup(String groupName) {
		// Get or create group for the appstore
		if (ua.getRole(groupName) == null) {
			return (Group) ua.createRole(groupName, Role.GROUP);
		}
		return (Group) ua.getRole(groupName);
	}

}
