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
package org.ogema.core.resourcemanager;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;

/**
 * ResourceManager API for Apps
 */
public interface ResourceAccess {

	/**
	 * Register a listener for resource events for resources specified by an ogema data model class Event will only be
	 * generated if the app has at least read permissions for the resources, and only for resources that are active.
	 * 
	 * @param resourceType
	 *            must be an interface extending Resource; a resourceAvailable event will be triggered for all resources
	 *            implementing this interface
	 * @param listener
	 *            receiving resource available events
	 * @throws NullPointerException
	 *          if the reference to the listener was null. A valid listener must be passed to the demand.
	 */
	<S extends Resource, T extends S> void addResourceDemand(Class<T> resourceType, ResourceDemandListener<S> listener);

	/**
	 * delete resource demand from the framework. If the demand is based on {@link ResourcePattern} all
	 * resources connected via the demand will be disconnected
	 * 
	 * Granted access permissions stay valid.
	 * 
	 * @param resourceType
	 *            all resource demands based on this resourceType made by the application will be deleted
	 */
	<S extends Resource, T extends S> void removeResourceDemand(Class<T> resourceType,
			ResourceDemandListener<S> listener);

	/**
	 * Lookup a resource by its fully qualified name (path). The delimiter of the path must be "/". Perform security
	 * checks and return appropriate proxy object.
	 * 
	 * @param path
	 *            fully qualified path
	 * @return the requested resource, null if no fitting resource is found
	 * @throws SecurityException
	 *             if access is not allowed
	 */
	<T extends Resource> T getResource(String path) throws SecurityException;

	/**
	 * Get all resources of a given type (or any subtype) which are readable
     * by this application.
	 * 
	 * @param resourceType
	 *            if null, return all resources for which view permission exists
	 * @return List of all resources (read proxy objects) of a given type for which permissions exists
	 */
	<T extends Resource> List<T> getResources(Class<T> resourceType);

	/**
	 * Get all resources being of or inheriting from a given resource type. If a suitable resource exists but no access
	 * permission for the resource exist, the resource is not contained in the result.
	 * 
	 * @param resourceType
	 *            type of the resources contained in the result. Passing null or Resource.class means "all resources".
	 * @return List of all resources of a given type for which permissions exists
	 */
	<T extends Resource> List<T> getToplevelResources(Class<T> resourceType);

	/**
	 * Creates and returns a new transaction object that can be used to perform multiple changes to the resource graph as an
	 * atomic operation.
	 * @deprecated
	 */
	@Deprecated
	Transaction createTransaction();
	
	/**
	 * Creates and returns a new transaction object that can be used to perform multiple changes to the resource graph as an
	 * atomic operation.
	 * @return
	 */
	ResourceTransaction createResourceTransaction();
	
}
