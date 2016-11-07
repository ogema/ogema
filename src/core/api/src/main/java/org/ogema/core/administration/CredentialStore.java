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
package org.ogema.core.administration;

import java.io.IOException;

/**
 * Interface to the management of the credentials of users.
 */
public interface CredentialStore {

	/**
	 * Create a user on a gateway
	 * 
	 * @param accountGW
	 *            account name on gateway
	 * @param passwordGW
	 *            password on gateway
	 * @param accountStore
	 *            account name on appstore
	 * @param passwordStore
	 *            password on appstore
	 * @return true if a user was created
	 * @throws IOException
	 *             sign server not reachable
	 * @throws IllegalArgumentException
	 *             any of the parameters uri3 or email3 does not conform to the specified format
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
}
