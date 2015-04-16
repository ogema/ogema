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
import org.ogema.core.resourcemanager.ResourceDemandListener;

/** 
 * Representation of a resource demand registered by an application for administration purposes 
 */
public interface RegisteredResourceDemand {

	/** 
	 * Gets the administrator access to the application that registered the demand.
	 * @return admin access of registering application
	 */
	AdminApplication getApplication();

	/**
	 * Gets the resource type demanded.
	 * @return resource type of the demand
	 */
	Class<? extends Resource> getTypeDemanded();

	/**
	 * Gets the listener that is informed about new resources of the demanded type.
	 * @return the listener for this demand
	 */
	ResourceDemandListener<?> getListener();
}
