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
 * Describes an event associated to the change of a resource's structure. An instance implementing this interface is
 * passed to the callback methods of a {@link ResourceStructureListener}.
 */
public interface ResourceStructureEvent {

	public static enum EventType {

		/**
		 * A resource has been created under the path that the listener has been registered on.
		 */
		RESOURCE_CREATED,
		/**
		 * The resource the structure listener has been registered on has been deleted.
		 */
		RESOURCE_DELETED,
		/**
		 * The resource (that the listener has been registered on) has been activated.
		 */
		RESOURCE_ACTIVATED,
		/**
		 * The resource (that the listener has been registered on) has been de-activated.
		 */
		RESOURCE_DEACTIVATED,
		/**
		 * A sub-resource has been added. The respective sub-resource can be read via {@link #getChangedResource()}.
		 */
		SUBRESOURCE_ADDED,
		/**
		 * A sub-resource has been removed. The respective sub-resource can be read via {@link #getChangedResource()}.
		 */
		SUBRESOURCE_REMOVED,
		/**
		 * The resource has been added as a reference, the source of the reference
		 * is available via {@link #getChangedResource() }.
		 */
		REFERENCE_ADDED,
		/**
		 * A reference to the resource has been removed, the former source of the reference
		 * is available via {@link #getChangedResource() }, unless it has been deleted.
		 */
		REFERENCE_REMOVED
	}

	/**
	 * @return The type of event that caused the callback.
	 */
	public EventType getType();

	/**
	 * @return Source of this event, will be null exactly if {@link #getType} is {@link EventType#RESOURCE_DELETED}
	 */
	public Resource getSource();

	/**
	 * @return the updated/added resource for events of type RESOURCE_ACTIVATED, RESOURCE_DEACTIVATED, 
	 * SUBRESOURCE_ADDED or SUBRESOURCE_REMOVED.
	 *         
	 */
	public Resource getChangedResource();
}
