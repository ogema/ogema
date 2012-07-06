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

import java.lang.ref.WeakReference;
import java.util.Objects;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessModeListener;

/**
 *
 * @author jlapp
 */
class AccessModeListenerRegistration {
	final ApplicationManager app;
	final WeakReference<AccessModeListener> listener;
	final Resource res;

	public AccessModeListenerRegistration(ApplicationManager app, Resource res, AccessModeListener listener) {
        this.app = app;
        this.listener = new WeakReference<>(listener);
        this.res = res;
    }

	public boolean notifyFor(ApplicationManager app, Resource res) {
		return this.app == app && this.res.equalsPath(res);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof AccessModeListenerRegistration)) {
			return false;
		}
		AccessModeListenerRegistration other = (AccessModeListenerRegistration) obj;
		return other.app == app && other.res.equals(res) && other.listener.get() == listener.get();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.app);
		hash = 89 * hash + Objects.hashCode(this.listener);
		hash = 89 * hash + Objects.hashCode(this.res);
		return hash;
	}

}
