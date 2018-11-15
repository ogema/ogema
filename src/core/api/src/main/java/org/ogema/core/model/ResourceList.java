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
package org.ogema.core.model;

import java.util.List;

import org.ogema.core.model.array.FloatArrayResource;

/**
 * Resource type containing a list of separate child resources. In contrast to
 * the SimpleArrayResources each entry of the list is a resource in its own
 * right (in the SimpleArrayResources entries in the array are mere values).<br>
 * All resources within the list are of type T or types inherited from T. This
 * type is returned by {@link getElementType}. Elements of the array may be
 * resources located in the array or references to resources located somewhere
 * else.<br>
 * All methods of {@link org.ogema.core.model.Resource} affect the list
 * considering the elements of the list as children of the ResourceList.<br>
 * For arrays of simple resources usually the respective array type shall be
 * used. As an exception also an array of e.g. FloatResources grouped into a
 * ResourceList can be used in case every single float value in the array shall
 * have its own decorator, e.g. a schedule. This is not possible using
 * {@link FloatArrayResource}.<br>
 *
 * @param <T> Type of this array's elements.
 */
/*
 * TODO Adding optional elements and decorators is forwarded to all direct
 * children, but not to decorators.
 */
public interface ResourceList<T extends Resource> extends Resource {

	/**
	 * Get all elements of the list. The sequence of the elements is determined
	 * by the sequence of adding the elements. The set of elements returned is the same as 
	 * the one obtained from calling
	 * {@link org.ogema.core.model.Resource#getSubResources}({@link getElementType()},
	 * false), if the element type is set. If it is not set, an empty list is returned.
	 *
	 * @return list of elements in the list (direct subresources and
	 * references).
	 */
	List<T> getAllElements();

	/**
	 * Get number of elements in the list. If the list type is not set, 0 is returned.
	 *
	 * @return
	 */
	int size();

	/**
	 * Add reference to the list. This will add the reference as a decorator
	 * with an automatically generated name and return the decorator.
	 *
	 * @param reference The element to add to this array.
	 * @throws IllegalArgumentException if the type of reference is incompatible
	 * with this array's type.
	 */
	T add(T reference);

	/**
	 * Add direct child resource as element of the list
	 *
	 * @return reference to new element child resource
	 */
	T add();

	/**
	 * Add direct child resource as element of the list.
	 *
	 * @param type type of the new element.
	 * @param <S> concrete sub type of the new element.
	 * 
	 * @return reference to new element child resource
	 */
	<S extends T> S add(Class<S> type);

	/**
	 * Remove element from the list. If the element is a reference, only the
	 * reference is deleted, if it is a direct subresource, the entire resource
	 * is deleted. If a resource is referenced more than once, all references
	 * are deleted. If the resource is direct subresource, but also a reference,
	 * the direct subresouce as well as the references are deleted.
	 *
	 * @param element
	 * @throws IllegalStateException if this array's element type has not been
	 * set.
	 */
	void remove(T element);

	/**
	 * Get resource type of all elements of the list. Note: {@link Resource#getResourceType()
	 * } returns ResourceList, not the type of the elements of the list.
	 *
	 * @return The type of this ResourceList's elements, or null if the type has not been set
	 */
	Class<T> getElementType();

	/**
	 * Set type of resources in the list (relevant for top-level lists or lists 
	 * added as decorator only)
	 *
	 * @param resType resource type for all resources in the list. Must be set
	 * for decorators before the first element is accessed.<br>
	 * The method may only be called once for top-level lists or decorating lists 
	 * and may not be called for other lists at all, otherwise an 
	 * {@link IllegalStateException} is thrown (if the new type coincides with the 
	 * type already set, no exception is thrown, however). 	 
	 * @throws IllegalStateException if the type of this ResourceList elements
	 * has already been set.
	 */
	void setElementType(Class<? extends Resource> resType);

	/**
	 * Tests whether this list contains a given resource by comparing resource
	 * locations (see
	 * {@link Resource#equalsLocation(org.ogema.core.model.Resource)}).
	 *
	 * @param resource Resource to search for.
	 * @return {@code true} if this list contains the given resource.
	 */
	boolean contains(Resource resource);
}
