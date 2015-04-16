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
package org.ogema.core.model;

import java.util.List;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.InvalidResourceTypeException;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceGraphException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.VirtualResourceException;

/**
 * Application-specific view on a resource. 
 * No resource is ever of this type, but all resource types inherit from this.
 * Consequently, the methods defined herein are available on all resources.
 * Note that some methods are a property of the resource and 
 * universal between applications (e.g. {@link #getLocation()}, {@link #getResourceType()}), 
 * whereas some methods are specific to the application's view on the resource 
 * (e.g. {@link #getPath() getPath}, {@link #getAccessMode() getAccessMode} ).<br>
 * Resource objects are associated to a path. For objects initially returned by
 * the framework to {@link ResourceListener}s have a path that equals the location
 * of the resource. Further navigation can lead to paths that are not equal to the
 * location, then (both path and location can be read via {@link #getPath()} and
 * {@link #getLocation()}, respectively).
 */
public interface Resource {

	/**
	 * Gets the name of this resource, which is the last part of the path.
	 * @return name of the resource
	 * @see Resource#getPath() 
	 */
	String getName();

	/**
	 * Get full path of resource including name. Path delimiter is "/".
	 * @return full resource path
	 */
	String getPath();

	/**
	 * Get full path of resource including name
	 * 
	 * @param delimiter
	 *            character or string to be inserted between each path element
	 * @return full resource path
	 */
	String getPath(String delimiter);

	/**
	 * Gets the location of this resource (i.e. the path to this resource
	 * that does not contain a reference) including the resource name. The
	 * path delimiter is "/".
	 * 
	 * @see #getLocation(java.lang.String) 
	 * @return full resource path
	 */
	String getLocation();

	/**
	 * Gets the location of this resource (i.e. the path to this resource
	 * that does not contain a reference) including the resource name.
	 * 
	 * @param delimiter
	 *            character or string to be inserted between each path element
	 * @return resource location
	 */
	String getLocation(String delimiter);

	/**
	 * Get type of resource.
	 * 
	 * @return usually the simple class name of the interface implemented by the resource. In case of custom types the
	 *         resource itself may not really implement the interface, in this case the resource type can only be
	 *         determined via getResourceType.
	 */
	Class<? extends Resource> getResourceType();

	/**
	 * Register listener receiving callbacks whenever the resource value is changed or written.
	 * 
	 * @param listener
	 *            reference to the listener receiving the callbacks.
	 * @param callOnEveryUpdate
	 *            receive a callback every time the resource value is written, even it is the same value.
	 *            For schedules and array-values resources this must be true.
	 */
	void addValueListener(ResourceValueListener<?> listener, boolean callOnEveryUpdate);

	/**
	 * Register listener receiving callback whenever the resource value changes
	 * to a new value (for simple values containing only a single value) or if
	 * the values are written to (for array resources and schedules).
	 * 
	 * @param listener
	 *            reference to the listener receiving the callbacks.
	 * @see #addValueListener(org.ogema.core.resourcemanager.ResourceValueListener, boolean) 
	 */
	void addValueListener(ResourceValueListener<?> listener);

	/**
	 * Unregister a resource listener. If the listener had not been registered previously, 
	 * this does nothing.
	 * 
	 * @param listener the listener to remove.
	 * 
	 * @return true iff the listener was actually registered on this resource and has been removed.
	 */
	boolean removeValueListener(ResourceValueListener<?> listener);

	/**
	 * Register listener receiving callback on value changed events of the resource (including all sub resources) TODO
	 * should that really be the case, givne that resources can be arbitrarily complex?
	 * 
	 * @param listener
	 *            reference to the listener receiving the callbacks.
	 * @param recursive
	 *            register listener on all sub resources.
	 * @deprecated use {@link #addValueListener(org.ogema.core.resourcemanager.ResourceValueListener)} instead
	 */
	@Deprecated
	void addResourceListener(org.ogema.core.resourcemanager.ResourceListener listener, boolean recursive);

	/**
	 * Unregister a resource listener. If the listener had not been registered previously, 
	 * this does nothing.
	 * 
	 * @param listener
	 *            reference to the listener receiving the callbacks
	 * @return true if the listener was registered before and has now been removed.
	 * 
	 * @deprecated use {@link #removeValueListener(org.ogema.core.resourcemanager.ResourceValueListener) } instead
	 */
	@Deprecated
	boolean removeResourceListener(org.ogema.core.resourcemanager.ResourceListener listener);

