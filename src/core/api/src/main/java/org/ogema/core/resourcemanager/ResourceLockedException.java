/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
