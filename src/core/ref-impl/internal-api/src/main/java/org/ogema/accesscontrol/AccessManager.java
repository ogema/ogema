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
package org.ogema.accesscontrol;

import java.security.Permission;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ogema.core.application.AppID;
import org.ogema.core.security.AppPermission;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

public interface AccessManager extends Authenticator {

	/**
	 * An user is created and and added to the user management. The user could represent an App on any machine or a
	 * natural person which has to be authenticated on web interface. This property is given by the value of
	 * {@link isNatural}. If isNatrural is false a machine user is created that is able to access the rest interface
	 * only. The natural users are able to login to the web interface of the system.
	 * 
	 * If the given password is null the users name is set as the initial password.
	 * 
	 * The added user doesn't be decorated with any app or resource permissions as default at the creation time. They
	 * could be granted later on the administration interface.
	 * 
	 * @param userName
	 *            name of the entity which is added to the system as user.
	 * @param password
	 *            the initial password that is given to the user.
	 * @param isNatural
	 *            indicates if the user is a natural one or it represent a machine
	 * @return true if a new user is created or false if the user is already exists.
	 */
	public boolean createUser(String userName, String password, boolean isNatural);

	/**
	 * Removes the user from the user management.
	 * 
	 * @param userName
	 *            name of the user to be removed.
	 */
	public void removeUser(String userName);

	/**
	 * Create a role of type group by using the underlying user management. If the group already exists its returned.
	 * 
	 * @param name
	 *            Name of the group
	 * @return The group object matching with the given name.
	 */
	public Group createGroup(String name);

	/**
	 * Get the role object with the specified name from the user administration.
	 * 
	 * @param name
	 *            Name of the role
	 * @return The reference to the role object registered with the given name. null if no such role name is known by
	 *         the user management.
	 */
	public Role getRole(String name);

	/**
	 * Get all natural user registered to the AccessManager. Natural users are entities that can be logged in the system
	 * and access the web or rest interface.
	 * 
	 * @return List of name strings of the users registered in the system.
	 */
	public List<String> getAllUsers();

	/**
	 * Add a permission for the specific user. Note: to remove permissions use 
	 * ConditionalPermissionAdmin.
	 * @param user
	 * @param permission
	 * @return if the update was successful
	 * @throws IllegalStateException 
	 *            if user does not exist
	 */
	public boolean addPermission(String user, Permission permission);
	/**
	 * Add a set of permissions for the specific user. Note: to remove permissions use 
	 * ConditionalPermissionAdmin.
	 * @param user
	 * @param permissions
	 * @return if the update was successful
	 * @throws IllegalStateException 
	 *            if user does not exist
	 */
	public boolean addPermissions(String user, List<Permission> permissions);
	
	/**
	 * Extend the authorization of the given user by the rights to access the web resources of the app specified by its
	 * AppPreperties.
	 * 
	 * @param user
	 *            Name of the user it's rights are extended.
	 * @param props
	 *            A set of properties related to the application that should be granted to access to.
	 * @throws IllegalStateException 
	 *            if user does not exist
	 * 
	 */
	public void addPermission(String user, AppPermissionFilter props);

	/**
	 * Extend the authorization of the given user by the rights to access the web resources of apps specified each by an
	 * entry of the list of AppPermissionFilter.
	 * 
	 * @param user
	 *            Name of the user it's rights are extended.
	 * @param props
	 *            A list of AppPermissionFilter where each of them contains a set of properties related to an
	 *            application that should be granted to access to.
	 * @throws IllegalStateException 
	 *            if user does not exist
	 * 
	 */
	public void addPermission(String user, List<AppPermissionFilter> props);

	/**
	 * Remove the authorization of the given user to access resources of the given app.
	 * 
	 * @param user
	 *            Name of the user it's access rights are changed.
	 * @param properties
	 *            The applications properties it's resources will be no longer accessible for the user.
	 * @throws IllegalStateException 
	 *            if user does not exist
	 */
	public void removePermission(String user, AppPermissionFilter properties);

	/**
	 * Change the persistently stored password information of the natural user.
	 * 
	 * @param user
	 *            Users name.
	 * @param oldPwd
	 *            The old password.
	 * @param newPwd
	 *            The new password.
	 */
	public void setNewPassword(String user, String oldPwd, String newPwd);

