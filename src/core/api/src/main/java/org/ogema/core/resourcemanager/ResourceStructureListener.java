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
package org.ogema.core.resourcemanager;

/**
 * Observer interface for receiving notifications about resource structure changes.
 * 
 * Structure listeners are registered on a resource and will remain attached
 * to that resource path, as long as the corresponding top level resource is not deleted
 * (Creation/deletion of top level resources can be monitored with a {@link ResourceDemandListener}).
 */
public interface ResourceStructureListener {

	/**
	 * Callback method invoked when the structure of a resource on which this listener
	 * has been registered on is changed. The change causing the callback is described
	 * in the {@link ResourceStructureEvent}.
	 * @param event {@link ResourceStructureEvent} describing the event
	 */
	public void resourceStructureChanged(ResourceStructureEvent event);

}
