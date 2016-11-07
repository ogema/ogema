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
