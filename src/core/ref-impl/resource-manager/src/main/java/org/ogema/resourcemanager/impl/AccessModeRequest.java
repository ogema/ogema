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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredAccessModeRequest;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import static org.ogema.core.resourcemanager.AccessMode.EXCLUSIVE;
import org.ogema.core.resourcemanager.AccessPriority;

/**
 *
 * @author jlapp
 */
public class AccessModeRequest implements Comparable<AccessModeRequest>, RegisteredAccessModeRequest {

	static final AtomicLong instanceCounter = new AtomicLong(0);

	private final Resource res;
	private final ElementInfo elInfo;
	private final ApplicationManager app;
	private final AccessMode requestedMode;
	private final AccessPriority priority;
	private final long instanceNumber;

	private AccessMode grantedMode = AccessMode.READ_ONLY;

	public AccessModeRequest(Resource res, ElementInfo elInfo, ApplicationManager app, AccessMode requestedMode,
			AccessPriority priority) {
		this.res = res;
		this.elInfo = elInfo;
		this.app = app;
		this.requestedMode = requestedMode;
		this.priority = priority;
		//grantedMode = requestedMode;
		instanceNumber = instanceCounter.incrementAndGet();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + Objects.hashCode(this.res);
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
		final AccessModeRequest other = (AccessModeRequest) obj;
		if (!Objects.equals(this.res, other.res)) {
			return false;
		}
		return Objects.equals(this.app, other.app);
	}

	/* highest priority is smallest */
	@Override
	public int compareTo(AccessModeRequest o) {
		int cmpPrio = priority.compareTo(o.priority);
		if (cmpPrio == 0) {
			if (requestedMode == EXCLUSIVE) {
				if (o.requestedMode == EXCLUSIVE) {
					return Long.compare(instanceNumber, o.instanceNumber);
				}
				else {
					return -1;
				}
			}
			if (o.requestedMode == EXCLUSIVE) {
				return 1;
			}
		}
		return cmpPrio;
	}

	public void setAvailableMode(AccessMode mode) {
		if (mode == requestedMode && grantedMode != mode) {
			fireResourceAvailable(true);
		}
		else if (mode != requestedMode && grantedMode == requestedMode) {
			fireResourceAvailable(false);
		}
		grantedMode = mode;
	}

	public Resource getResource() {
		return res;
	}

	private void fireResourceAvailable(final boolean available) {
		elInfo.fireAccessModeChanged(app, res, available);
	}

	@Override
	public AccessMode getRequiredAccessMode() {
		return requestedMode;
	}

	@Override
	public boolean isFulfilled() {
		return requestedMode == grantedMode;
	}

	public ApplicationManager getApplicationManager() {
		return app;
	}

	@Override
	public AccessPriority getPriority() {
		return priority;
	}

	@Override
	public AdminApplication getApplication() {
		return app.getAdministrationManager().getAppById(app.getAppID().getIDString());
	}

}
