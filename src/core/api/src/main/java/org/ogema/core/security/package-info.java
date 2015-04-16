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
/**
 * Provides the interfaces to manage the policies of access control in OGEMA framework.
 * The protection of security sensitive resources is performed by setting of policies
 * that are referenced to an application or an user. An AppPermission instance is linked
 * with a such entity and contains a number of AppPermissionTypes. Each AppPermissionType
 * instance describes a policy that is valid for the owner entity. The sum of all policies
 * described in AppPermissionType objects is the collection of the effective granted
 * permissions to this entity.
 * 
 * The PermissionManager supports application in reading or writing of the policies
 * for an application. The access rights for an user can be manipulated via AccessManager.
 * WebAccessManager holds the registration of web resources by the application and
 * controls the rights to access to these resources.
 * 
 */
package org.ogema.core.security;

