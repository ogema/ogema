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
import org.ogema.resourcemanager.impl.transaction.ReadAction;

public abstract class GenericReadAction<T, V extends Resource> implements ReadAction<T> {
	
	private final V resource;
	private T value = null;
	private boolean done = false;
	private boolean setBack = false;
	 
	public GenericReadAction(V resource) {
		this.resource =resource;
	}
	
	protected abstract T read(V resource);
	

	@Override
	public void execute() throws Exception {
		if (done || setBack)
			throw new IllegalStateException("Transaction has been executed already");
		done = true;
		value = read(resource);
	}

	@Override
	public void rollback() throws IllegalStateException {
		setBack = true;
	}

	@Override
	public boolean requiresStructureWriteLock() {
		return false;
	}

	@Override
	public boolean requiresCommitWriteLock() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.READ; 
	}

	@Override
	public V getSource() {
		return resource;
	}

	@Override
	public T getValue() {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet");
		if (setBack)
			throw new IllegalStateException("Transaction failed");
		return value;
	}


}