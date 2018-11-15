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
import java.util.Iterator;
import java.util.concurrent.Callable;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessModeListener;

/*
 * Manages AccessModeListener registrations for an ElementInfo.
 * @author jlapp
 */
public class AccessModeListenerList {

	private Collection<AccessModeListenerRegistration> listeners;
    
    private synchronized Collection<AccessModeListenerRegistration> getListeners() {
        return listeners;
    }

	synchronized void addAll(AccessModeListenerList other) {
        Collection<AccessModeListenerRegistration> otherListeners;
        if (other == null || (otherListeners = other.getListeners()) == null) {
            return;
        }
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.addAll(otherListeners);
    }

	synchronized void addListener(AccessModeListener l, Resource res, ApplicationManager app) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(new AccessModeListenerRegistration(app, res, l));
    }

	synchronized boolean removeListener(AccessModeListener listener, ApplicationManager app) {
		if (listeners != null) {
			for (Iterator<AccessModeListenerRegistration> it = listeners.iterator(); it.hasNext();) {
				AccessModeListenerRegistration registration = it.next();
				AccessModeListener l = registration.listener.get();
				if (l == null) {
					it.remove();
				}
				else {
					if (l == listener && registration.app == app) {
						it.remove();
						return true;
					}
				}
			}
		}
		return false;
	}

	synchronized void fireAccessModeChanged(ApplicationManager app, final Resource r,
			final boolean requestedModeAvailable) {
		if (listeners != null) {
			for (Iterator<AccessModeListenerRegistration> it = listeners.iterator(); it.hasNext();) {
				AccessModeListenerRegistration registration = it.next();
				final AccessModeListener l = registration.listener.get();
				if (l == null) {
					it.remove();
				}
				else {
					if (registration.app == app) {
						app.submitEvent(createAccessModeChangedCallback(l, r));
					}
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

}
