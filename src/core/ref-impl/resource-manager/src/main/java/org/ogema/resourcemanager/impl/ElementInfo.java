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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import org.ogema.core.administration.RegisteredAccessModeRequest;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;

import static org.ogema.core.resourcemanager.AccessMode.EXCLUSIVE;
import static org.ogema.core.resourcemanager.AccessMode.READ_ONLY;
import static org.ogema.core.resourcemanager.AccessMode.SHARED;
import org.ogema.core.resourcemanager.AccessModeListener;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.persistence.impl.faketree.ScheduleTreeElement;
import org.ogema.resourcemanager.impl.timeseries.DefaultRecordedData;
import org.ogema.resourcetree.TreeElement;
import org.slf4j.LoggerFactory;

/**
 * Info elemnt set on {@link TreeElement}, adding info on resource demands,
 * existing locks and listener registrations. TreeElement and ElementInfo have a
 * n:1 relationship since a TreeElement shares its ElementInfo with its
 * referencing nodes.
 *
 * @author jlapp
 */
public class ElementInfo {

	final ResourceDBManager man;

	// TreeElements which have a reference to this element.
	private Collection<TreeElement> references;

	private Collection<Object> listeners;

	static final int INITIAL_ACCESSREQUESTS_QUEUE_SIZE = 5;
	PriorityQueue<AccessModeRequest> accessRequests;

	/**
	 * Schedule tree element wrapping the tree element, in case the element
	 * represents a schedule.
	 */
	ScheduleTreeElement scheduleTreeElement = null;

