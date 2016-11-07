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