	/**
	 * Register a listener receiving callbacks when the {@link AccessMode} granted on this
	 * resource is changed.
	 * @param listener 
	 *          reference to the listener receiving the callback.
	 */
	void addAccessModeListener(AccessModeListener listener);

	/**
	 * Removes a previously-registered AccessModeListener on this resource. If the
	 * listener had not been registered before, this has no effect.
	 * @param listener 
	 *          reference to the listener to remove
	 * @return true if the listener was registered before and has now been removed.
	 * @see #addAccessModeListener(org.ogema.core.resourcemanager.AccessModeListener) 
	 */
	boolean removeAccessModeListener(AccessModeListener listener);

	/**
	 * Adds a listener that is informed about structural changes on the resource. When a structural change happens the listener is notified via a callback. 
	 * @param listener reference to the listener receiving the callbacks.
	 */
	void addStructureListener(ResourceStructureListener listener);

	/**
	 * Removes a previously-registered structure listener. If the listener had not been registered before, this has no effect.
	 * @param listener the listener to register.
	 * @return true if the listener was registered before and has now been removed.
	 */
	boolean removeStructureListener(ResourceStructureListener listener);

	/**
	 * Checks whether resource is active. Virtual resources are always inactive.
	 * 
	 * @return true if resource is active, false if resource is inactive
	 */
	boolean isActive();

	/**
	 * Checks whether resource is top-level
	 * 
	 * @return true if resource is top-level, false if resource is a sub-resource.
	 */
	boolean isTopLevel();

	/**
	 * Returns true exactly if the resource is writeable. 
	 * @return true iff resource is writable
	 */
	boolean isWriteable();

	/**
	 * @return true if this resource is a decorator of its parent resource.
	 */
	boolean isDecorator();

	/**
	 * Try to change the application's AccessMode to this resource.
	 * Shared access mode is granted if no exclusive access with equal or higher
	 * priority is requested.
	 * Exclusive access mode is overridden by exclusive access requests
	 * with a higher priority. Shared access mode is overwritten when another application
	 * is granted exclusive write access.
	 *
	 * Requesting {@link AccessMode#READ_ONLY} will remove an existing request by the
	 * same application.
	 *
	 * @param priority The required priority in case that exclusive write access is demanded. 
	 * Use lowest priority (@link Priority.PRIO_LOWEST) for shared access and read-only access.
	 * @param accessMode The required access mode.
	 * @throws SecurityException
	 *             if the calling application has no write permission on the resource,
	 *             or no permission to use the requested priority (???)
	 * @return true if required access mode was granted, false if write access was denied as another application has exclusive
	 *         write access to the resource with equal or higher priority
	 */
	boolean requestAccessMode(AccessMode accessMode, AccessPriority priority) throws SecurityException;

	/** Get AccessPriority active for the calling application
	 * @return the application's current AccessPriority for this resource */
	AccessPriority getAccessPriority();

	/** Get WriteAccessMode active for the calling application
	 * @return the application's current AccessMode for this resource */
	AccessMode getAccessMode();

	/**
	 * Gets the parent of the resource. The parent is defined by path, i.e.
	 * depends on the path the resource is accessed by.
	 * 
	 * @param <T>
	 * @return parent or null if a top-level resource
	 */
	<T extends Resource> T getParent();

	/**
	 * Get reference-parent resources of a given type that have a reference to the resource.
	 * Pass null-type or Resource.class to get all resources referencing this.
	 * 
	 * @param <T>
	 * @param parentType
	 *            resource type of parents. If given all elements returned are of this type or inherited types. If null
	 *            all referencing resources are returned.
	 * @return list of referencing resources, returns the direct parents of the references (may be empty)
	 */
	<T extends Resource> List<T> getReferencingResources(Class<T> parentType);

	/**
	 * Get all sub-resources including children connected via references
	 * 
	 * @param recursive
	 *            if true the entire tree below the resource is returned
	 * 
	 * @return
	 */
	List<Resource> getSubResources(boolean recursive);

