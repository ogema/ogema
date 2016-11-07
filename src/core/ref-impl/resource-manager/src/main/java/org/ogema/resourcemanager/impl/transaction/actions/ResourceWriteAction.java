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
import java.util.Objects;
import java.util.Queue;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.ResourceOperationException.Type;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.core.resourcemanager.transaction.WriteConfiguration;
import org.ogema.resourcemanager.impl.transaction.AtomicAction;
import org.ogema.tools.resource.util.ValueResourceUtils;

/**
 * Write a value to a resource.
 * Write actions are composite actions in general (i.e. they have subactions),
 * since they may include creation and activation of the respective resource
 * (depending on the WriteConfiguration). 
 *
 * @param <T>
 * @param <V>
 */
public class ResourceWriteAction<T, V extends ValueResource> implements AtomicAction {
	
	private final V resource;
	// initial value is null, but null may also be returned, if the resource is virtual 
	// and the read operataion is configured to ignore this
	private final T value;
	private T oldValue = null; // for rollback
	private boolean doWrite = true;
	private final WriteConfiguration config;
	private boolean done = false;
	private boolean setBack = false;
	private final Queue<AtomicAction> subactions = new ArrayDeque<>();  // creation, activation
	private final Deque<AtomicAction> subactionsDone = new ArrayDeque<>();
	
	public ResourceWriteAction(V resource, T value, WriteConfiguration config) {
		Objects.requireNonNull(resource);
		Objects.requireNonNull(value);
		Objects.requireNonNull(config);
		this.resource = resource;
		this.config = config;
		this.value = value;
	}
	
	@Override
	public boolean requiresCommitWriteLock() {
		return true;
	}
	
	@Override
	public boolean requiresStructureWriteLock() {
		return true; // XXX ?
	}
	
	@SuppressWarnings("unchecked")
	protected T read(V resource) {
		return (T) ValueResourceUtils.getValue(resource);
	}
	
	protected void write(V resource, T value) {
		ValueResourceUtils.setValue(resource, value);
	}
	
	/**
	 * For most actions this is simply the same as writing, but there 
	 * are exception, such as some schedules write operations.
	 * @param resource
	 * @param value
	 */
	protected void undo(V resource, T value) {
		write(resource,value);
	}
	
	// only one subaction possible, which may itself be composite, however
    @SuppressWarnings("fallthrough")
	private void buildActionsTree() {
		boolean activate = false;
		boolean create = false;
		switch (config) {
		case CREATE_AND_ACTIVATE:
			create = true;
		case ACTIVATE:
			activate = true;
			break;
		default:
		}
		if (activate)
			subactions.add(new ActivationAction(resource, true, false, create));
	}
	
	private void executeSubActions() throws Exception {
		AtomicAction action;
		while ((action = subactions.poll()) != null) {
			subactionsDone.add(action); // add this immediately, so we'll try to rollback this action even if it fails
			action.execute();
		}
	}
	
	private void rollbackSubactions() throws IllegalStateException {
		AtomicAction action;
		while ((action = subactionsDone.pollLast()) != null) {
			try {
				action.rollback();
			} catch (Exception e) {
				continue;
			}
		}
	}
	
	@Override
	public void execute() throws Exception {
		if (done || setBack)
			throw new IllegalStateException("Transaction has been executed already");
		done = true; // we must set this immediately, so if an exception occurs, we can still rollback the action
		buildActionsTree(); // we cannot do this earlier, e.g. in the constructor, since at that time no resource lock is held
		executeSubActions();
		if (config == WriteConfiguration.FAIL && !resource.isActive())
			throw new VirtualResourceException("Resource " + resource + " found virtual");
		if (!resource.exists()) {
			if (config == WriteConfiguration.IGNORE || config == WriteConfiguration.ACTIVATE)
				doWrite = false;
		}
		oldValue = read(resource);
		if (doWrite)
			write(resource, value);
	}

	// FIXME we need to prevent listener callbacks in this case!
	@Override
	public void rollback() throws IllegalStateException {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet, cannot set back");
		if (setBack)
			throw new IllegalStateException("Transaction has been rolled back already");
		setBack =true;
		if (doWrite && oldValue != null && !value.equals(oldValue) && resource.exists())
			undo(resource, oldValue);
		rollbackSubactions();
	}
	
	@Override
	public Type getType() {
		return Type.WRITE;
	}
	
	@Override
	public Resource getSource() {
		return resource;
	}
	
}