	public ElementInfo(ResourceDBManager man, TreeElement el) {
		Objects.requireNonNull(man);
		Objects.requireNonNull(el);
		this.man = man;
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection<StructureListenerRegistration> getStructureListeners() {
		if (listeners == null) {
			return Collections.EMPTY_LIST;
		}
		return getListeners(StructureListenerRegistration.class);
	}

	private synchronized <T> Collection<T> getListeners(Class<T> type) {
        if (listeners == null) {
            return Collections.emptyList();
        }
        Collection<T> listenersOfRequestedType = new ArrayList<>();
        for (Object listener : listeners) {
            if (type.isAssignableFrom(listener.getClass())) {
                listenersOfRequestedType.add(type.cast(listener));
            }
        }
        return listenersOfRequestedType;
    }

	private synchronized void addListener(Object listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        for (Object l : listeners) {
            if (listener.equals(l)) {
                return;
            }
        }
        listeners.add(listener);
    }

	@SuppressWarnings("unchecked")
	private synchronized <T> T removeListener(T listener) {
		if (listeners == null) {
			return null;
		}
		Iterator<Object> it = listeners.iterator();
		while (it.hasNext()) {
			Object l = it.next();
			if (listener.equals(l)) {
				it.remove();
				return (T) l;
			}
		}
		return null;
	}

	public synchronized Collection<ResourceListenerRegistration> getResourceListeners() {
		return listeners == null ? Collections.<ResourceListenerRegistration> emptyList()
				: getListeners(ResourceListenerRegistration.class);
	}

	public void updateStructureListenerRegistrations() {
		man.updateStructureListenerRegistrations();
	}

	public synchronized StructureListenerRegistration addStructureListener(Resource resource,
			ResourceStructureListener listener, ApplicationManager appman) {
		StructureListenerRegistration slr = new StructureListenerRegistration(resource, listener, appman);
		addListener(slr);
		return slr;
	}

	public synchronized void addResourceListener(ResourceListenerRegistration l) {
		addListener(l);
	}

	public StructureListenerRegistration removeStructureListener(Resource resource, ResourceStructureListener listener,
			ApplicationManager appman) {
		StructureListenerRegistration search = new StructureListenerRegistration(resource, listener, appman);
		return removeListener(search);
	}

	public void fireResourceActiveStateChanged(TreeElement el, boolean state) {
		while (el.isReference()) {
			el = el.getReference();
		}
		for (StructureListenerRegistration reg : getListeners(StructureListenerRegistration.class)) {
			if (!el.getPath().equals(reg.resource.getLocation()) && !el.getPath().equals(reg.resource.getPath())) {
				removeStructureListener(reg.resource, reg.listener, reg.appman);
			}
			else {
				reg.queueActiveStateChangedEvent(state);
			}
		}
	}

	public void fireSubResourceAdded(TreeElement subresource) {
		for (StructureListenerRegistration reg : getListeners(StructureListenerRegistration.class)) {
			reg.queueSubResourceAddedEvent(subresource);
		}
	}

	public void fireSubResourceRemoved(TreeElement subresource) {
		for (StructureListenerRegistration reg : getListeners(StructureListenerRegistration.class)) {
			reg.queueSubResourceRemovedEvent(subresource);
		}
	}

	public void fireResourceDeleted(Resource r) {
		for (StructureListenerRegistration reg : getListeners(StructureListenerRegistration.class)) {
			if (!r.equalsPath(reg.getResource()) && r.isReference(false)) {
				reg.queueReferenceChangedEvent(((ConnectedResource) r.getParent()).getTreeElement(), false);
			}
			else {
				reg.queueResourceDeletedEvent();
			}
		}
	}

	public void fireResourceCreated(String path) {
		for (StructureListenerRegistration reg : getListeners(StructureListenerRegistration.class)) {
			reg.queueResourceCreatedEvent(path);
		}
	}

	public void fireReferenceRemoved(Resource referer, Resource target) {
		for (StructureListenerRegistration reg : getListeners(StructureListenerRegistration.class)) {
			if (reg.getResource().equals(referer)) {
				reg.queueEvent(new DefaultResourceStructureEvent(ResourceStructureEvent.EventType.REFERENCE_REMOVED,
						target, referer));
			}
		}
	}

	/*
	 * Removes a matching ResourceListenerRegistration and returns the
	 * originally registered object, or null if no such registration is found.
	 */
	public synchronized ResourceListenerRegistration removeResourceListener(ResourceListenerRegistration reg) {
		return removeListener(reg);
	}

	public void fireResourceChanged(final ConnectedResource r, long time, boolean valueChanged) {
		if (!r.isActive()) {
			return;
		}
		DefaultRecordedData d = man.getExistingRecordedData(r.getTreeElement());
		if (d != null) {
			d.update(time);
		}
		for (ResourceListenerRegistration reg : getListeners(ResourceListenerRegistration.class)) {
			if (reg.isAbandoned()) {
				removeListener(reg);
			}
			else {
				reg.queueResourceChangedEvent(r, valueChanged);
			}
		}
	}

	/**
	 * call this after adding sub resources or references to register existing
	 * listeners on all newly reachable elements
	 */
	public void updateListenerRegistrations() {
		for (ResourceListenerRegistration reg : getListeners(ResourceListenerRegistration.class)) {
			if (reg.isAbandoned()) {
				removeListener(reg);
			}
			else {
				reg.performRegistration();
			}
		}
	}

	/**
	 * call this when a TreeElement is no longer reachable via a formerly valild
	 * path.
	 *
	 * @param oldPath remove all resource listeners that where registered for
	 * this path but are no longer valid for this TreeElement.
	 * @return list of removed listener registrations.
	 */
	public synchronized List<ResourceListenerRegistration> invalidateListenerRegistrations(String oldPath) {
        List<ResourceListenerRegistration> invalidRegs = new ArrayList<>();
        for (ResourceListenerRegistration reg : getListeners(ResourceListenerRegistration.class)) {
            if (reg.isAbandoned()) {
                removeListener(reg);
                continue;
            }
            if (oldPath.startsWith("/")){
                oldPath = oldPath.substring(1);
            }
            if (oldPath.startsWith(reg.getResource().getPath())) {
                removeListener(reg);
                invalidRegs.add(reg);
            }
        }
        return invalidRegs;
    }

	public ScheduleTreeElement getSchedule() {
		return scheduleTreeElement;
	}

	public void setSchedule(TreeElement element) {
		if (scheduleTreeElement == null) {
			scheduleTreeElement = new ScheduleTreeElement(element);
		}
	}

	public synchronized void addReference(TreeElement el) {
        if (references == null) {
            references = new HashSet<>(5);
        }
        references.add(el);
        for (StructureListenerRegistration reg : getStructureListeners()) {
            reg.queueReferenceChangedEvent(el, true);
        }
    }

	public synchronized void removeReference(TreeElement el) {
		if (references == null) {
			//FIXME
			LoggerFactory.getLogger(getClass()).warn("suspicious removeReference call on {}", el);
			return;
		}
		references.remove(el);
		for (StructureListenerRegistration reg : getStructureListeners()) {
			reg.queueReferenceChangedEvent(el, false);
		}
	}

	public synchronized Collection<TreeElement> getReferences() {
		if (references == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(references.toArray(new TreeElement[references.size()]));
	}

	synchronized AccessModeRequest addAccessModeRequest(Resource res, ApplicationManager app, AccessMode mode, AccessPriority priority) {
        if (accessRequests == null) {
            accessRequests = new PriorityQueue<>(INITIAL_ACCESSREQUESTS_QUEUE_SIZE);
        }
        AccessModeRequest newReq = new AccessModeRequest(res, this, app, mode, priority);

        //remove previous request by the same app
        for (Iterator<AccessModeRequest> it = accessRequests.iterator(); it.hasNext();) {
            AccessModeRequest r = it.next();
            if (r.getApplicationManager() == app) {
                it.remove();
            }
        }

        if (mode != AccessMode.READ_ONLY) {
            accessRequests.add(newReq);
        } else { //READ_ONLY requests are not stored (treated as 'no request').
            newReq.setAvailableMode(READ_ONLY);
            if (accessRequests.isEmpty()) {
                return newReq;
            }
        }
        AccessMode topMode = accessRequests.peek().getRequiredAccessMode();
        AccessMode availableMode = topMode == AccessMode.EXCLUSIVE
                ? READ_ONLY : AccessMode.SHARED;
        for (AccessModeRequest r : accessRequests) {
            if (r == accessRequests.peek()) {
                r.setAvailableMode(r.getRequiredAccessMode());
            } else {
                r.setAvailableMode(availableMode);
            }
        }

        return newReq;
    }

	public synchronized List<RegisteredAccessModeRequest> getAccessRequests(ApplicationManager app) {
        if (accessRequests == null) {
            return Collections.emptyList();
        }
        List<RegisteredAccessModeRequest> rval = new ArrayList<>(accessRequests.size());
        for (AccessModeRequest r : accessRequests) {
            if (r.getApplicationManager() == app) {
                rval.add(r);
            }
        }
        return rval;
    }

	public AccessMode getAccessMode(ApplicationManager app) {
		if (accessRequests == null || accessRequests.isEmpty()) {
			return SHARED;
		}
		AccessModeRequest top = accessRequests.peek();
		if (top.getRequiredAccessMode() == EXCLUSIVE) {
			if (top.getApplicationManager() == app) {
				return EXCLUSIVE;
			}
			else {
				return READ_ONLY;
			}
		}
		else {
			return SHARED;
		}
	}

	public AccessPriority getAccessPriority(ApplicationManager app) {
		if (accessRequests == null) {
			return AccessPriority.PRIO_LOWEST;
		}
		for (AccessModeRequest r : accessRequests) {
			if (r.getApplicationManager() == app) {
				return r.getPriority();
			}
		}
		return AccessPriority.PRIO_LOWEST;
	}

	public synchronized void addAccessModeListener(AccessModeListener l, Resource res, ApplicationManager app) {
		addListener(new AccessModeListenerRegistration(app, res, l));
	}

	public boolean removeAccessModeListener(AccessModeListener l, Resource res, ApplicationManager app) {
		return removeListener(new AccessModeListenerRegistration(app, res, l)) != null;
	}

	public synchronized void fireAccessModeChanged(ApplicationManager app, final Resource r,
			final boolean requestedModeAvailable) {
		for (AccessModeListenerRegistration reg : getListeners(AccessModeListenerRegistration.class)) {
			final AccessModeListener l = reg.listener.get();
			if (l == null) {
				removeListener(reg);
			}
			else {
				if (reg.app == app && reg.res.equals(r)) {
					app.submitEvent(createAccessModeChangedCallback(l, r));
				}
			}
		}
	}

	private Callable<Void> createAccessModeChangedCallback(final AccessModeListener l, final Resource r) {
		return new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				l.accessModeChanged(r);
				return null;
			}
		};
	}

	public void transferListeners(TreeElement target) {
		ElementInfo targetElementInfo = (ElementInfo) target.getResRef();
		for (ResourceListenerRegistration reg : getResourceListeners()) {
			if (!reg.isRecursive()) {
				targetElementInfo.addListener(reg);
			}
		}
		for (StructureListenerRegistration slr : getListeners(StructureListenerRegistration.class)) {
			targetElementInfo.addStructureListener(slr.getResource(), slr.getListener(), slr.appman);
		}
		targetElementInfo.addAccessModeListeners(this);
	}

	private synchronized void addAccessModeListeners(ElementInfo other) {
        if (listeners == null){
            listeners = new ArrayList<>();
        }
        Collection<AccessModeListenerRegistration> otherListeners = other.getListeners(AccessModeListenerRegistration.class);
        for (AccessModeListenerRegistration amlr: otherListeners){
            if (!listeners.contains(amlr)){
                listeners.add(amlr);
            }
        }
    }
}
