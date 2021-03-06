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
import org.ogema.core.resourcemanager.ResourceOperationException.Type;
import org.ogema.resourcemanager.impl.transaction.AtomicAction;

public class CreationAction implements AtomicAction {
	
	private final Resource resource;
	private boolean existed;
	private boolean done = false;
	private boolean setBack = false;

	
	public CreationAction(Resource target) {
		this.resource = target;
	}

	@Override
	public void execute() throws Exception {
		if (done || setBack)
			throw new IllegalStateException("Transaction has been executed already");
		done = true;
		existed = resource.exists();
		if (!existed)
			resource.create();
	}

	@Override
	public void rollback() throws IllegalStateException {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet, cannot set back");
		if (setBack)
			throw new IllegalStateException("Transaction has been rolled back already");
		setBack =true;
		if (!existed)
			resource.delete();
	}

	@Override
	public boolean requiresStructureWriteLock() {
		return true;
	}

	@Override
	public boolean requiresCommitWriteLock() {
		return true;
	}

	@Override
	public Type getType() {
		return Type.CREATE;
	}

	@Override
	public Resource getSource() {
		return resource;
	}

}
