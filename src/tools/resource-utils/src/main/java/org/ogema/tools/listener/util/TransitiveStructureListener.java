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

import org.ogema.core.resourcemanager.ResourceStructureListener;

/**
 * Represents a {@link ResourceStructureListener} that is registered for all suitable subresources
 * of a given resource. More precisely, besides the usual resourceStructureChanged callbacks, it 
 * receives in addition callbacks of type EventType#SUBRESOURCE_ADDED and 
 * EventType#SUBRESOURCE_REMOVED if a subresource somewhere below in the resource tree 
 * is added or removed.  
 */
public interface TransitiveStructureListener {

	/**
	 * unregister all value listener registrations
	 */
	public void destroy();
	
	/**
	 * Get the structure listener that is registered for the resourceStructureChanged callbacks 
	 * @return
	 */
	public ResourceStructureListener getStructureListener();
	
}
