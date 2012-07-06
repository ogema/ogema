/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.accesscontrol;

import java.io.PrintStream;
import java.security.Permission;

import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.security.AppPermission;
import org.ogema.core.security.WebAccessManager;
import org.ogema.resourcetree.TreeElement;

/**
 * This class is the entry point to the functionality related to permission handling. It encapsulates the Java/OSGi
 * permissions handling via Conditional Permission Admin and the OGEMA specific access permissions via AccessManager.
 */
public interface PermissionManager {

	/**
	 * Get the single instance of AccessManager which controls user based resource access by the users.
	 * 
	 * @return The AccessManager instance.
	 */
	public AccessManager getAccessManager();

	/**
	 * Update the policy table of the conditional permission admin. The given AppPermission includes a list of
	 * AppPermissionTypes where each of them represents a named entry of the policy table. The AppPermission instance is
	 * obtained by the call to {@link getPolicies}. Multiple entries, positive (permission) or negative(exception),
	 * should be avoided, in order to prevent negative effects on the performance.
	 * 
	 * @param perm
	 *            AppPermission that contains the description of the permissions to be granted.
	 */
	public void installPerms(AppPermission perm);

	/**
	 * Get a list of access policy applied to the given application. It include all access permissions granted to this
	 * application.
	 * 
	 * @param app
	 *            The application its policy is requested.
	 * @return List of descriptions with an entry each granted permission.
	 */
	public AppPermission getPolicies(AppID app);

	/**
	 * Install a set of permissions defined as default permissions for all Apps. This method is called if the
	 * administrator requests the change of installed Default policies.
	 * 
	 * @param pInfos
	 *            List of Permission where each of them can be provided with an condition as expected by the method
	 *            org.osgi.service.condpermadmin .ConditionalPermissionUpdate.getConditionalPermissionInfos(). For the
	 *            exact format definition of the permission info string see OSGi Service Platform Core Specification,
	 *            The OSGi Alliance, Release 4, Version 4.2
	 */
	public void setDefaultPolicies(AppPermission pInfos);

	/**
	 * Checks if the current access context contains the permission that implies the given permission. The check policy
	 * is: If the call stack contains a class of an ogema application, the protection domain of the last calling
	 * application is used. If no ogema application is contained the standard AccessControlContext without any
	 * modifications is used.
	 * 
	 * @param perm
	 *            The permission to be checked.
	 * @return true if the permission is granted, false otherwise.
	 */
	public boolean handleSecurity(Permission perm);

	/**
	 * Gets the reference to the {@link WebAccessManager} instance.
	 * 
	 * @return WebAccessManager reference.
	 */
	public WebAccessManager getWebAccess();

	/**
	 * Gets the implementation dependent object that support the injection of permissions and the manipulation of the
	 * permission configuration of the system. In a security implementation that bases on the OSGi security this object
	 * could be the ConditionalPermissionAdmin.
	 * 
	 * @return The object reference.
	 */
	public Object getSystemPermissionAdmin();

	/**
	 * Creates an AppPermission instance that contains the application specific policies. This method is called to get
	 * the current policy configuration of the application or to manipulate it.
	 * 
	 * @param location
	 *            The location string of the bundle, that contains the application.
	 * @return The reference to the AppPermission object.
	 */
	public AppPermission createAppPermission(String location);

	/**
	 * Checks the permission to create an OGEMA resource by the given application. The resource to be created is
	 * specified by its type class and resource path. The specified count gives the number of the resources from this
	 * type that are already created by the same application.
	 * 
	 * @param app
	 *            The Application object which is the owner of the resource.
	 * @param type
	 *            The type class of the resource.
	 * @param name
	 *            The unique path of the resource.
	 * @param count
	 *            The number of the resources of the demanded type that are already owned by the specified application.
	 * @return true if the permission is granted, false otherwise.
	 */
	public boolean checkCreateResource(Application app, Class<? extends Resource> type, String name, int count);

	/**
	 * Creates a {@link ResourceAccessRights} object that contains and caches the permission of an application to access
	 * to the specified resource. The injected permission is determined at the calling time and doesn't change even
	 * tough the permission configuration in the system changes.
	 * 
	 * @param app
	 *            The application owning the access permission.
	 * @param el
	 *            The {@link TreeElement} object, that represents the resource, the access right are to be evaluated
	 *            for.
	 * @return The ResourceAccessRights object.
	 */
	public ResourceAccessRights getAccessRights(Application app, TreeElement el);

	/**
	 * Checks the permission of the given application to delete the resource specified with its TreeElement object.
	 * 
	 * @param app
	 *            The application that demands the delete access to the resource.
	 * @param te
	 *            The {@link TreeElement} object that represent the resource that is to be deleted.
	 * @return true if the permission is granted, false otherwise.
	 */
	public boolean checkDeleteResource(Application app, TreeElement te);

	/**
	 * Gets the {@link AdministrationManager} instance of the system.
	 * 
	 * @return The AdministrationManager reference.
	 */
	public AdministrationManager getAdminManager();

	/**
	 * Gets an AppPermission instance that encapsulates the default permission currently set in the system.
	 * 
	 * @return The AppPermission reference
	 */
	public AppPermission getDefaultPolicies();

	/**
	 * Prints the policies currently configured out to the specified PrintStream.
	 * 
	 * @param os
	 *            The PrintStream, the policies printed out to.
	 */
	public void printPolicies(PrintStream os);

	/**
	 * Checks the Permission to create a channel to a low level driver via the {@link ChannelManager}. The application
	 * the permission is granted to is determined as the latest calling application in the call stack. {@see
	 * PermissionManager#handleSecurity(Permission)}.
	 * 
	 * @param configuration
	 *            The description object of the channel to be created.
	 * @param deviceLocator
	 *            The description object of the device, the channel connected to.
	 * @return true if the Permission is granted, false otherwise.
	 */
	public boolean checkAddChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator);

	/**
	 * Checks the Permission to delete a channel that is created before. The application the permission is granted to is
	 * determined as the latest calling application in the call stack. {@see
	 * PermissionManager#handleSecurity(Permission)}.
	 * 
	 * @param configuration
	 *            The description object of the channel to be deleted.
	 * @param deviceLocator
	 *            The description object of the device, the channel connected to.
	 * @return true if the Permission is granted, false otherwise.
	 */
	public boolean checkDeleteChannel(ChannelConfiguration configuration, DeviceLocator deviceLocator);

}
