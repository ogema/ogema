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

import org.ogema.core.model.Resource;

/**
 * Observer interface for receiving resource change notifications. Only active
 * Resources will generate resource change events.
 * 
 * @deprecated recursive change listeners considered harmful, use {@link ResourceValueListener} instead.
 */
@Deprecated
public interface ResourceListener {

	/**
	 * Callback method called when the resource the listener is registered on (or a sub-resource of the
	 * resource if recursive listening was set to true) has changed its value.
	 * @param resource The resource that changed its value. If the listener was registered recursively
	 * this can be a different resource than the resource the listener was registered on.
	 */
	public void resourceChanged(Resource resource);
}
