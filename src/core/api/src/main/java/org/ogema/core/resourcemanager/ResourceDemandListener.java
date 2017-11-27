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
 * Listener for standard resource demands. Resource demands are issued on a resource
 * type. For each resource becoming available that is of the issued type the listener
 * is called once. The path reported in the callback always is the resource location -
 * no additional callbacks are issued for other paths under which the resource may be
 * available. If the resource becomes unavailable, the resourceUnavailable callback is
 * issued if the resource was available previously. That is, subsequent deactivation and
 * deletion of a resource will issue at most one resourceUnavailable callback: The one
 * for the deactivation of the resource.
 * 
 * @param <T> type of the demanded resource.
 */
public interface ResourceDemandListener<T extends Resource> {

	/**
	 * Indicates the reason why access to a resource has been lost. Used in resourceUnavaible
	 * callbacks.
	 */
	public enum AccessLossReason {
		/**
		 * Resource was set to inactive, which usually is done to indicate that
		 * if the resource exists no sensible values should be expected in it and
		 * no effect of writing to it should be expected.
		 */
		RESOURCE_INACTIVE,
		/**
		 * The resource has been or is about to be deleted.
		 */
		RESOURCE_DELETED
	}

	/**
	 * Application notification when a resource fitting a resource demand is available
	 * 
	 * @param resource
	 *            reference to resource fitting demand. The path of the resource equals
	 *          the resource's location.
	 */
	void resourceAvailable(T resource);

	/**
	 * Application notification when access to a resource demanded is lost or reduced
	 * 
	 * @param resource
	 *            reference to resource fitting demand. The path of the resource equals
	 *          the resource's location.
	 */
	void resourceUnavailable(T resource);
}
