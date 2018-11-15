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
package org.ogema.core.security;

import java.util.List;
import java.util.Map;

import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;

/**
 * An AppPermission is a collection of permissions that are set or to be set in the protection domain of an application.
 * Each entry of this collection is encoded as an AppPermissionType. An AppPermission instance can hold the permissions
 * of an already installed app or the permissions that are prepared for a new app installation.
 * 
 */
public interface AppPermission {

	/**
	 * Get a list of all permissions granted/denied to the owner app.
	 * 
	 * @return List of AppPermissionType where each of them can represent an ALLOW or DENY policy.
	 */
	public List<AppPermissionType> getTypes();

	/**
	 * Get a list of all permissions denied to the owner app.
	 * 
	 * @return List of AppPermissionType where each of them represent a DENY policy.
	 */
	public List<AppPermissionType> getExceptions();

	/**
	 * Add a policy to be denied to the owner app. The policy is specified by the parameter action, filter and
	 * condition. The possible values for the action Enum are defined in {@link AppPermissionType}. Each Enum
	 * corresponds with a Permission class. The filter string depends on the chosen action Enum and and the
	 * corresponding permission. For details on filter string format see the descriptions to the permissions classes.
	 * The condition information object defines an extra condition that has to be fulfilled so the policy would be
	 * applied. The method creates an instance of AppPermissionType which contains all relevant information of the
	 * defined policy.
	 * 
	 * @param action
	 *            A value of one of the Enum's defined in AppPermissionType. The permissions class depends on the
	 *            referenced Enum.
	 * @param filter
	 *            A filter string which is used by the Permission class and describes the resource, the access to be
	 *            denied to.
	 * @param condition
	 *            A condition information object which defines extra condition that has to be fulfilled so the policy
	 *            would be applied.
	 * @return An AppPermissionType instance describing the added policy.
	 */
	public AppPermissionType addException(Enum<?> action, String filter, ConditionInfo condition);

	/**
	 * Add a policy to be denied to the owner app. The policy is specified by the parameter action adn filter. The
	 * possible values for the action Enum are defined in {@link AppPermissionType}. Each Enum corresponds with a
	 * Permission class. The filter string depends on the chosen action Enum and and the corresponding permission. For
	 * details on filter string format see the descriptions to the permissions classes. The condition information object
	 * defines an extra condition that has to be fulfilled so the policy would be applied. The method creates an
	 * instance of AppPermissionType which contains all relevant information of the defined policy.
	 * 
	 * @param action
	 *            A value of one of the Enum's defined in AppPermissionType. The permissions class depends on the
	 *            referenced Enum.
	 * @param filter
	 *            A filter string which is used by the Permission class and describes the resource, the access to be
	 *            denied to.
	 * @return An AppPermissionType instance describing the added policy.
	 */
	public AppPermissionType addException(Enum<?> action, String filter);

	/**
	 * Add a policy to be denied to the owner app. The policy is specified by the permission name, filter string and a
	 * condition object. The filter string and the list of actions as elements of the second parameter depend on the
	 * specified permission class. For details on filter string format see the descriptions to the permissions classes.
	 * The condition information object defines an extra condition that has to be fulfilled so the policy would be
	 * applied. The method creates an instance of AppPermissionType which contains all relevant information of the
	 * defined policy.
	 * 
	 * @param permName
	 *            The first parameter is the fully qualified class name of the permission class.
	 * @param args
	 *            An array of strings that are specific to the given permission class. Usually this array consists of
	 *            two elements where the first one is the filter string and the second one is the comma separated list
	 *            of actions.
	 * @param cond
	 *            A condition information object which defines extra condition that has to be fulfilled so the policy
	 *            would be applied.
	 * @return An AppPermissionType instance describing the added policy.
	 */
	public AppPermissionType addException(String permName, String[] args, ConditionInfo cond);

	/**
	 * Removes an negative policy which was added before via
	 * {@link AppPermission#addException(java.lang.String, java.lang.String[], org.osgi.service.condpermadmin.ConditionInfo)
	 * addException}. Each AppPermissionType has an unique id as name string, see {@link AppPermissionType#getName() }.
	 * 
	 * @param name
	 *            Name string which is unique for the AppPermissionType
	 */
	public void removeException(String name);

