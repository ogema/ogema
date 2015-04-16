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
import org.ogema.core.resourcemanager.ResourceStructureListener;

/**
 * Representation of a resource structure listener registered by an application.
 */
public interface RegisteredStructureListener {

	/**
	 * Gets the resource that the listener is connected to.
	 * @return a resource object that represents the resource at is seen by the
	 * application returned by {@link #getApplication()}. That is, calling 
	 * {@link Resource#getAccessMode()} returns the application's access mode.
	 */
	Resource getResource();

	/**
	 * Application that connected the listener to the resource.
	 */
	AdminApplication getApplication();

	/**
	 * Gets the methods that is informed about changes in the resource's value.
	 */
	ResourceStructureListener getListener();
}
