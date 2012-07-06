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
package org.ogema.resourcetree;

import java.util.List;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceNotFoundException;
import org.ogema.persistence.ResourceDB;

/**
 * 
 *
 */
public interface TreeElement {

	/**
	 * Gets the ID String of the app which is registered as owner of this TreeElement.
	 * 
	 * @return App id string.
	 */
	public String getAppID();

	/**
	 * Sets the ID String of the app which is registered as owner of this TreeElement.
	 * 
	 * @param appID
	 *            the new app id string.
	 */
	public void setAppID(String appID);

	/**
	 * Gets the reference to the resource object represented by this TreeElement.
	 * 
	 * @return The resource reference.
	 */
	public Object getResRef();

	/**
	 * Sets the reference to the resource object represented by this TreeElement.
	 * 
	 * @param resRef
	 *            The resource reference.
	 */
	public void setResRef(Object resRef);

	/**
	 * Checks if the resource associated with this TreeElement is active.
	 * 
	 * @return true if the resource is active false otherwise.
	 */
	public boolean isActive();

	/**
	 * Changes the activity status of the resource associated with this TreeElement.
	 * 
	 * @param active
	 *            The new status of the activity where true means active and false inactive.
	 */
	public void setActive(boolean active);

	/**
	 * Gets the parent node of this TreeElement.
	 * 
	 * @return The reference to the TreeElement of the parent node or null if this is the root node.
	 */
	public TreeElement getParent();

	/**
	 * Gets the id of this TreeElement which is used as data base index of the persistently stored resource.
	 * 
	 * @return Resource id as integer value.
	 */
	public int getResID();

	/**
	 * A key value that indicates which simple model definition the (sub) resource owns. Only basic types have different
	 * type keys where all complex types have the same type key.
	 * 
	 * @return The key of the simple type.
	 * 
	 *         TYPE_KEY_BOOLEAN = 0; TYPE_KEY_FLOAT = 1; TYPE_KEY_INT = 2; TYPE_KEY_LONG = 3; TYPE_KEY_STRING = 4;
	 *         TYPE_KEY_BOOLEAN_ARR = 5; TYPE_KEY_FLOAT_ARR = 6; TYPE_KEY_INT_ARR = 7; TYPE_KEY_LONG_ARR = 8;
	 *         TYPE_KEY_STRING_ARR = 9; TYPE_KEY_COMPLEX_ARR = 10; TYPE_KEY_COMPLEX = 11; TYPE_KEY_OPAQUE = 12;
	 */
	public int getTypeKey();

	/**
	 * Gets the name string of the resource associated with this TreeElement. In case of top level resource the name
	 * string is set at the call to {@link ResourceDB.addResource} and for sub resources it is specified at the call to
	 * {@link TreeElement.addChild}.
	 * 
	 * @return The name string.
	 */
	public String getName();

	/**
	 * Gets the class reference of the data model definition of the resource associated with this TreeElement.
	 * 
	 * @return The class reference.
	 */
	public Class<? extends Resource> getType();

	/**
	 * Gets the persistence status of the resource represented by this TreeElement.
	 * 
	 * @return true if the resource is stored persistently or false otherwise.
	 */
	public boolean isNonpersistent();

	/**
	 * Gets kind of the relation of the resource associated with this TreeElement to its parent resource.
	 * 
	 * @return false if the resource is a model defined sub resource of its parent or true if its a decorator of its
	 *         parent resource.
	 */
	public boolean isDecorator();

	/**
	 * Checks if this TreeElement is a top level one.
	 * 
	 * @return true if the TreeElement is the root of the tree or false otherwise.
	 */
	public boolean isToplevel();

	/**
	 * Checks if this TreeElement is a direct child of its parent resource or if it represent a reference to a node in
	 * an other sub tree.
	 * 
	 * @return true if the TreeElement refer to an external node or false if the resource is a direct child of its
	 *         parent.
	 */
	public boolean isReference();

	/**
	 * Checks if the element is a ResourceList (formerly ResourceList) element.
	 * 
	 * @return true if the element is a ResourceList type resource, false if not.
	 */
	public boolean isComplexArray();

	/**
	 * If the child resource is a reference the node of the referenced resource is delivered by this method.
	 * 
	 * @return If the child resource is a reference the node of the referenced resource is delivered by this method.
	 */
	public TreeElement getReference();

