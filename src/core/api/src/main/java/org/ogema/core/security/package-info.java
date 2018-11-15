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

