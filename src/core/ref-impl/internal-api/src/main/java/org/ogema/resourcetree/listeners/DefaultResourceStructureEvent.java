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
package org.ogema.resourcetree.listeners;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;

/**
 *
 * @author jlapp
 */
public class DefaultResourceStructureEvent implements ResourceStructureEvent {

	private final EventType type;
	private final Resource source;
	private final Resource changedResource;

	public DefaultResourceStructureEvent(EventType type, Resource source, Resource changedResource) {
		this.type = type;
		this.source = source;
		this.changedResource = changedResource;
	}

	public static ResourceStructureEvent createResourceActivatedEvent(Resource r) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_ACTIVATED, r, r);
	}

	public static ResourceStructureEvent createResourceDeactivatedEvent(Resource r) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_DEACTIVATED, r, r);
	}

	public static ResourceStructureEvent createResourceAddedEvent(Resource parent, Resource child) {
		return new DefaultResourceStructureEvent(EventType.SUBRESOURCE_ADDED, parent, child);
	}

	public static ResourceStructureEvent createResourceRemovedEvent(Resource parent, Resource child) {
		return new DefaultResourceStructureEvent(EventType.SUBRESOURCE_REMOVED, parent, child);
	}

	public static ResourceStructureEvent createResourceCreatedEvent(Resource newResource) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_CREATED, newResource, newResource);
	}

	public static ResourceStructureEvent createResourceDeletedEvent(Resource deletedResource) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_DELETED, deletedResource, deletedResource);
	}

	@Override
	public EventType getType() {
		return type;
	}

	@Override
	public Resource getSource() {
		return source;
	}

	@Override
	public Resource getChangedResource() {
		return changedResource;
	}

    @Override
    public String toString() {
        return String.format("%s: source=%s changed=%s", type, source.getPath(), changedResource != null? changedResource.getPath() : "<na>");
    }

}
