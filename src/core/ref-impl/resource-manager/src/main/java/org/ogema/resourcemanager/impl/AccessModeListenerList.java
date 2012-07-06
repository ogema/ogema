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

	synchronized void addAll(AccessModeListenerList other) {
        if (other == null || other.listeners == null) {
            return;
        }
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.addAll(other.listeners);
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
