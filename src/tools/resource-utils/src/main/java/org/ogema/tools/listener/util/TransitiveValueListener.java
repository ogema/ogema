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
