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
package org.ogema.impl.apploader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.administration.CredentialStore;
import org.ogema.core.security.AppPermission;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(specVersion = "1.2", immediate = true)
@Service(CredentialStore.class)
public class CredentialStoreImpl implements CredentialStore {

	private static final String APPSTORE_GROUP_NAME = "appstoreGroup";
	private static final String APPSTORE_PWD_NAME = "appstoreCred";
	private static final String APPSTORE_USER_NAME = "appstoreUsr";
	@Reference
	private UserAdmin userAdmin;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public void setGWPassword(String usrName, String oldPwd, final String newPwd) {
		if (!login(usrName, oldPwd))
			throw new SecurityException("Wrong old passowrd!");
		Role role = userAdmin.getRole(usrName);
		final User usr = (User) role;

		if (role == null) {
			throw new IllegalArgumentException("User doesn't exist: " + usrName);
		}
		else {
			// Set users default password
			Boolean hasCred = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					@SuppressWarnings("unchecked")
					Dictionary<String, Object> dict = usr.getCredentials();
					dict.put(Constants.PASSWORD_NAME, newPwd);
					return usr.hasCredential(Constants.PASSWORD_NAME, newPwd);
				}
			});

			if (hasCred) {
				logger.debug("Set new password succeeded.");
			}
			else {
				logger.debug("Set new password failed.");
			}
		}

	}

	@Override
	public boolean createUser(String accountGW, String passwordGW, String accountStore, String passwordStore)
			throws IOException, IllegalArgumentException, RuntimeException {
		Objects.requireNonNull(accountGW);
		Objects.requireNonNull(passwordGW);
		// Check validity of the parameter
		boolean check = true;
		if (accountGW == null || accountGW.equals(""))
			check = false;
		// else if (accountGW != null && !accountGW.equals("") && passwordGW == null)
		// check = false;
		else if (accountStore != null && accountStore.equals("")) // accountstore may be null, that results in creation
																	// of a local user only
			check = false;
		else if (accountStore != null && passwordStore == null)
			check = false;
		if (!check)
			throw new IllegalArgumentException(); // TODO explain why (move to separate cases above?)
		// create/update gateway user
		setCredential(accountGW, Constants.PASSWORD_NAME, passwordGW);
		if (accountStore != null)
			addStoreCredentials(accountGW, accountStore, passwordStore);
		return true;
	}

	@Override
	public boolean login(String usrName, final String pwd) {
		Role role = userAdmin.getRole(usrName);
		if (role == null)
			return false;
		final User admin = (User) role;
		Boolean hasCred = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				return admin.hasCredential(Constants.PASSWORD_NAME, pwd);
			}
		});
		if (!hasCred)
			return false;
		return true;

	}

	@Override
	public void logout(String usrName) {
	}

	@Override
	public void removeUser(String usrName) {
	}

	private void setCredential(String user, String credential, String value) {
		User usr;
		Role role = userAdmin.getRole(user);
		if (role == null) {
			throw new IllegalArgumentException();
		}
		usr = (User) role;
		// Set users credential
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = usr.getCredentials();
		dict.put(credential, value);
		if (usr.hasCredential(user, credential))
			logger.debug("User credential is set correctly");
	}

	Boolean hasAccess(String user) {
		User thisUser = (User) userAdmin.getRole(user);

		return thisUser.hasCredential(APPSTORE_USER_NAME, user);
	}

	private Group getAppstoreGroup(String groupName) {
		// Get or create group for the appstore
		if (userAdmin.getRole(groupName) == null) {
			return (Group) userAdmin.createRole(groupName, Role.GROUP);
		}
		return (Group) userAdmin.getRole(groupName);
	}

	private String addStoreCredentials(String gwUser, String storeUser, String storePwd) {
		User thisUser = (User) userAdmin.getRole(gwUser);
		Group appstoreAccessRole;

		if (!hasAccess(gwUser)) {

			appstoreAccessRole = getAppstoreGroup(APPSTORE_GROUP_NAME);

			// Set credentials and assign user to role
			setCredential(gwUser, APPSTORE_PWD_NAME, storePwd);
			setCredential(gwUser, APPSTORE_USER_NAME, storeUser);
			appstoreAccessRole.addMember(thisUser);
			return "The user " + gwUser + " was successfully assigned to the appstore with the name " + storeUser;
		}
		else {
			setCredential(gwUser, APPSTORE_PWD_NAME, storePwd);
			return "The password has successfully been changed";
		}
	}

	@Override
	public String getGWId() {
		String clientID = AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				String id = FrameworkUtil.getBundle(getClass()).getBundleContext()
						.getProperty("org.ogema.secloader.gatewayidentifier");
				return id;
			}
		});
		if (clientID == null) {
			try {
				clientID = "OGEMA-" + InetAddress.getLocalHost().toString();
			} catch (UnknownHostException e) {
				clientID = "OGEMA-" + System.currentTimeMillis();
			}
		}
		return clientID;
	}

	@Override
	public SSLContext getDISSLContext() {
		return null;
	}
}
