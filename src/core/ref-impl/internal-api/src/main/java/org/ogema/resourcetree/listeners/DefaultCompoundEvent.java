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

import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean active;

	public DefaultCompoundEvent(CompoundEventType type, Resource source, Resource changedResource, T previousValue, T updateValue, 
			boolean valueChanged, AtomicBoolean active) {
		this.type = type;
		this.source = source;
		this.changedResource = changedResource;
        this.previousValue = previousValue;
        this.updateValue = updateValue;
        this.valueChanged = valueChanged;
        this.active = active;
	}

	public static CompoundResourceEvent<Void> createResourceActivatedEvent(Resource r, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_ACTIVATED, r, r, null, null, false, active);
	}

	public static CompoundResourceEvent<Void> createResourceDeactivatedEvent(Resource r, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_DEACTIVATED, r, r, null, null, false, active);
	}

	public static CompoundResourceEvent<Void> createResourceAddedEvent(Resource parent, Resource child, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.SUBRESOURCE_ADDED, parent, child, null, null, false, active);
	}

	public static CompoundResourceEvent<Void> createResourceRemovedEvent(Resource parent, Resource child, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.SUBRESOURCE_REMOVED, parent, child, null, null, false, active);
	}

	public static CompoundResourceEvent<Void> createResourceCreatedEvent(Resource newResource, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_CREATED, newResource, newResource, null, null, false, active);
	}

	public static CompoundResourceEvent<Void> createResourceDeletedEvent(Resource deletedResource, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_DELETED, deletedResource, deletedResource, null, null, false, active);
	}
    
    public static <T> CompoundResourceEvent<T> createResourceUpdatedEvent(Resource updatedResource, T previousValue, T updateValue, boolean valueChanged, AtomicBoolean active) {
		return new DefaultCompoundEvent<>(CompoundEventType.RESOURCE_DELETED, updatedResource, updatedResource, previousValue, updateValue, valueChanged, active);
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
    
    public boolean isActive() {
    	return active.get();
    }

}