	/**
	 * Add a policy to be granted to the owner app. The policy is specified by the action and filter string. The
	 * possible values for the action Enum are defined in {@link AppPermissionType}. Each Enum corresponds with a
	 * Permission class. The filter string depends on the chosen action Enum and and the corresponding permission. For
	 * details on filter string format see the descriptions to the permissions classes. The condition information object
	 * defines an extra condition that has to be fulfilled so the policy would be applied. The method creates an
	 * instance of AppPermissionType which contains all relevant information of the defined policy.
	 * 
	 * @param action
	 *            A value of one of the Enum's defined in AppPermissionType. The permissions class depends on the
	 *            referenced Enum.
	 * @param filter
	 *            A filter string which is used by the Permission class and describes the resource, the access to be
	 *            denied to.
	 * @return An AppPermissionType instance describing the added policy.
	 */
	public AppPermissionType addPermission(Enum<?> action, String filter);

	/**
	 * Add a policy to be granted to the owner app. The policy is specified by the parameter action, filter and
	 * condition. The possible values for the action Enum are defined in {@link AppPermissionType}. Each Enum
	 * corresponds with a Permission class. The filter string depends on the chosen action Enum and and the
	 * corresponding permission. For details on filter string format see the descriptions to the permissions classes.
	 * The condition information object defines an extra condition that has to be fulfilled so the policy would be
	 * applied. The method creates an instance of AppPermissionType which contains all relevant information of the
	 * defined policy.
	 * 
	 * @param action
	 *            A value of one of the Enum's defined in AppPermissionType. The permissions class depends on the
	 *            referenced Enum.
	 * @param filter
	 *            A filter string which is used by the Permission class and describes the resource, the access to be
	 *            denied to.
	 * @param cond
	 *            A condition information object which defines extra condition that has to be fulfilled so the policy
	 *            would be applied.
	 * @return An AppPermissionType instance describing the added policy.
	 */
	public AppPermissionType addPermission(Enum<?> action, String filter, ConditionInfo cond);

	/**
	 * Add a policy to be granted to the owner app. The policy is specified by the permission name, filter string and a
	 * condition object. The filter string and the list of actions as elements of the second parameter depend on the
	 * specified permission class. For details on filter string format see the descriptions to the permissions classes.
	 * The condition information object defines an extra condition that has to be fulfilled so the policy would be
	 * applied. The method creates an instance of AppPermissionType which contains all relevant information of the
	 * defined policy.
	 * 
	 * @param perm
	 *            The first parameter is the fully qualified class name of the permission class.
	 * @param args
	 *            An array of strings that are specific to the given permission class. Usually this array consists of
	 *            two elements where the first one is the filter string and the second one is the comma separated list
	 *            of actions.
	 * @param cond
	 *            A condition information object which defines extra condition that has to be fulfilled so the policy
	 *            would be applied.
	 * @return An AppPermissionType instance describing the added policy.
	 */
	public AppPermissionType addPermission(String perm, String[] args, ConditionInfo cond);

	/**
	 * Removes an positive policy which was added before via
	 * {@link AppPermission#addPermission(java.lang.String, java.lang.String[], org.osgi.service.condpermadmin.ConditionInfo)
	 * addPermission}. Each AppPermissionType has an unique id as name string, see {@link AppPermissionType#getName() }.
	 * 
	 * @param name
	 *            Name string which is unique for the AppPermissionType
	 */
	public void removePermission(String name);

	/**
	 * Get a map all set policies for an app. A map entry consists of the unique name of the policy as key and a policy
	 * description instance of ConditionalPermissionInfo as specified in section 50.7 of
	 * "OSGi Service Platform Core Specification Release 4, version 4.3" as value.
	 * 
	 * @return Map of all policies added to this AppPermission.
	 */
	public Map<String, ConditionalPermissionInfo> getGrantedPerms();

	/**
	 * Removes all policies added to this AppPermission before by calling the addException/addPermission methods. After
	 * the execution of this method no one of the policies added before is considered by the permission check. System
	 * policies that are set by the system and potentially more than one application has right of are not removed by
	 * this method.
	 */
	public void removeAllPolicies();

	/**
	 * Remove all policies with positive access type. See {@link AppPermission#removeAllPolicies()}
	 */
	public void removePermissions();

	/**
	 * Remove all policies with negative access type. See {@link AppPermission#removeAllPolicies()}
	 */
	public void removeExceptions();

}