	/**
	 * Adds a child resource to this node as a required sub resource. If isDecorating is false the caller ensures that
	 * the child to be added specified by name and type is an element of the type definition of the resource represented
	 * by this node. {@see getType()}. Otherwise an exception is thrown.
	 * 
	 * @param name
	 *            Name of the sub resource. If the resource doesn't decorate its parent, it has to be a part of the
	 *            model definition of the parent resource as an optional element.
	 * @param type
	 *            Class reference of the model definition interface.
	 * @param isDecorating
	 *            If true the added sub resource mustn't be a part of the model of the parent otherwise an optional
	 *            element must exist in the model definition.
	 * @return The tree node instance representing the added sub resource.
	 * @throws ResourceAlreadyExistsException
	 *             If the child is decorating but an optional element with the same name exists in the model definition.
	 * 
	 * @throws ResourceNotFoundException
	 *             If the child is not decorating and no optional element with the given name is part of the model
	 *             definition.
	 * @throws InvalidResourceTypeException
	 *             If the given type class can't be verified as a valid type definition interface class.
	 * 
	 */
	public TreeElement addChild(String name, Class<? extends Resource> type, boolean isDecorating)
			throws ResourceAlreadyExistsException, ResourceNotFoundException, InvalidResourceTypeException;

	/**
	 * Adds a child resource to this node as a required sub resource which references an other resource instead of being
	 * an instance of a resource itself. If isDecorating is false the caller ensures that the child to be added
	 * specified by name and type is an element of the type definition of the resource represented by this node. {@see
	 * getType()}.
	 * 
	 * @param ref
	 *            The reference to the node which is referenced by this child.
	 * @param name
	 *            The name string of the child. This is independent of the name string of the referenced resource.
	 * @param If
	 *            true the added sub resource mustn't be a part of the model of the parent otherwise an optional element
	 *            must exist in the model definition.
	 * @return The tree node instance representing the added sub resource.
	 */
	public TreeElement addReference(TreeElement ref, String name, boolean isDecorating);

	/**
	 * Gets all required children of the resource on this node. These are all sub resources added by calling
	 * {@link addChild} or {@link addReference}. If the child resource is a reference the node of the referenced
	 * resource is to get via {@link getReference()}. This method doesn't dereference this TreeElement if its a
	 * reference. It doesn't return the children of the referenced resource but it delivers the list of the decorators.
	 * 
	 * @return List of all children of the resource represented by this TreeElement
	 */
	public List<TreeElement> getChildren();

	/**
	 * Gets a sub node of this TreeElement which has the given name. The sub node has to be added via {@link addChild}
	 * or {@link addReference} before. If the child resource is a reference the node of the referenced resource is to
	 * get via {@link getReference()}. This method doesn't dereference it to deliver the children of the referenced
	 * resource but it delivers the list of the decorators.
	 * 
	 * @param childName
	 *            Name of the sub node.
	 * @return TreeElement instance representing the node with the given name or null if no such node exists.
	 */
	public TreeElement getChild(String childName);

	/**
	 * Gets a SimpleResourceData instance which covers the value of a TreeElement representing a SimpleResource.
	 * 
	 * @return A SimpleResourceData instance.
	 * @throws ResourceNotFoundException
	 *             If this TreeElement instance not registered in the data base.
	 * @throws UnsupportedOperationException
	 *             If this TreeElement doesn't represent a SimpleResource.
	 */
	public SimpleResourceData getData() throws ResourceNotFoundException, UnsupportedOperationException;

	/**
	 * Sends an event that signalizes change of the value of this node. This method is used specially after changes made
	 * on array values, which are accessed directly instead of via setter methods.
	 */
	public void fireChangeEvent();

	/**
	 * Gets the path of the node within the tree.
	 * 
	 * @return Path string
	 */
	public String getPath();

	/**
	 * Gets the resource type of the sub elements of the ResourceList that owns this TreeElement
	 * 
	 * @return The class object of the sub elements.
	 */
	public Class<? extends Resource> getResourceListType();

	/**
	 * Sets the resource type of the sub elements of the ResourceList that owns this TreeElement
	 * 
	 * @param cls
	 *            The class object of the sub elements.
	 */
	public void setResourceListType(Class<? extends Resource> cls);
}
