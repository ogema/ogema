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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.ogema.resourcemanager.impl.timeseries.DefaultRecordedData;
import org.ogema.resourcetree.TreeElement;
import org.ogema.resourcetree.listeners.InternalValueChangedListenerRegistration;
import org.slf4j.LoggerFactory;

/**
 * Info elemnt set on {@link TreeElement}, adding info on resource demands,
 * existing locks and listener registrations. TreeElement and ElementInfo have a
 * n:1 relationship since a TreeElement shares its ElementInfo with its
 * referencing nodes.
 * 
 * (This class is not thread safe - read / write operations are synchronized
 * in ResourceBase) - obsolete: synchronisation is done internally
 *
 * @author jlapp
 */
public class ElementInfo {

	final ResourceDBManager man;

	private final Object referencesLock = new Object();
	// TreeElements which have a reference to this element.
	private List<TreeElement> references;

	private final Object listenersLock = new Object();
	private Collection<Object> listeners;

	static final int INITIAL_ACCESSREQUESTS_QUEUE_SIZE = 5;
	private final Object accessLock = new Object();
	PriorityQueue<AccessModeRequest> accessRequests;

	public ElementInfo(ResourceDBManager man, TreeElement el) {
        //System.out.printf("NEW ELEMENTINFO %s: %s (%s)%n", this, el.getPath(), el.getLocation());
		Objects.requireNonNull(man);
		Objects.requireNonNull(el);
		this.man = man;
	}