	/**
	 * Change the persistently stored information of any user registered to the AccessManager.
	 * 
	 * @param user
	 *            Users name.
	 * @param credential
	 *            The name string of the credential its value is to be changed.
	 * @param value
	 *            The value of the credential to be set.
	 */
	public void setCredential(String user, String credential, String value);

	/**
	 * Get a list of all applications their registered resources are accessible partially or entirely by the given user.
	 * 
	 * @param user
	 *            Name of the user.
	 * @return List of AppID objects of the permitted apps
	 */
	public List<AppID> getAppsPermitted(String user);

	/**
	 * Check if the user has permission to access (web)resources of the application specified by its given AppID
	 * reference. This method is called by WebResourceManager during security handling.
	 * 
	 * @param user
	 *            The user object.
	 * @param app
	 *            application id reference.
	 * 
	 * @return true if the user is permitted to access the resources registered by the application, false otherwise.
	 */
	public boolean isAppPermitted(User user, AppID app);

	/**
	 * Check if the user has permission to access (web)resources of the application specified by its given AppID
	 * reference.
	 * 
	 * @param user
	 *            Name of the user.
	 * @param app
	 *            application id reference.
	 * 
	 * @return true if the user is permitted to access the resources registered by the application, false otherwise.
	 */
	boolean isAppPermitted(String user, AppID app);

	/**
	 * Check if the specified user is permitted to log into the system with the specified password. If the user doesn't
	 * exist the authentications fails and the method returns false.
	 * 
	 * @param remoteUser
	 *            name of the user.
	 * @param remotePasswd
	 *            password given by the user.
	 * @param isNatural
	 *            the user is a natural user (false for machine user)
	 * @return true if the user authentication succeeds false otherwise.
	 */
	public boolean authenticate(String remoteUser, String remotePasswd, boolean isNatural);
	
	/**
	 * Check the user name/pw and/or available {@link Authenticator} services whether
	 * the request can be associated to a user.
	 * @param req
	 * @return
	 * 		the user id or null, if the request could not be authenticated.
	 */
	public String authenticate(HttpServletRequest req);
	
	/**
	 * Like {@link #authenticate(HttpServletRequest)}, but authenticate only natural users
	 * (if natrualUser is true) or machine users (if natural user is false).
	 * @param req
	 * @param naturalUser
	 * @return
	 */
	public String authenticate(HttpServletRequest req, boolean naturalUser);
	
	/**
	 * Get the natural user associated with a servlet request, or null if no user 
	 * is logged in.
	 * @param req
	 * @return
	 */
	public String getLoggedInUser(HttpServletRequest req);

	/**
	 * Log out a previously successfully authenticated user.
	 * 
	 * @param usrName
	 *            the users name.
	 */
	public void logout(String usrName);

	/**
	 * Register a new installed app to the user management sub system. This is necessary to set user as permitted to
	 * access the apps resources.
	 * 
	 * @param id
	 *            application id reference to be registered.
	 */
	public void registerApp(AppID id);

	/**
	 * Remove an uninstalled app from the user management sub system.
	 * 
	 * @param id
	 *            application id reference to be unregistered.
	 */
	public void unregisterApp(AppID id);

	/**
	 * Check if the specified user has the specified role. If the roleName equals the application id string, this method
	 * behave the same as isAppPermitted. roleName could alternatively be the name of a ogema resource where the access
	 * right of the user to this resource is checked.
	 * 
	 * @param userName
	 *            name String of the user.
	 * @param roleName
	 *            role name describing the the protected resource.
	 * @return true if the user is permitted to access the resource specified with the role name, false otherwise.
	 */
	// public boolean checkPermission(String userName, String roleName);
	
	/**
	* Get the admissible authenticator ids.
	* @param user
	* @return
	*     Either null, meaning all authenticators are allowed, or the collection of admissible
	*     authenticator ids. If the authenticators have not been set explicitly, null is returned.
	*/
	public Collection<String> getSupportedAuthenticators(String user);

	/**
	* Add an admissible authenticator id. This is a void operation if currently all authenticators are allowed
	* for the user/group (the default).
	* @param user a user or group id
	* @param authenticatorId
	*/
	public void addSupportedAuthenticator(String user, String authenticatorId);

