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
 * Observer interface for receiving notifications about resource structure changes.
 * 
 * Structure listeners are registered on a resource and will remain attached
 * to that resource path, as long as the corresponding top level resource is not deleted
 * (Creation/deletion of top level resources can be monitored with a {@link ResourceDemandListener}).
 */
public interface ResourceStructureListener {

	/**
	 * Callback method invoked when the structure of a resource on which this listener
	 * has been registered on is changed. The change causing the callback is described
	 * in the {@link ResourceStructureEvent}.
	 * @param event {@link ResourceStructureEvent} describing the event
	 */
	public void resourceStructureChanged(ResourceStructureEvent event);

}
