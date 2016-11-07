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
package org.ogema.resourcemanager.impl.transaction.actions;

import java.util.Objects;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceOperationException.Type;
import org.ogema.resourcemanager.impl.transaction.ReadAction;

/**
 * Write action for access mode 
 *
 */
// TODO in rollback, restore access mode and priority for other apps!
public class AccessModeAction implements ReadAction<Boolean> {
	
	private final Resource target;
	private final AccessMode mode;
	private final AccessPriority prio;
	private AccessMode oldMode;
	private AccessPriority oldPrio;
	private final boolean failOnReject;
	private boolean success;
	private boolean done = false;
	private boolean setBack = false;
	
	public AccessModeAction(Resource resource, AccessMode mode, AccessPriority priority,boolean failOnReject) {
		Objects.requireNonNull(resource);
		if (mode == null)
			mode = AccessMode.READ_ONLY;
		if (priority == null)
			priority = AccessPriority.PRIO_LOWEST;
		this.target = resource;
		this.mode = mode;
		this.prio = priority;
		this.failOnReject = failOnReject;
	}

	@Override
	public void execute() throws Exception {
		if (done || setBack)
			throw new IllegalStateException("Transaction has been executed already");
		done = true; // we must set this immediately, so if an exception occurs, we can still rollback the action
		oldMode = target.getAccessMode();
		oldPrio = target.getAccessPriority();
		success = target.requestAccessMode(mode, prio);
		if (failOnReject && !success)
			throw new ResourceException("Failed to acquire requested access mode for resource " + target);
	}

	@Override
	public void rollback() throws IllegalStateException {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet, cannot set back");
		if (setBack)
			throw new IllegalStateException("Transaction has been rolled back already");
		setBack =true;
		if (success && (oldMode != mode || oldPrio != prio))
			target.requestAccessMode(oldMode, oldPrio);
	}

	@Override
	public boolean requiresStructureWriteLock() {
		return true; // XXX ?
	}

	@Override
	public boolean requiresCommitWriteLock() {
		return true; // XXX ?
	}

	@Override
	public Type getType() {
		return Type.ACCESS_MODE;
	}

	@Override
	public Resource getSource() {
		return target;
	}

	@Override
	public Boolean getValue() {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet");
		if (setBack)
			throw new IllegalStateException("Transaction failed");
		return success;
	}

	
	
}