	/**
	 * Get all direct sub-resources excluding children connected via references
	 * @param recursive return all sub-resources below this resources recursively
	 * @return this resource's direct sub-resources
	 */
	List<Resource> getDirectSubResources(boolean recursive);

	/**
	 * Check if resource was accessed via a reference
	 * 
	 * @param recursive
	 *            if true the entire path for obtaining the resource is checked for references, if false only the last
	 *            element of the path leading to the final resource is checked. Note that if isReference(true) detects
	 *            false the location and the path of the resource are the same.
	 * @return true if resource path check detects at least one reference, otherwise false
	 */
	boolean isReference(boolean recursive);

	/**
	 * Get a sub-resource or reference by its relative path.
	 * 
	 * @param <T>
	 * @param name
	 *            name is a path expression
	 * @return Returns the sub resource if it exists. Returns null if the demanded sub resource does not exist.
	 * @throws NoSuchResourceException if the name is not a valid OGEMA resource name.
	 */
	<T extends Resource> T getSubResource(String name) throws NoSuchResourceException;

	/**
	 * Gets all sub resources of a certain type, including referenced sub-resources.
	 * 
	 * @param <T>
	 * @param resourceType
	 * @param recursive
	 *            if true all resources of the give type are returned anywhere below, otherwise only direct children are
	 *            returned
	 * @return list of resource that may be empty if no fitting resources exist
	 */
	<T extends Resource> List<T> getSubResources(Class<T> resourceType, boolean recursive);

	/**
	 * Activate resource. Active resources are reported to demand listeners and
	 * resource listeners. To activate
	 * a resource, the application must have write access on it.
	 * 
	 * @param recursive
	 *            if true all sub-resources are also set active. Resources that are sub-resources only via references
	 *            are NOT activated.
	 * @throws SecurityException
	 *            if the resource, or one of its visible sub-resources in case of recursive==true, is
	 *             not writeable because of missing write permissions.
	 * @throws VirtualResourceException
	 *            if the resource is virtual. Virtual resources cannot be activated.
	 */
	void activate(boolean recursive) throws SecurityException, VirtualResourceException;

	/**
	 * Deactivate resource. Inactive resources are not reported via resource demand listeners. To
	 * deactivate a resource, an application needs write access to the resource. Calling this on
	 * a virtual ressource has no effect (virtual resources are always inactive).
	 * 
	 * @param recursive
	 *            if true all sub-resources are also set inactive. Resources that are sub-resources only via references
	 *            are NOT deactivated.
	 * @throws SecurityException
	 *            if the resource, or one of its visible sub-resources in case of recursive==true, is
	 *             not writeable because of missing write permissions.
	 */
	void deactivate(boolean recursive) throws SecurityException;

	/**
	 * Attach a resource as an optional element reference. If the optional element exists as a reference the reference
	 * is re-linked to the newElement, but the previosly-referenced resource is not deleted or changed. If the optional
	 * element existed as a sub-resource the existing sub-resource is deleted. Possibly-existing references to this
	 * resource are re-linked to the new Element.<br>
	 * The type of the reference must be assignable to the type of the optional element, otherwise a ResourceException
	 * is thrown. If assigning the reference would result in an invalid resource state, this is not changed and a
	 * ResourceException is thrown.
	 * 
	 * @param name
	 *            of the optional sub-element to which the resource shall be attached. For resources of type
	 *            {@link ResourceList} the name may be null. In this case the name will be generated by the
	 *            framework.
	 * @param newElement
	 *            resource to be attached.
	 * @throws NoSuchResourceException
	 *             if this resource's type does not contain a sub-resource with that name.
	 * @throws InvalidResourceTypeException
	 *             if the type of the referenced element is not assignable to the optional element's type.
	 * @throws ResourceGraphException
	 *             if performing the operation would cause an illegal state of the set of all resources in the OGEMA
	 *             system (e.g. due to reference-loops).
	 * @throws VirtualResourceException
	 *             if this or newElement are virtual.
	 * 
	 * @see #setAsReference(org.ogema.core.model.Resource) 
	 */
	void setOptionalElement(String name, Resource newElement) throws NoSuchResourceException,
			InvalidResourceTypeException, ResourceGraphException, VirtualResourceException;

