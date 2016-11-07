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
package org.ogema.resourcetree.listeners;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.CompoundResourceEvent;

/**
 * @param <T> value type for resource change events.
 * @author jlapp
 */
public class DefaultCompoundEvent<T> implements CompoundResourceEvent<T> {

	private final CompoundEventType type;
	private final Resource source;
	private final Resource changedResource;
    private final boolean valueChanged;
    private final T previousValue;
    private final T updateValue;
    

	public DefaultCompoundEvent(CompoundEventType type, Resource source, Resource changedResource, T previousValue, T updateValue, boolean valueChanged) {
		this.type = type;
		this.source = source;
		this.changedResource = changedResource;
        this.previousValue = previousValue;
        this.updateValue = updateValue;
        this.valueChanged = valueChanged;
	}

	public static CompoundResourceEvent<Void> createResourceActivatedEvent(Resource r) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_ACTIVATED, r, r, null, null, false);
	}

	public static CompoundResourceEvent<Void> createResourceDeactivatedEvent(Resource r) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_DEACTIVATED, r, r, null, null, false);
	}

	public static CompoundResourceEvent<Void> createResourceAddedEvent(Resource parent, Resource child) {
		return new DefaultCompoundEvent<>(CompoundEventType.SUBRESOURCE_ADDED, parent, child, null, null, false);
	}

	public static CompoundResourceEvent<Void> createResourceRemovedEvent(Resource parent, Resource child) {
		return new DefaultCompoundEvent<>(CompoundEventType.SUBRESOURCE_REMOVED, parent, child, null, null, false);
	}

	public static CompoundResourceEvent<Void> createResourceCreatedEvent(Resource newResource) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_CREATED, newResource, newResource, null, null, false);
	}

	public static CompoundResourceEvent<Void> createResourceDeletedEvent(Resource deletedResource) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_DELETED, deletedResource, deletedResource, null, null, false);
	}
    
    public static <T> CompoundResourceEvent<T> createResourceUpdatedEvent(Resource updatedResource, T previousValue, T updateValue, boolean valueChanged) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_DELETED, updatedResource, updatedResource, previousValue, updateValue, valueChanged);
	}

	@Override
	public CompoundEventType getType() {
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
    public boolean isValueChanged() {
        return valueChanged;
    }
    
    @Override
    public T getPreviousValue() {
        return previousValue;
    }

    @Override
    public T getUpdateValue() {
        return updateValue;
    }

    @Override
    public String toString() {
        return String.format("%s: source=%s changed=%s", type, source.getPath(), changedResource != null? changedResource.getPath() : "<na>");
    }

}
