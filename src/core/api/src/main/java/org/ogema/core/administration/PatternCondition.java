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
package org.ogema.core.administration;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

/**
 * Represents a {@link ResourcePattern} condition, i.e. a field of type
 * {@link Resource}. Mirrors some methods of {@link Resource}.
 * Access to a PatternCondition object requires read access to the corresponding resource. TODO
 *
 */
public interface PatternCondition {

	/**
	 * Returns the pattern field name.
	 * @return
	 */
	String getFieldName();
	
	/**
	 * Is the condition satisfied?
	 * @return
	 */
	boolean isSatisfied();

	/**
	 * Is the resource required to exist and be active?
	 * @return
	 */
	boolean isOptional();
	
	/**
	 * @see Resource#getResourceType()	
	 * @return
	 */
	Class<? extends Resource> getResourceType();
	
	/**
	 * @see Resource#exists()
	 * @return
	 */
	boolean exists();
	
	/**
	 * @see Resource#isActive()
	 * @return
	 */
	boolean isActive();
	
	/**
	 * @see Resource#isReference(boolean) Resource#isReference(true)
	 * @return
	 */
	boolean isReference();
	
	/**
	 * @see Resource#getPath()
	 * @return
	 */
	String getPath();
	
	/**
	 * @see Resource#getLocation()
	 * @return
	 */
	String getLocation();
	
	/**
	 * @see Resource#getAccessMode()
	 * @return
	 */
	AccessMode getAccessMode();
	
	/**
	 * See individual @see SingleValueResource methods #getValue().
	 * Returns null if the resource is not of type SingleValueResource.
	 * @return
	 */
	Object getValue();
	
}
