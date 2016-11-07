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