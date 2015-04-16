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
package org.ogema.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.resourcetree.TreeElement;

/**
 * Interface to get all from DB needed for the ResourceAdministration
 * 
 * A resource is initially created containing only the required (non-optional) sub resources. The optional elements are
 * not (yet) created when calling addResource, but have to be added, when resource is decorated. When this occurs, the
 * corresponding values of the optional elements can, however, be persistently stored. Conversely, it may be that values
 * are non-optional, but their values are not yet persisted (for example, in the model, the value TempSens mmxTemp is
 * mandatory (must always be read and written), but is not persistent). Say the database needs to know that there is the
 * element mmxTemp, but do not store the actual float. The methods isOptional and isPersistent are independent of each
 * other.<br>
 * Note: The Id values of the top level resources are persistent and do not change over the entire lifetime of a
 * resource. The indexes/ids of the sub resources may change on every startup of the framework, but remain valid until
 * shutdown of the application accessing the index.
 * 
 */
public interface ResourceDB {

	/**
	 * Add resource type or add additional new elements, delete elements not in the new model. Name of class is the name
	 * of the type. If resources exist that are from the type to be updated and one of these resources does not fit the
	 * mandatory part of the new resource type, the update demand is to be rejected. The annotations @Nonpersistent and @Optional
	 * are to be evaluated. The annotation @Nonpersistent means that the value mustn't be stored in the data base. The
	 * annotations @Optional and @Nonpersistent are to be stored in the ResourceType entry.
	 * 
	 * @param type
	 *            new model
	 * @return ID of the created or updated type.
	 * @throws ResourceAlreadyExistsException
	 *             if resources of the type exist and the new model demands to add non-optional elements or demands to
	 *             remove elements that are present in an existing resource. InvalidResourceTypeException if the class
	 *             representing the type definition
	 * @throws InvalidResourceTypeException
	 *             the invalid resource type exception
	 */
	// @TODO this method is obsolete and can be removed from this interface. The
	// type is given as parameter to addResource, where the type registration
	// actions could be done too.
	public Class<? extends Resource> addOrUpdateResourceType(Class<? extends Resource> type)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException;

	/**
	 * Get a collection of children of a type which contains all own children and all children inherited from the super
	 * types.
	 * 
	 * @param name
	 *            Fully qualified name of the class defining the type. @see
	 * @return A collection of {@link Class} references where each child is represented by its type definition class.
	 * @throws TypeNotFoundException
	 *             if a type with the specified name doesn't exist in the DB. {@link addOrUpdateResourceType}
	 */
	public Collection<Class<?>> getTypeChildren(String name);

	/**
	 * Check if any resource type is registered with the specified name.
	 * 
	 * @param name
	 *            Fully qualified name of the class defining the type. @see
	 * @return true if the type exists false otherwise. {@link addOrUpdateResourceType}
	 */
	public boolean hasResourceType(String name);

	/**
	 * All currently installed resource types in the data base are returned.
	 * 
	 * @return A list of name strings of registered types. @see {@link addOrUpdateResourceType}. Size zero if no types
	 *         are installed.
	 */
	public List<Class<? extends Resource>> getAllResourceTypesInstalled();

	/**
	 * Add a top level resource and its tree in the persistent data storage. The tree must be fully resolved and
	 * initialized before it can be added in the data base. Resource is inactive by default.
	 * 
	 * @param name
	 *            Unique name for the top level resource which is demanded by the application.
	 * @param type
	 *            resolved class reference of the interface defining the resource type.
	 * @return TreeElement object representing the top level resource added to the database.
	 * @throws ResourceAlreadyExistsException
	 *             if a top level resource with the same name already exists.
	 * @throws InvalidResourceTypeException
	 *             if the class defining the type is not a valid type definition or its sub resources are not fully
	 *             resolved.
	 * @throws TypeNotFoundException
	 *             if the demanded resource type not yet registered in the database. @see addOrUpdateResourceType
	 */
	public TreeElement addResource(String name, Class<? extends Resource> type, String appID)
			throws ResourceAlreadyExistsException, InvalidResourceTypeException;

	/**
	 * Remove the top level resource from the data base permanently.
	 * 
	 * @param id
	 *            Index of the resource to be deleted.
	 */
	public void deleteResource(TreeElement elem);

	/**
	 * Check if the data base contains a top level resource with given name.
	 * 
	 * @param name
	 *            Name of the resource specified at the time of the resource creation. @see {@link addResource}.
	 * @return true if the resource exists false otherwise.
	 */
	public boolean hasResource(String name);

	/**
	 * Get id of top-level resource based on its name or null if the no top level resource exists with this name.
	 * 
	 * @param name
	 *            name of the resource
	 * @return A TreeElement Object holding the resource tree of this top level resource or null id no top level
	 *         resource is registered in the database with the given name.
	 */
	public TreeElement getToplevelResource(String name);

	/**
	 * Get a collection of id's of all registered top-level resources.
	 * 
	 * @return collection of indices of all top-level resources.
	 */
	public Collection<TreeElement> getAllToplevelResources();

	/**
	 * Signal the database that a transaction is started. The database implementation should synchronize the state of
	 * the data with the transactions which are initiated by the resource management to achieve a consistent persistent
	 * data at any time.
	 * 
	 */
	public void startTransaction();

	/**
	 * Signal the storage sub system of the database that a transaction its start was signaled before, is now
	 * terminated.
	 * 
	 */
	public void finishTransaction();

	/**
	 * Indicate if the database interface is initialized properly.
	 * 
	 * @return true if the database is ready to use false otherwise.
	 */
	public boolean isDBReady();

	/**
	 * Get the node object of the resource specified by its unique number
	 * 
	 * @param id
	 *            The unique number of the resource
	 * @return The Node object as instance of TreeElement
	 */
	public TreeElement getByID(int id);

	/**
	 * Get a collection of node objects, that match with the filter specified in dict. The entries of dict are
	 * implementation specific.
	 * 
	 * @param dict
	 *            a map key value pairs, that describe the nodes of the requested resources.
	 * @return Collection of matching nodes.
	 */
	Collection<TreeElement> getFilteredNodes(Map<String, String> dict);

}
