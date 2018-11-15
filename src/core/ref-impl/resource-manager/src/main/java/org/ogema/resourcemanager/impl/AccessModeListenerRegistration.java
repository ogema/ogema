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
	
	private volatile boolean active = true;
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

	public boolean isActive() {
		return active;
	}
	
	public void dispose() {
		this.active = false;
	}
	
}
