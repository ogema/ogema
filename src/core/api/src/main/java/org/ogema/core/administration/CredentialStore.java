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
package org.ogema.core.administration;

import java.io.IOException;

import javax.net.ssl.SSLContext;

/**
 * Interface to the management of the credentials of users.
 */
public interface CredentialStore {

	/**
	 * Create a user on a gateway. If only a gateway user name and password are provided, a local gateway user is
	 * created, only. If the store user and password are provided as well (i.e. are not null), a connection to the
	 * governing application store is opened, and a request for the registration of a new user is sent, which will be
	 * connected to the local gateway user. <br>
	 * If the user exists, the password is updated.
	 * 
	 * @param accountGW
	 *            account name on gateway
	 * @param passwordGW
	 *            password on gateway
	 * @param accountStore
	 *            account name on appstore; may be null, in which case only a local gateway user is created/updated.
	 * @param passwordStore
	 *            password on appstore; may be null, in which case only a local gateway user is created/updated.
	 * @return true if a user was created
	 * @throws IOException
	 *             sign server not reachable
	 * @throws RuntimeException
	 *             Marketplace rejected the user
	 */
	public boolean createUser(String accountGW, String passwordGW, String accountStore, String passwordStore)
			throws IOException, IllegalArgumentException, RuntimeException;

	/**
	 * remove a user from the credential storage system
	 * 
	 * @param usrName
	 *            the users name
	 */
	public void removeUser(String usrName);

	/**
	 * Change the gateway password of the user. If the user not yet exists an empty string is given as old password. In
	 * this case a new user is created.
	 *
	 * @param usrName
	 *            the name of the user
	 * @param oldPwd
	 *            the old password
	 * @param newPwd
	 *            the new password
	 * @throws SecurityException
	 * 			  if the old password is wrong
	 */
	public void setGWPassword(String usrName, String oldPwd, String newPwd);

	/**
	 * Log an user in credential store provider and check the authentication of the user specified with its gateway user
	 * name and gateway password. The user is specified with its user name and password strings. The user is registered
	 * previously by calling {@link #createUser(String, String, String, String)}.
	 * 
	 * @param usrName
	 *            name of the user.
	 * @param pwd
	 *            password of the user.
	 * @return an object reference that identifies the session beginning with this login.
	 */
	public boolean login(String usrName, String pwd);

	/**
	 * Terminate a session previously started with login of the same user.
	 * 
	 * @param usrName
	 *            the user name.
	 */
	public void logout(String usrName);

	/**
	 * Get the ID string of the gateway.
	 * 
	 * @return ID string
	 */
	public String getGWId();
	
	public SSLContext getDISSLContext();
}
