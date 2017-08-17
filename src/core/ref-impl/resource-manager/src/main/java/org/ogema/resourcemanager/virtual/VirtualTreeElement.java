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
package org.ogema.resourcemanager.virtual;

import org.ogema.core.model.Resource;
import org.ogema.resourcetree.TreeElement;

/**
 *
 * @author jlapp
 */
public interface VirtualTreeElement extends TreeElement {

	boolean isVirtual();

	boolean create();

	/**
	 * Return child with given name and type, allows navigation across vritual
	 * decorators.
	 * 
	 * @param name
	 * @param type
	 * @return child element
	 */
	VirtualTreeElement getChild(String name, Class<? extends Resource> type);

	@Override
	VirtualTreeElement getChild(String name);
    
    /**
     * Returns only non-virtual subresources, does not create virtual child elements.
     * @param name
     * @return a non-virtual child element or null
     */
    VirtualTreeElement getExistingChild(String name);

	void delete();
    
    /**
     * Replace the type of this TreeElement with a sub type of the current type.
     * Only works on a virtual element.
     * @throws IllegalStateException if thus element already exists and has a different type.
     */
    void constrainType(Class<? extends Resource> type);
    
}
