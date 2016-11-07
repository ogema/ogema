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
package org.ogema.tools.listener.util;

import org.ogema.core.model.Resource;

/**
 * Represents a combined ResourceStructureListener structure and 
 *  ResourceValueListener value listener registration 
 * 
 * @param <T>
 * @see org.ogema.core.resourcemanager.ResourceValueListener ResourceValueListener
 * @see org.ogema.core.resourcemanager.ResourceStructureListener ResourceStructureListener
 */
public interface RegisteredStructureValueListener<T extends Resource> {
	
	/**
	 * Unregister listeners.
	 */
	void destroy();
	
	/**
	 * Returns the listener that receives structure and value callbacks.
	 * @return
	 */
	StructureValueListener<T> getListener();

}
