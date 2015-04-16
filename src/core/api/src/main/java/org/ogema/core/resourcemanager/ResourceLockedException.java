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

/**
 * Thrown if a resource in the system such as a time series set is already in use and thus cannot be accessed in the way
 * requested
 * 
 * @author David Nestle
 * @version %I%, %G% %U%
 */

/*
 * - Resources can be fully or partially (individual nodes) locked-
 */
/*
 * - In the case of resourceTypes means locked only that resources of this type exist, and therefore, a delete of the
 * resource type is not allowed.
 */
/*
 * - The Lock status for the resources can not be maintained persistent but only at runtime.
 */
/*
 * - Delete a resource is not allowed if the resource itself, or at least one of its SubResource is marked as locked.
 */
/*
 * - Locked resources can be read but not written. So, the RsourceLockedException be thrown by the setter methods. But
 * this is controlled by the ResourceAdministration, so that the ResourceDB doesn't need to check the locked status
 * before setting a resource value. But in case of deleting of ResourceTypes ResourceDB has to check if resources exist
 * from the type to be deleted. In this case ResourceLockedException is to be thrown.
 */
public class ResourceLockedException extends ResourceException {
	/**
	 * @see java.lang.RuntimeException
	 */
	private static final long serialVersionUID = 8869818216622404042L;

	/**
	 * @see java.lang.RuntimeException
	 */
	public ResourceLockedException(String message) {
		super(message);
	}

	/**
	 * @see java.lang.RuntimeException
	 */
	public ResourceLockedException(String message, Throwable cause) {
		super(message, cause);
	}
}