	/**
	 * Add empty optional sub-resource according to the model specification. All sub-resources defined
	 * in the OGEMA data model (or in custom data models) are optional, so a sub-resource is always
	 * created with no sub-resources of its own.<br>
	 * If the optional element to be created exists as a reference the old reference is overwritten, but the referenced
	 * resource is not deleted or changed. If the optional element already exists as a sub-resource the existing sub
	 * resource is returned. If the resource is newly created it is provided inactive, otherwise the active-state is not
	 * changed.
	 * 
	 * @param name
	 *            of the optional sub-element to which the resource shall be attached. For resources of type
	 *            {@link ResourceList} the name may be null. In this case the name will be generated by the
	 *            framework.
	 * @return new sub-resource added. The new sub-resource is created in inactive state.
	 * @throws NoSuchResourceException
	 *             if this resource's type does not contain a sub-resource with that name.
	 * 
	 * @see #create() 
	 */
	Resource addOptionalElement(String name) throws NoSuchResourceException;

	/**
	 * Adds an empty decorator. Decorators are sub-resources that are not specified as
	 * optional elements. For this reason such sub-resources are not directly accessible 
	 * via optional element methods but only through {@link #getSubResource(java.lang.String) } etc.<br>
	 * If the name matches that of a defined optional sub-resource, a ResourceAlreadyExistsException is thrown, even if
	 * the optional element has not yet been created. If a decorator with the same name exists and its type is
	 * assignable to the requested type, that decorator is returned. If the type is incompatible, an exception is
	 * thrown. If the decorator is newly created it is provided inactive, otherwise the active-state is not changed.
	 * 
	 * @param <T> decorator type
	 * @param name
	 *            name of decorating sub-resource. For allowed names see
	 *            {@link org.ogema.core.resourcemanager.ResourceManagement#createResource(java.lang.String, java.lang.Class) }
	 * @param resourceType
	 *            resource type of decorator
	 * @return newly added sub-resource or compatible already-existing resource.
	 * @throws ResourceAlreadyExistsException
	 *             if the name is that of an optional element defined for this resource, or there already exists a
	 *             decorator with the same name and incompatible type.
	 * @throws NoSuchResourceException
	 *             if the name is not a legal resource name.
	 * @throws SecurityException
	 *             if the caller does not have write permission on this resource or the decorator exists but the caller
	 *             does not have read permissions for it.
	 */
	// TODO: correct permissions
	<T extends Resource> T addDecorator(String name, Class<T> resourceType) throws NoSuchResourceException,
			ResourceAlreadyExistsException;

	/**
	 * Add resource reference as a decorator.
	 * See {@link Resource#addDecorator(java.lang.String, java.lang.Class) addDecorator(String,Class)}
	 * regarding the definition of decorators.
	 * See {@link Resource#setOptionalElement(java.lang.String, org.ogema.core.model.Resource) setOptionalElement}
	 * regarding the behaviour of references.
	 * 
	 * @param decoratingResource
	 *            resource to be referenced as decorator.
	 * @param name
	 *            name of the new decorator sub-resource that shall contain the new reference. For names allowed see
	 *            {@link org.ogema.core.resourcemanager.ResourceManagement#createResource(java.lang.String, java.lang.Class) ResourceManagement.createResource}.
	 * @return the newly added decorator.
	 * @param <T>
	 *            the type of the decorator.
	 * @throws ResourceAlreadyExistsException
	 *             if the name is that of an optional element defined for this resource, or there already exists a
	 *             decorator with the same name and incompatible type.
	 * @throws NoSuchResourceException
	 *             if the name is not a legal resource name.
	 * @throws ResourceGraphException
	 *             if adding the reference would cause an invalid state of the set of all OGEMA resources in the system.
	 * @throws VirtualResourceException
	 *             if either this or decoratingResource are virtual.
	 */
	<T extends Resource> T addDecorator(String name, T decoratingResource) throws ResourceAlreadyExistsException,
			NoSuchResourceException, ResourceGraphException, VirtualResourceException;

	/**
	 * Delete optional child element, decorator child element or reference
	 * 
	 * @param name
	 *            name of sub-resource or reference to delete
	 * 
	 * @see #delete() 
	 */
	void deleteElement(String name);

