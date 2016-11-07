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
package org.ogema.core.installationmanager;

import java.util.List;

import org.osgi.framework.Bundle;

/**
 * An application source is an OGEMA marketplace or a file directory containing OGEMA apps (as OSGi bundles)
 */
public interface ApplicationSource {
	/**
	 * Get name of OGEMA marketplace. If the application source is a local directory the name may be empty or may be
	 * defined by the framework.
	 * 
	 * @return name of OGEMA application source
	 */
	public String getName();

	/**
	 * Get applications available in the application source
	 * 
	 * @return
	 */
	public List<InstallableApplication> getAppsAvailable();

	/**
	 * Get web address of the OGEMA marketplace or local directory path containing OGEMA applications.
	 * 
	 * @return The address of the marketplace.
	 */
	public String getAddress();

	/**
	 * Establishes a connection to the ApplicationSource.
	 * 
	 * @return true if the connection could be successfully established or false if any problem is occurred
	 */
	public boolean connect();

	/**
	 * Shuts down the connection to the ApplicationSource.
	 * 
	 * @return true if the connection could be successfully shoot down or false if any problem is occurred
	 */
	public boolean disconnect();

	/**
	 * Get applications available in the specified directory of application source.
	 *
	 * @param user
	 *            the name of the user the request is sent for.
	 * @return the applications available in the specified directory
	 */
	public List<InstallableApplication> getAppsAvailable(String user);

	/**
	 * Initiate the installation of an application specified with its location directory and name.
	 *
	 * @param name
	 *            the name of the application.
	 * @param user
	 *            the users name
	 * @return The bundle reference resulted from the installation.
	 */
	public Bundle installApp(String name, String user);

	/**
	 * Add a new store user associated to the given gateway user.
	 *
	 * @param gwUserName
	 *            the name of the gateway user the new store user associated with.
	 * @param storeUserName
	 *            the name of the created store user.
	 * @param gwPWD
	 *            the password of the gateway user
	 * @param storePWD
	 *            the password which is to set as market place password for the new user.
	 * @return true if successful, false if user already exists.
	 * @throws RuntimeException
	 *             if any error occurs.
	 */
	public boolean addStoreUser(String gwUserName, String storeUserName, String gwPWD, String storePWD);

	/**
	 * Remove an existing store user from the gateway.
	 *
	 * @param storeUserName
	 *            the store user name
	 * @return true, if successful
	 */
	public boolean removeStoreUser(String storeUserName);

}
