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
package org.ogema.resourcemanager.impl;

import java.util.concurrent.Callable;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.resourcetree.TreeElement;
import org.ogema.resourcetree.listeners.DefaultResourceStructureEvent;
import org.ogema.resourcetree.listeners.InternalStructureListenerRegistration;

/**
 *
 * @author jlapp
 */
public class StructureListenerRegistration extends InternalStructureListenerRegistration {

	final Resource resource;

	final ResourceStructureListener listener;

	final ApplicationManager appman;

	public StructureListenerRegistration(Resource resource, ResourceStructureListener listener,
			ApplicationManager appman) {
		this.resource = resource;
		this.listener = listener;
		this.appman = appman;
        virtualRegistration = !resource.exists();
	}
    
    /**
     the registration is sitting on a virtual resource, or has reported the resource as deleted
     */
    private boolean virtualRegistration; //XXX bad name
    
    public boolean isVirtualRegistration() {
        return virtualRegistration;
    }

	public void queueEvent(final ResourceStructureEvent e) {
		Callable<Boolean> c = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				ResourceDBManager dbMan = ((ApplicationResourceManager) appman.getResourceManagement())
						.getDatabaseManager();
				// FIXME is this really necessary?
				// get the structure lock and release it to insure that the action which triggered this event is completed
				dbMan.lockStructureWrite();  // XXX why isn't it sufficient to lock structure read?
				dbMan.unlockStructureWrite();
				if (!isActive())
					return false;
				listener.resourceStructureChanged(e);
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
        virtualRegistration = false;
		queueEvent(DefaultResourceStructureEvent.createResourceCreatedEvent(resource));
	}

	public void queueResourceDeletedEvent() {
        virtualRegistration = true;
		queueEvent(DefaultResourceStructureEvent.createResourceDeletedEvent(resource));
	}

	public void queueSubResourceAddedEvent(final TreeElement subresource) {
		try {
			Resource subResource = findResource(subresource, resource);
			queueEvent(DefaultResourceStructureEvent.createResourceAddedEvent(resource, subResource));
		} catch (SecurityException expected) {
			appman.getLogger().debug("Callback for sub resource added denied by security: {}", expected.getMessage());
		}
	}

	public void queueSubResourceRemovedEvent(final TreeElement subresource) {
		try {
			Resource subResource = findResource(subresource, resource);
			assert subResource != null;
			queueEvent(DefaultResourceStructureEvent.createResourceRemovedEvent(resource, subResource));
		} catch (SecurityException expected) {
			appman.getLogger().debug("Callback for sub resource removed denied by security: {}", expected.getMessage());
		}
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

    @Override
	public void queueReferenceChangedEvent(final TreeElement referencingElement, final boolean added) {
		String resourcePath = referencingElement.getPath().replace('.', '/');
        try {
            Resource referencingResource = appman.getResourceAccess().getResource(resourcePath);
            assert referencingResource != null : "no such resource: " + resourcePath;
            if (referencingResource.equals(resource.getParent())) {
                // spurious call when a virtual resource is replaced by a reference.
                // hard to detect where it actually happens so it's filtered here.
                return;
            }
            queueEvent(new DefaultResourceStructureEvent(added ? ResourceStructureEvent.EventType.REFERENCE_ADDED
                    : ResourceStructureEvent.EventType.REFERENCE_REMOVED, resource, referencingResource));
        } catch (SecurityException se) {
            appman.getLogger().debug("Callback for reference change denied by security: {}", se.getMessage());
        }
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
	
	@Override
	public ApplicationManager getApplicationManager() {
		return appman;
	}
	
}