	/**
	 * Tests if two resource objects refer to the same OGEMA resource, irrespective of the path used to arrive at the
	 * resource. Note: if resource references are used, two resources can have a common sub-resource, addressed by two
	 * different paths. To compare resources using only their path, call
	 * {@link Resource#equalsPath(org.ogema.core.model.Resource)} or the standard equals method.
	 * 
	 * @see Resource#getLocation()
	 * @see Resource#equalsPath(org.ogema.core.model.Resource) 
	 * 
	 * @param other the resource to compare with
	 * @return true if this Resource refers to the same OGEMA resource as other, false otherwise..
	 */
	boolean equalsLocation(Resource other);

	/**
	 * Tests if two Resource objects refer to the same OGEMA resource and are addressed via the same path,
	 * which is the same as using the default {@code equals} method. Note: if
	 * resource references are used, two resources can have a common sub-resource, addressed by two different paths. For an
	 * alternative comparison that checks if both Resource objects refer to the same location,
	 * see {@link #equalsLocation(org.ogema.core.model.Resource) }.
	 * 
	 * @see Resource#getPath() 
	 * @see Resource#equalsLocation(org.ogema.core.model.Resource) 
	 * 
	 * @param other the resource to compare with
	 * @return true if this and other represent the same resource and the same path to arrive at the resource, false
	 *          otherwise. If other does not represent a resource, false is returned.
	 */
	boolean equalsPath(Resource other);

	/**
	 * Returns true if the resource exists.
	 * @return true iff the resource exists
	 */
	boolean exists();

	/**
	 * Creates the resource. If this resource's parent is virtual create the parent resource first. Resources are 
	 * created inactive, in which state they are not found by resource demands. OGEMA does not provide default
	 * values for {@link SimpleResource}s. Applications must ensure that such resources contains sensible values
	 * before activating them. If an application cannot ensure this, it should probably not activate the resource
	 * (which would make the possibly non-sensible state visible to other applications).
	 * <br>
	 * If the resource already exists, simply return it.
	 * <br>
	 * In case this resource is a reference, this method will not create a new resource
	 * but instead will return the reference. Use {@link #setOptionalElement(java.lang.String, org.ogema.core.model.Resource) setOptionalElement}
	 * instead, or delete the reference first.
	 * 
	 * @param <T> generic for convenience only
	 * @return Returns the resource as it exists after creation.
	 * @throws NoSuchResourceException The resource could not be created because the specified path is no longer valid. This can occur when the resource already exists but is of a different
	 * resource type than requested or if the full path to the resource can no longer be parsed.
	 * The path of the virtual resource can no longer be resolved (e.g. another application created a resource in the path that has an incompatible type). The resource is not created.
	 */
	<T extends Resource> T create() throws NoSuchResourceException;

	/**
	 * Deletes the resource. If the resource is a reference, this removes only
	 * the reference, otherwise delete the resource and all its subresources.
	 * If the resource is virtual, this does nothing.
	 * 
	 * @see #deleteElement(java.lang.String) 
	 */
	void delete();

	/**
	 * Replace this resource, which must be an optional element of its parent, with a reference. This behaves like calling
	 * {@link #setOptionalElement(java.lang.String, org.ogema.model.Resource)}
	 * on the parent resource with the name of this resource and the given reference.
	 * Refer to {@link #setOptionalElement(java.lang.String, org.ogema.core.model.Resource) setOptionalElement}
	 * for possible exceptions.
	 * 
	 * @param <T> the type of the reference
	 * @param reference the resource to refer to.
	 * @return the new reference.
	 * @see #setOptionalElement(java.lang.String, org.ogema.core.model.Resource) 
	 */
	<T extends Resource> T setAsReference(T reference) throws NoSuchResourceException, ResourceGraphException,
			VirtualResourceException;

	/**
	 * Gets a sub-resource with that name and type, returning a virtual resource
	 * if no matching sub-resource exists yet.
	 * @param <T>
	 * @param name name of the requested sub-resource.
	 * @param type type of the sub-resource.
	 * @return a sub-resource with the requested name and type. If the sub-resource does not exist yet, this returns a virtual resource.
	 * @throws NoSuchResourceException if a sub-resource with that name already exists but has an incompatible type, or
	 *           the requested sub-resource is an optional element with an incompatible type.
	 * @throws NoSuchResourceException if the name is not a valid OGEMA resource name.
	 */
	<T extends Resource> T getSubResource(String name, Class<T> type) throws NoSuchResourceException;

}
