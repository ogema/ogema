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
