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

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceOperationException.Type;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.resourcemanager.impl.transaction.AtomicAction;

public class ReferenceAction<R extends Resource> implements AtomicAction {
	
	private final ResourceManagement rm;
	private final R resource;
	private final R target;
	private boolean changed = false;
	private boolean done = false;
	private boolean setBack = false;
	private DeletionAction deletion = null; // is created in execute() if the target exists at the time of execution; must be rolled back as well
	
	public ReferenceAction(R resource, R target, ResourceManagement rm) {
		this.resource = resource;
		this.target = target;
		this.rm = rm;
	}

	@Override
	public void execute() throws Exception {
		if (done || setBack)
			throw new IllegalStateException("Transaction has been executed already");
		done = true; // we must set this immediately, so if an exception occurs we can still rollback the action
		if (!target.exists())
			throw new VirtualResourceException("Reference target does not exist");
		if (resource.exists() && resource.equalsLocation(target))
			return;
		changed = true;
		if (resource.exists()) {
			deletion = new DeletionAction(resource,rm);
			deletion.execute();
		}
		resource.setAsReference(target);
	}

	@Override
	public void rollback() throws IllegalStateException {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet, cannot set back");
		if (setBack)
			throw new IllegalStateException("Transaction has been rolled back already");
		setBack =true;
		if (!changed)
			return;
		resource.delete();
		if (deletion != null)
			deletion.rollback();
	}

	@Override
	public boolean requiresStructureWriteLock() {
		return true;
	}

	@Override
	public boolean requiresCommitWriteLock() {
		return true; // XXX ?
	}

	@Override
	public Type getType() {
		return Type.REFERENCE;
	}

	@Override
	public Resource getSource() {
		return resource;
	}
}
