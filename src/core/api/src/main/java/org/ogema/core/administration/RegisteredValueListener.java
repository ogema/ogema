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
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Representation of a value listener registered by an application.
 */
public interface RegisteredValueListener {

	/**
	 * Gets the resource that the listener is connected to.
	 * @return a resource object that represents the resource at is seen by the
	 * application returned by {@link #getApplication()}. That is, calling 
	 * {@link Resource#getAccessMode()} returns the application's access mode.
	 */
	Resource getResource();

	/**
	 * Application that connected the listener to the resource.
	 * @return application which has registered the listener.
	 */
	AdminApplication getApplication();

	/**
	 * Gets the listener that is informed about changes in the resource's value.
	 * @return the registered listener.
	 */
	<T extends Resource> ResourceValueListener<T> getValueListener();

	/**
	 * Has the listener been registered to receive callbacks on every update?
	 */
	boolean isCallOnEveryUpdate();

}
