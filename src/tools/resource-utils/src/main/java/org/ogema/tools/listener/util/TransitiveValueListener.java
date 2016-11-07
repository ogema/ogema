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
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Represents a {@link ResourceValueListener} that is registered for all suitable subresources
 * of a given resource. 
 * @param <T>
 * 		The value listener is registered for all subresources of a type 
 * 		compatible with T; in the generic case, T = ValueResource.
 */
public interface TransitiveValueListener<T extends Resource> {

	/**
	 * unregister all value listener registrations
	 */
	public void destroy();
	
	/**
	 * Get the value listener that is registered for the valueChanged callbacks 
	 * @return
	 */
	public ResourceValueListener<T> getValueListener();
	
}
