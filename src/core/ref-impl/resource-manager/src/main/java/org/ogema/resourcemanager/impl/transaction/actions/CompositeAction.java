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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceOperationException.Type;
import org.ogema.resourcemanager.impl.transaction.AtomicAction;

/**
 * An action that is composed of multiple sub-actions.
 * Note: it may seem strange that a CompositeAction is an AtomicAction 
 * - the reason is, that "atomic" here refers to the user point of view, 
 * not the internal operation. 
 */
public class CompositeAction implements AtomicAction {
	
	private final Queue<AtomicAction> pending;
	private final Deque<AtomicAction> done = new ArrayDeque<>();
	private boolean isDone = false;
	private boolean setBack = false;
	
	
	public CompositeAction(List<AtomicAction> actions) {
		this.pending = new ArrayDeque<>(actions);
	}

	// TODO like in transaction itself
	@Override
	public void execute() throws Exception {
		if (isDone || setBack)
			throw new IllegalStateException("Transaction has been executed already");
		isDone = true;
		AtomicAction action;
		while ((action = pending.poll()) != null) {
			done.add(action); // add this immediately, so we'll try to rollback this action even if it fails
			action.execute();
		}
	}

	@Override
	public void rollback() throws IllegalStateException {
		if (!isDone)
			throw new IllegalStateException("Transaction has not been executed yet, cannot set back");
		if (setBack)
			throw new IllegalStateException("Transaction has been rolled back already");
		AtomicAction action;
		while ((action = done.pollLast()) != null) {
			try {
				action.rollback();
			} catch (Exception ee) { // may not be so unusual
//				appMan.getLogger().warn("Transaction rollback failed",ee);
				continue;
			}
		}
		pending.clear();
	}

	@Override
	public boolean requiresStructureWriteLock() {
		Iterator<AtomicAction> it = pending.iterator();
		while (it.hasNext()) {
			AtomicAction act = it.next();
			if (act.requiresStructureWriteLock())
				return true;
		}
		return false;
	}

	@Override
	public boolean requiresCommitWriteLock() {
		Iterator<AtomicAction> it = pending.iterator();
		while (it.hasNext()) {
			AtomicAction act = it.next();
			if (act.requiresCommitWriteLock())
				return true;
		}
		return false;
	}

	// FIXME
	@Override
	public Type getType() {
		if (!pending.isEmpty()) 
			return pending.peek().getType();
		else if (!done.isEmpty())
			return done.peek().getType();
		else
			return null;
	}

	// FIXME
	@Override
	public Resource getSource() {
		if (!pending.isEmpty()) 
			return pending.peek().getSource();
		else if (!done.isEmpty())
			return done.peek().getSource();
		else
			return null;
	}

}