	private <T> Collection<T> getListeners(Class<T> type) {
		synchronized (listenersLock) {
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
    }

	private void addListener(Object listener) {
		synchronized (listenersLock) {
	        if (listeners == null) {
	            listeners = new ArrayList<>(3);
	        }
	        for (Object l : listeners) {
	            if (listener.equals(l)) {
	                return;
	            }
	        }
	        listeners.add(listener);
		}
    }

	@SuppressWarnings("unchecked")
	private <T> T removeListener(T listener) {
		synchronized (listenersLock) {
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
		}
		return null;
	}

	public Collection<InternalValueChangedListenerRegistration> getResourceListeners() {
		return getListeners(InternalValueChangedListenerRegistration.class);
	}

	public void addResourceListener(InternalValueChangedListenerRegistration l) {
		addListener(l);
	}

	/*
	 * Removes a matching ResourceListenerRegistration and returns the
	 * originally registered object, or null if no such registration is found.
	 */
	public InternalValueChangedListenerRegistration removeResourceListener(InternalValueChangedListenerRegistration reg) {
		final InternalValueChangedListenerRegistration old = removeListener(reg);
		if (old != null)
			old.dispose();
		return old;
	}
	
	public void fireResourceChanged(final ConnectedResource r, long time, boolean valueChanged) {
		if (!r.isActive()) {
			return;
		}
		DefaultRecordedData d = man.getExistingRecordedData(r.getTreeElement());
		if (d != null) {
			d.update(time);
		}
		for (InternalValueChangedListenerRegistration reg : getListeners(InternalValueChangedListenerRegistration.class)) {
			if (reg instanceof ResourceListenerRegistration && ((ResourceListenerRegistration) reg).isAbandoned()) {
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
		for (InternalValueChangedListenerRegistration reg : getListeners(InternalValueChangedListenerRegistration.class)) {
			if (reg instanceof ResourceListenerRegistration)
				if (((ResourceListenerRegistration) reg).isAbandoned()) {
					removeListener(reg);
				}
				else {
					((ResourceListenerRegistration) reg).performRegistration();
			}
			else {
				// TODO check: ok?
				this.addResourceListener(reg);
//				reg.performRegistration();
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
	public List<InternalValueChangedListenerRegistration> invalidateListenerRegistrations(String oldPath) {
        List<InternalValueChangedListenerRegistration> invalidRegs = new ArrayList<>();
        for (InternalValueChangedListenerRegistration reg : getListeners(InternalValueChangedListenerRegistration.class)) {
            if (reg instanceof ResourceListenerRegistration && ((ResourceListenerRegistration) reg).isAbandoned()) {
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

    /** 
     * adds a reference poining to this ElementInfo's tree element. the reference
     * is the actual link element, not the 'reference parent' of this element.
     * @param reference the reference element
     */
	public void addReference(TreeElement reference) {
        //System.out.printf("ADD REFERENCE %s: %s%n", this, reference);
        if (!reference.isReference()) {
            throw new IllegalArgumentException("not a reference: " + reference);
        }
        synchronized (referencesLock) {
	        if (references == null) {
	            references = new ArrayList<>(3);
	        }
	        /* XXX references must be deleted in the correct order:
	          in case of a reference to a reference insert that element before the
	          element it references... 
	        */
	        boolean inserted = false;
	        for (int i = 0; i < references.size(); i++) {
	            TreeElement listEl = references.get(i);
	            if (listEl == reference) {
	                inserted = true;
	                break;
	            }
	            if (reference.isReference() && reference.getReference() == listEl) {
	                references.add(i, reference);
	                inserted = true;
	                break;
	            }
	        }
	        if (!inserted) {
	            references.add(reference);
	        }
        }
    }

	public void removeReference(TreeElement referenceElement, TreeElement target) {
        //System.out.printf("REMOVE REFERENCE %s: %s%n", this, referenceElement);
        boolean removed = false;
        synchronized (referencesLock) {
	        if (references != null) {
	            removed = references.remove(referenceElement);
	        }
        }
		if (!removed) {
			//FIXME
			LoggerFactory.getLogger(getClass()).warn("suspicious removeReference call on {}", referenceElement);
		}
        //System.out.printf("REFERENCES %s: %s%n", this, references);
	}

    public Collection<TreeElement> getReferences(String path, boolean transitive) {
    	synchronized (referencesLock) {
			if (references == null) {
				return Collections.emptyList();
			}
	        Collection<TreeElement> rval = new ArrayList<>(references.size());
	        for (TreeElement e: references) {
	            if (transitive) {
	            	TreeElement p = e;
		            while (p.isReference()) {
	                    if (p.getReference().getPath().equals(path)) {
		                    rval.add(e);
		                    break;
		                }
		                p = p.getReference();
		            }
	            }
	            else {
	            	if (e.isReference() && e.getReference().getPath().equals(path)) {
	                    rval.add(e);
	                }
	            }
	        }
	        return rval;
    	}
	}

	AccessModeRequest addAccessModeRequest(Resource res, ApplicationManager app, AccessMode mode, AccessPriority priority) {
		synchronized (accessLock) {
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
    }

	public List<RegisteredAccessModeRequest> getAccessRequests(ApplicationManager app) {
		synchronized (accessLock) {
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
    }

	public AccessMode getAccessMode(ApplicationManager app) {
		AccessModeRequest top;
		synchronized (accessLock) {
			if (accessRequests == null || accessRequests.isEmpty()) {
				return SHARED;
			}
			top = accessRequests.peek();
		}
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
		synchronized (accessLock) {
			if (accessRequests == null) {
				return AccessPriority.PRIO_LOWEST;
			}
			for (AccessModeRequest r : accessRequests) {
				if (r.getApplicationManager() == app) {
					return r.getPriority();
				}
			}
		}
		return AccessPriority.PRIO_LOWEST;
	}

	public void addAccessModeListener(AccessModeListener l, Resource res, ApplicationManager app) {
		addListener(new AccessModeListenerRegistration(app, res, l));
	}

	public boolean removeAccessModeListener(AccessModeListener l, Resource res, ApplicationManager app) {
		final AccessModeListenerRegistration reg = removeListener(new AccessModeListenerRegistration(app, res, l));
		if (reg != null)
			reg.dispose();
		return reg != null;
	}

	public void fireAccessModeChanged(ApplicationManager app, final Resource r,	final boolean requestedModeAvailable) {
		for (AccessModeListenerRegistration reg : getListeners(AccessModeListenerRegistration.class)) {
			final AccessModeListener l = reg.listener.get();
			if (l == null) {
				removeListener(reg);
			}
			else {
				if (reg.app == app && reg.res.equals(r)) {
					app.submitEvent(createAccessModeChangedCallback(reg, l, r));
				}
			}
		}
	}

	private Callable<Void> createAccessModeChangedCallback(final AccessModeListenerRegistration reg, final AccessModeListener l, final Resource r) {
		return new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (reg.isActive())
					l.accessModeChanged(r);
				return null;
			}
		};
	}

	public void transferListeners(TreeElement target) {
		ElementInfo targetElementInfo = man.getElementInfo(target);
		for (InternalValueChangedListenerRegistration reg : getResourceListeners()) {
			if (!(reg instanceof ResourceListenerRegistration) || !((ResourceListenerRegistration) reg).isRecursive()) {
				targetElementInfo.addListener(reg);
			}
		}
		targetElementInfo.addAccessModeListeners(this);
	}

	private void addAccessModeListeners(ElementInfo other) {
		synchronized (listenersLock) {
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
}
