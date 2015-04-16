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
package org.ogema.core.resourcemanager;

import org.ogema.core.model.Resource;

/**
 * Observer interface for receiving resource change notifications. Only active
 * Resources will generate resource change events.
 * 
 * @deprecated recursive change listeners considered harmful, use {@link ResourceValueListener} instead.
 */
@Deprecated
public interface ResourceListener {

	/**
	 * Callback method called when the resource the listener is registered on (or a sub-resource of the
	 * resource if recursive listening was set to true) has changed its value.
	 * @param resource The resource that changed its value. If the listener was registered recursively
	 * this can be a different resource than the resource the listener was registered on.
	 */
	public void resourceChanged(Resource resource);
}
