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
package org.ogema.core.rads.change;

import java.util.concurrent.atomic.AtomicBoolean;

import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.CompoundResourceEvent.CompoundEventType;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.resourcetree.TreeElement;
import org.ogema.resourcetree.listeners.DefaultCompoundEvent;
import org.ogema.resourcetree.listeners.InternalStructureListenerRegistration;

// FIXME mainly a copy of StructureListenerRegistration
// only implements ResourceStructureListener so that it can be registered via the API methods
public class PatternStructureChangeListener extends InternalStructureListenerRegistration implements ResourceStructureListener {
	
	private final PatternChangeListenerRegistration patternListener;
	private final Resource target;
	private final ApplicationManager am;
	private boolean virtualResource;
	private final AtomicBoolean active = new AtomicBoolean(true);
	
	public PatternStructureChangeListener(PatternChangeListenerRegistration patternListener, Resource resource) {
		this.patternListener = patternListener;
		this.target = resource;
		this.am = patternListener.getApplicationManager();
		this.virtualResource = !resource.exists();
	}

	@Override
	public Resource getResource() {
		return target;
	}

	@Override
	public AdminApplication getApplication() {
		return am.getAdministrationManager().getAppById(am.getAppID().getIDString());
	}

	@Override
	public ResourceStructureListener getListener() {
		return this;
	}

	@Override
	public boolean isVirtualRegistration() {
		return virtualResource;
	}

    @Override
	public void queueActiveStateChangedEvent(final boolean active) {
		final CompoundResourceEvent<?> e = active ? DefaultCompoundEvent.createResourceActivatedEvent(target, this.active)
				: DefaultCompoundEvent.createResourceDeactivatedEvent(target, this.active);
		patternListener.trigger(e);
	}

    @Override
	public void queueResourceCreatedEvent(String path) {
		if (!path.equals(target.getPath())) {
			return;
		}
        virtualResource = false;
		patternListener.trigger(DefaultCompoundEvent.createResourceCreatedEvent(target, active));
	}

    @Override
	public void queueResourceDeletedEvent() {
        virtualResource = true;
		patternListener.trigger(DefaultCompoundEvent.createResourceDeletedEvent(target, active));
	}

    @Override
	public void queueSubResourceAddedEvent(final TreeElement subresource) {
    	try {
    		Resource subResource = findResource(subresource, target);
    		patternListener.trigger(DefaultCompoundEvent.createResourceAddedEvent(target, subResource, active));
    	} catch (SecurityException expected) {}
	}

    @Override
	public void queueSubResourceRemovedEvent(final TreeElement subresource) {
    	try {
    		Resource subResource = findResource(subresource, target);
    		assert subResource != null;
    		patternListener.trigger(DefaultCompoundEvent.createResourceRemovedEvent(target, subResource, active));
    	} catch (SecurityException expected) {}
	}
	
    @Override
	public void queueReferenceChangedEvent(final TreeElement referencingElement, final boolean added) {
		String resourcePath = referencingElement.getPath().replace('.', '/');
		Resource referencingResource = am.getResourceAccess().getResource(resourcePath);
		assert referencingResource != null : "no such resource: " + resourcePath;
		if (referencingResource.equals(target.getParent())) {
			// spurious call when a virtual resource is replaced by a reference.
			// hard to detect where it actually happens so it's filtered here.
			return;
		}
		patternListener.trigger(new DefaultCompoundEvent<>(added ? CompoundEventType.REFERENCE_ADDED
				: CompoundEventType.REFERENCE_REMOVED, target, referencingResource, null, null, false, active));
	}

	@Override
	public ApplicationManager getApplicationManager() {
		return am;
	}
	
	/** find subresource relative to source... */
	private Resource findResource(final TreeElement subresource, Resource source) throws NoSuchResourceException,
			SecurityException {
		TreeElement el = subresource;
		StringBuilder sb = new StringBuilder();
		do {
			sb.insert(0, el.getName());
			sb.insert(0, "/");
			el = el.getParent();
		} while (el != null);
		Resource addedResource = am.getResourceAccess().getResource(sb.toString());
		assert addedResource != null;
		Resource subResource = source.getSubResource(addedResource.getName());
		if (subResource == null || !subResource.equalsLocation(addedResource)) {
			subResource = null;
			for (Resource child : source.getSubResources(false)) {
				if (child.equalsLocation(addedResource)) {
					subResource = child;
					break;
				}
			}
			assert subResource != null;
		}
		return subResource;
	}

	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		throw new UnsupportedOperationException("PatternStructureChangeListener does not expect any callbacks");
	}
	
	@Override
	public void dispose() {
		active.set(false);
	}
	
	@Override
	public boolean isActive() {
		return active.get();
	}
	
}
