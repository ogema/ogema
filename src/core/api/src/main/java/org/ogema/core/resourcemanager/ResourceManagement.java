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
package org.ogema.core.resourcemanager;

import java.util.List;

import org.ogema.core.model.Resource;

/**
 * Interface of the resource manager, which allows fundamental resource manipulations like adding resource types and
 * creating and deleting resources. Basic manipulation of resources is performed via the {@link ResourceAccess}.
 */
public interface ResourceManagement {

	/**
	 * Create a new resource
	 * 
	 * The resource is added instantly to the framework. Since all subresources
	 * are optional in the OGEMA data model, no additional subresources will be created.
	 * 
	 * If a resource with the given name already exists and the resource is of the type specified, the existing resource
	 * is returned. If the type is different, an exception is thrown. If the object is newly created it is provided
	 * inactive, otherwise the active-state is not changed.
	 * 
	 * @param <T> actual type of the new resource.
	 * @param name
	 *            name of the new resource. The name must represent a valid Java name as specified by
	 *            http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8. Applications should get a unique
	 *            resource name via {@link #getUniqueResourceName(java.lang.String) getUniqueResourceName}.
	 * @param type
	 *            type of the new resource
	 * 
	 * @throws ResourceException
	 *             Occurs when the resource could not be created for some reason.
	 * @return reference to the target resource (either the newly-created one or the already-existing one)
	 */
	public <T extends Resource> T createResource(String name, Class<T> type) throws ResourceException;

	/**
	 * Get resource name that is unique on the system and can always be obtained by the application via its own chosen
	 * resource name. The unique resource name shall be used by the application when creating a new toplevel resource
	 * (e.g. via {@link createResource}) in order to avoid naming conflicts with other applications that might want to
	 * use the same name for another toplevel resource. If the framework needs
	 * to create a new unique name from the requested name, the requested name
	 * will be a prefix of the unique name ({@code returnValue.startsWith(appResourceName)})
	 * 
	 * @param appResourceName
	 *            resource name chosen by the application
	 * @return resource name that will be unique on the system and will not change.
	 * @deprecated will be removed in a future version; choose a sufficiently unique name instead
	 */
	@Deprecated
	public String getUniqueResourceName(String appResourceName);

	/**
	 * Removes a resource and all of its sub-resources from the system.
	 * 
	 * @param name
	 *            the location of the resource.
	 * @throws NoSuchResourceException
	 *             thrown when no resource exists at the given location.
	 */
	public void deleteResource(String name) throws NoSuchResourceException;

	/**
	 * Returns all resource types known to the framework.
	 * 
	 * @return a list of all known resource types
	 */
	List<Class<? extends Resource>> getResourceTypes();
}
