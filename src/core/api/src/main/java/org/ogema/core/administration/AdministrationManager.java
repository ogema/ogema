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
import org.ogema.core.installationmanager.SourcesManagement;
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
	 * Gets users account holding user credentials and properties.
	 * @throws RuntimeException if user does not exist
	 */
	public UserAccount getUser(String userName);

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
	 * Scan the call stack for the first occurrence of a class that belongs to an osgi bundle. All entries up to the
	 * specified class parameter are ignored.
	 * 
	 * @param ignore
	 *            The class on the stack the scan begins from
	 * @return Bundle reference of the found class
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
	 * Get the AppID object related to the ogema application with the specified bundle reference. FIXME this need not be
	 * a 1-1 correspondence
	 * 
	 * @param b
	 *            The bundle reference of the application.
	 * @return The AppID object
	 */
	public AppID getAppByBundle(Bundle b);

	/**
	 * Creates a new user account with the specified name. The created user could be handled by the framework in
	 * dependence of the user represents a natural person or not. This can be specified by the boolean parameter
	 * isnatural. The new password of the user is equal to the user name.
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
	
	/**
	 * Get the available application sources.
	 * @return
	 */
	public SourcesManagement getSources();
}
