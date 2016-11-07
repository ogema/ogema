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
