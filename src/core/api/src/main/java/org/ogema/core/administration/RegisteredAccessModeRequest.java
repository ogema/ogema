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
import org.ogema.core.resourcemanager.AccessPriority;

/**
 * Representation of an access mode request
 */
public interface RegisteredAccessModeRequest {

	/**
	 * Gets the resource that the listener is connected to.
	 *
	 * @return a resource object that represents the resource at is seen by the
	 * application returned by {@link #getApplication()}. That is, calling
	 * {@link Resource#getAccessMode()} returns the application's access mode.
	 */
	Resource getResource();

	/**
	 * Gets the administrator access to the application that registered the
	 * demand.
	 */
	AdminApplication getApplication();

	/**
	 * Gets the access mode that is required.
	 */
	AccessMode getRequiredAccessMode();

	/**
	 * Gets the priority with which the access mode has been demanded.
	 */
	AccessPriority getPriority();

	/**
	 * Checks if the access mode is fulfilled.
	 *
	 * @return true if the application has been granted the requested access,
	 * false if not.
	 */
	boolean isFulfilled();
}
