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

/*
 * The class implementing this interface takes over the user management.
 * User related settings are delegated to the OSGi UserAdmin
 * which holds the User registry including its persistent
 * storage. User settings that are relevant for resource access policy
 * are delegated to the corresponding resources.
 */
import java.util.List;

import org.ogema.core.application.AppID;
import org.ogema.core.installationmanager.InstallationManagement;
import org.osgi.framework.Bundle;

/** Framework component providing information and interaction for administration applications */
public interface AdministrationManager {

	/** Get all apps installed. */
	public List<AdminApplication> getAllApps();

	/**
	 * Gets all users registered.
	 */
	public List<UserAccount> getAllUsers();

	/**
	 * Add user account. If an account with this user name already exists the password is changed.
	 * 
	 * @param password
	 *            new password of user account. If null, the user account is deleted.
	 */
	public void setUserCredential(String userName, String password);

	/**
	 * Get all loggers on the system with administration level access (interface LoggerAdmin).
	 * 
	 * @return
	 */
	public List<AdminLogger> getAllLoggers();

	/**
	 * Gets a reference to the framework clock.
	 */
	public FrameworkClock getFrameworkClock();

	/**
	 * Scan the call stack for a class which is a class of an ogema application and determine the AppID object of the
	 * application. All found classes up to the specified class are ignored.
	 * 
	 * @param ignore
	 *            The class on the stack the scan begins from
	 * @return AppID of the application the found class belongs to
	 */
	public AppID getContextApp(Class<?> ignore);

	/**
	 * Scan the call stack for a class which is a class of an ogema application and determine the AppID object of the
	 * application. All found classes up to the specified class are ignored.
	 * 
	 * @param ignore
	 *            The class on the stack the scan begins from
	 * @return Bundle reference of the application the found class belongs to
	 */
	public Bundle getContextBundle(Class<?> ignore);

	/**
	 * Get the AppAdminAccess object related to the specified ogema application. In order to get get access the caller
	 * needs org.ogema.accesscontrol.AdminPermission.
	 * 
	 * @param id
	 *            Application id string
	 * @return AppAdminAccess object
	 */
	public AdminApplication getAppById(String id);

	/**
	 * Get the AppID object related to the ogema application with the specified bundle reference.
	 * 
	 * @param b
	 *            The bundle reference of the application.
	 * @return The AppID object
	 */
	public AppID getAppByBundle(Bundle b);

	/**
	 * Creates a new user account with the specified name. The created user could be handled by the framework in
	 * dependence of the user represents a natural person or not. This can be specified by the boolean parameter
	 * isnatural.
	 * 
	 * @param name
	 *            The name of the user
	 * @param isnatural
	 *            true if the user represents a natural person or false the user represents a machine.
	 * @return Reference to the newly created UserAccount object
	 */
	public UserAccount createUserAccount(String name, boolean isnatural);

	/**
	 * Removes a previously created user account with the given name.
	 * 
	 * @param name
	 *            The name of the user account
	 */
	public void removeUserAccount(String name);

	/**
	 * Gets the InstallationManager service reference.
	 * 
	 * @return The InstallationManager service reference
	 */
	public InstallationManagement getInstallationManager();
}
