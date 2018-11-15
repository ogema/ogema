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
