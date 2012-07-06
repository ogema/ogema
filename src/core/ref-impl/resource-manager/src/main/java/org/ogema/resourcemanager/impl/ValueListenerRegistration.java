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

import java.util.concurrent.Callable;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.application.ApplicationManager;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Represents a listener registration generated by a call to
 * {@link Resource#addResourceListener(org.ogema.core.resourcemanager.ResourceListener)}. The same
 * ResourceListenerRegistration object will be used in the {@link ElementInfo} of all affected Resources, ie. all sub
 * Resources of the Resource on which the listener was registered.
 * 
 * @author jlapp
 */
public class ValueListenerRegistration implements ResourceListenerRegistration {

	protected final ResourceBase origin;
	protected final ResourceValueListener listener;
	protected final boolean callOnEveryUpdate;

	public ValueListenerRegistration(ResourceBase origin, ResourceValueListener listener, boolean callOnEveryUpdate) {
		this.origin = origin;
		this.listener = listener;
		this.callOnEveryUpdate = callOnEveryUpdate;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ValueListenerRegistration)) {
			return false;
		}
		ValueListenerRegistration other = (ValueListenerRegistration) obj;
		return other.listener == listener && other.origin.equals(origin);
	}

	@Override
	public int hashCode() {
		return origin.hashCode();
	}

	@Override
	public void queueResourceChangedEvent(final Resource r, boolean valueChanged) {
		if (!callOnEveryUpdate && !valueChanged) {
			return;
		}
		Callable<Void> listenerCall = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				listener.resourceChanged(r);
				return null;
			}
		};
		origin.resMan.getApplicationManager().submitEvent(listenerCall);
	}

	@Override
	public void performRegistration() {
		final ResourceDBManager manager = origin.resMan.getDatabaseManager();
		ElementInfo info = manager.getElementInfo(origin.getEl());
		info.addResourceListener(this);
	}

	@Override
	public void unregister() {
		final ResourceDBManager manager = origin.resMan.getDatabaseManager();
		ElementInfo info = manager.getElementInfo(origin.getEl());
		info.removeResourceListener(this);
	}

	@Override
	public Resource getResource() {
		return origin;
	}

	@Override
	public AdminApplication getApplication() {
		ApplicationManager appman = origin.resMan.getApplicationManager();
		return appman.getAdministrationManager().getAppById(appman.getAppID().getIDString());
	}

	public ResourceValueListener getValueListener() {
		return listener;
	}

	public boolean isCallOnEveryUpdate() {
		return callOnEveryUpdate;
	}

	@Override
	public ResourceListener getListener() {
		return null;
	}

	@Override
	public boolean isAbandoned() {
		return false;
	}

	@Override
	public boolean isRecursive() {
		return false;
	}

}