	/**
	* Disable a specific authenticator for the user/group.
	* @param user a user or group id
	* @param authenticatorId
	*/
	public void removeSupportedAuthenticator(String user, String authenticatorId);

	/**
	* @param user a user or group id
	* @param authenticatorIds
	*     Pass null to allow all authenticators (default), or a collection to specify which authenticators are allowed.
	*/
	public void setSupportedAuthenticators(String user, Collection<String> authenticatorIds);	 
	
	/**
	* Get the admissible authenticator ids. This setting is applicable to all users; get the authenticators for
	* specific users via {@link #getSupportedAuthenticators(String)}.
	* @return
	*     Either null, meaning all authenticators are allowed, or the collection of admissible
	*     authenticator ids. If the authenticators have not been set explicitly, null is returned.
	*/
	public Collection<String> getSupportedAuthenticators();

	/**
	* Add an admissible authenticator id for all users. This is a void operation if currently all authenticators are allowed (the default).
	* @param authenticatorId
	*/
	public void addSupportedAuthenticator(String authenticatorId);

	/**
	* Disable a specific authenticator for all users.
	* @param authenticatorId
	*/
	public void removeSupportedAuthenticator(String authenticatorId);

	/**
	* @param authenticatorIds
	*     Pass null to allow all authenticators (default), or a collection to specify which authenticators are allowed.
	*/
	public void setSupportedAuthenticators(Collection<String> authenticatorIds);	 

	/**
	 * Get the UserRightsProxy object linked to the given user.
	 * 
	 * @param usr
	 *            the users name
	 * @return the UserRightsProxy object
	 */
	public UserRightsProxy getUrp(String usr);

	/**
	 * Get an instance of AppPermission that holds the current permission granted to the specified user. This object can
	 * be used to manipulate the permission configuration of the user.
	 * 
	 * @param user
	 *            The name of the user
	 * @return the AppPermission object
	 */
	public AppPermission getPolicies(String user);

	/**
	 * Set a property that is registered by the user management. This can be read with the method {@link getProperty}.
	 * User properties are public. Unlike the user credentials there is no need of any permissions to read them.
	 * 
	 * @param user
	 *            Name of the user
	 * @param propName
	 *            Name of the property
	 * @param propValue
	 *            Value of the property
	 */
	public void setProperty(String user, String propName, String propValue);

	/**
	 * Get the value of a property set for the specified user
	 * 
	 * @param user
	 *            Name of the user
	 * @param propName
	 *            Name of the property
	 * @return Value string of the property
	 */
	public String getProperty(String user, String propName);

	/**
	 * Check if the specified user represents a natural person or a machine.
	 * 
	 * @param user
	 *            Name of the user
	 * @return true if the user is a natural person, false otherwise
	 */
	public boolean isNatural(String user);

	/**
	 * Checks if the user has unrestricted access to the web interfaces of all apps.
	 *
	 * @param user
	 *            the user name.
	 * @return true, if the user is permitted to access all apps, or false, otherwise.
	 */
	public boolean isAllAppsPermitted(String user);

	/**
	 * Checks if the user has access to the web interfaces of any app.
	 *
	 * @param user
	 *            the user name.
	 * @return true, if the user is not permitted to access any app or false, otherwise.
	 */
	public boolean isNoAppPermitted(String user);

	final public static String OWNER_NAME = "fadmin";
	final public static String SYSTEM_ID = "system";

	/**
	 * Gets all linear ancestors of the specified user, if the framework support hierarchical multi-cleint user
	 * management as specified in security level 3 of Ogema.
	 * 
	 * @param userName
	 *            Name of the user
	 * @return List of all linear ancestors
	 */
	public List<String> getParents(String userName);

	/**
	 * Gets the name of the user, the current thread runs in its security scope.
	 * 
	 * @return The name String of the current user.
	 */
	public String getCurrentUser();

	/**
	 * Sets the user specified with its user name as the user, the current thread runs in its security scope.
	 * 
	 * @throws SecurityException
	 *             if the calling context doesn't possess the org.ogema.accesscontrol.AdminPermission with the action
	 *             "system"
	 */
	public void setCurrentUser(String userName);

	/**
	 * Resets the the current user to the initial value.
	 */
	public void removeCurrentUser();

}
