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
package org.ogema.core.rads.change;

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
		final CompoundResourceEvent<?> e = active ? DefaultCompoundEvent.createResourceActivatedEvent(target)
				: DefaultCompoundEvent.createResourceDeactivatedEvent(target);
		patternListener.trigger(e);
	}

    @Override
	public void queueResourceCreatedEvent(String path) {
		if (!path.equals(target.getPath())) {
			return;
		}
        virtualResource = false;
		patternListener.trigger(DefaultCompoundEvent.createResourceCreatedEvent(target));
	}

    @Override
	public void queueResourceDeletedEvent() {
        virtualResource = true;
		patternListener.trigger(DefaultCompoundEvent.createResourceDeletedEvent(target));
	}

    @Override
	public void queueSubResourceAddedEvent(final TreeElement subresource) {
		Resource subResource = findResource(subresource, target);
		patternListener.trigger(DefaultCompoundEvent.createResourceAddedEvent(target, subResource));
	}

    @Override
	public void queueSubResourceRemovedEvent(final TreeElement subresource) {
		Resource subResource = findResource(subresource, target);
		assert subResource != null;
		patternListener.trigger(DefaultCompoundEvent.createResourceRemovedEvent(target, subResource));
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
				: CompoundEventType.REFERENCE_REMOVED, target, referencingResource, null, null, false));
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
	
}
