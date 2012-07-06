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
	 */
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
