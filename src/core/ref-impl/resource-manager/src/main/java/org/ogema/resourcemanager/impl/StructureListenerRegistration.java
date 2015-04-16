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
package org.ogema.resourcemanager.impl;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.resourcetree.TreeElement;

/**
 *
 * @author jlapp
 */
public class StructureListenerRegistration implements RegisteredStructureListener {

	final Resource resource;

	final ResourceStructureListener listener;

	final ApplicationManager appman;

	public StructureListenerRegistration(Resource resource, ResourceStructureListener listener,
			ApplicationManager appman) {
		this.resource = resource;
		this.listener = listener;
		this.appman = appman;
	}

	public void queueEvent(final ResourceStructureEvent e) {
		Callable<Boolean> c = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				ReadWriteLock structureLock = ((ApplicationResourceManager) appman.getResourceManagement())
						.getDatabaseManager().getStructureLock();
				//XXX holding lock during callback...
				structureLock.writeLock().lock();
				try {
					listener.resourceStructureChanged(e);
				} finally {
					structureLock.writeLock().unlock();
				}
				return true;
			}
		};
		appman.submitEvent(c);
	}

	public void queueActiveStateChangedEvent(final boolean active) {
		final ResourceStructureEvent e = active ? DefaultResourceStructureEvent.createResourceActivatedEvent(resource)
				: DefaultResourceStructureEvent.createResourceDeactivatedEvent(resource);
		queueEvent(e);
	}

	public void queueResourceCreatedEvent(String path) {
		if (!path.equals(resource.getPath())) {
			return;
		}
		queueEvent(DefaultResourceStructureEvent.createResourceCreatedEvent(resource));
	}

	public void queueResourceDeletedEvent() {
		queueEvent(DefaultResourceStructureEvent.createResourceDeletedEvent(resource));
	}

	public void queueSubResourceAddedEvent(final TreeElement subresource) {
		Resource subResource = findResource(subresource, resource);
		queueEvent(DefaultResourceStructureEvent.createResourceAddedEvent(resource, subResource));
	}

	public void queueSubResourceRemovedEvent(final TreeElement subresource) {
		Resource subResource = findResource(subresource, resource);
		assert subResource != null;
		queueEvent(DefaultResourceStructureEvent.createResourceRemovedEvent(resource, subResource));
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
		Resource addedResource = appman.getResourceAccess().getResource(sb.toString());
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

	public void queueReferenceChangedEvent(final TreeElement referencingElement, final boolean added) {
		String resourcePath = referencingElement.getPath().replace('.', '/');
		Resource referencingResource = appman.getResourceAccess().getResource(resourcePath);
		assert referencingResource != null : "no such resource: " + resourcePath;
		if (referencingResource.equals(resource.getParent())) {
			// spurious call when a virtual resource is replaced by a reference.
			// hard to detect where it actually happens so it's filtered here.
			return;
		}
		queueEvent(new DefaultResourceStructureEvent(added ? ResourceStructureEvent.EventType.REFERENCE_ADDED
				: ResourceStructureEvent.EventType.REFERENCE_REMOVED, resource, referencingResource));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(resource.getPath());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StructureListenerRegistration other = (StructureListenerRegistration) obj;
		if (!Objects.equals(this.resource, other.resource)) {
			return false;
		}
		if (!Objects.equals(this.listener, other.listener)) {
			return false;
		}
		return Objects.equals(this.appman, other.appman);
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public AdminApplication getApplication() {
		return appman.getAdministrationManager().getAppById(appman.getAppID().getIDString());
	}

	@Override
	public ResourceStructureListener getListener() {
		return listener;
	}

}
