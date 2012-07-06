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
package org.ogema.resourcemanager.impl.model;

import org.ogema.core.model.Resource;
import org.ogema.resourcetree.TreeElement;

/**
 * Defines the functionality for a resource that is connected to the OGEMA resource graph defined by the TreeElements.
 * This interface is accessible only from within the OGEMA Application Manager.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface ConnectedResource extends Resource {

	/**
	 * Gets the TreeElement this resource object represents.
	 */
	TreeElement getTreeElement();
}
