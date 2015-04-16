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
					return (int) Math.signum(instanceNumber - o.instanceNumber);
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
