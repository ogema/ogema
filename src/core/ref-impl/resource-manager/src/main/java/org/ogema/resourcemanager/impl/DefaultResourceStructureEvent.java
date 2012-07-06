/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.resourcemanager.impl;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;

/**
 *
 * @author jlapp
 */
public class DefaultResourceStructureEvent implements ResourceStructureEvent {

	final EventType type;
	final Resource source;
	final Resource changedResource;

	public DefaultResourceStructureEvent(EventType type, Resource source, Resource changedResource) {
		this.type = type;
		this.source = source;
		this.changedResource = changedResource;
	}

	static ResourceStructureEvent createResourceActivatedEvent(Resource r) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_ACTIVATED, r, r);
	}

	static ResourceStructureEvent createResourceDeactivatedEvent(Resource r) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_DEACTIVATED, r, r);
	}

	static ResourceStructureEvent createResourceAddedEvent(Resource parent, Resource child) {
		return new DefaultResourceStructureEvent(EventType.SUBRESOURCE_ADDED, parent, child);
	}

	static ResourceStructureEvent createResourceRemovedEvent(Resource parent, Resource child) {
		return new DefaultResourceStructureEvent(EventType.SUBRESOURCE_REMOVED, parent, child);
	}

	static ResourceStructureEvent createResourceCreatedEvent(Resource newResource) {
		return new DefaultResourceStructureEvent(EventType.RESOURCE_CREATED, newResource, newResource);
	}

	static ResourceStructureEvent createResourceDeletedEvent(Resource deletedResource) {
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

}
