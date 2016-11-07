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
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceOperationException.Type;
import org.ogema.resourcemanager.impl.transaction.AtomicAction;
import org.ogema.tools.resource.util.ValueResourceUtils;

// TODO restore access mode and priorities for other apps in rollback
public class DeletionAction implements AtomicAction {
	
	private final ResourceManagement rm;
	private final boolean isTopLevel;
	private final Resource target;
	private final String path; // only set if isTopLevel == true; for rollback
	private final Class<? extends Resource> type; // only set if isTopLevel == true; for rollback
	private boolean existed = false; // for rollback
	private Resource oldReferenceTarget = null; // for rollback
	private List<Resource> oldReferences = null; // for rollback
	private AccessMode oldMode = null; // for rollback -> TODO restore the access mode for all apps
	private AccessPriority oldPriority = null; // for rollback -> TODO restore the access priority for all apps
	private boolean wasActive = false;
	private Object oldValue = null;
	private boolean done;
	private boolean setBack;
	private final Queue<AtomicAction> subactions = new ArrayDeque<>();
	private final Deque<AtomicAction> subactionsDone = new ArrayDeque<>();
	
	public DeletionAction(Resource target, ResourceManagement rm) {
		Objects.requireNonNull(target);
		this.target = target;
		this.rm = rm;
		this.isTopLevel = target.isTopLevel();
		if (isTopLevel) {
			path = target.getPath();
			type = target.getResourceType();
		}
		else {
			path = null;
			type = null;
		}
	}
	
	private void buildActionsTree() {
		// subresources of references must no be deleted
		
		if (!target.isReference(false)) { 
			List<Resource> subresources = target.getSubResources(false);
			// subresources
			for (Resource sub: subresources) {
				subactions.add(new DeletionAction(sub,null));
			}
		}
		List<Resource> refs = target.getReferencingNodes(false); 
		for (Resource ref :refs) {
			subactions.add(new DeletionAction(ref, null));
		}
		
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
		done = true;
		buildActionsTree(); // we cannot do this earlier, e.g. in the constructor, since at that time no resource lock is held
		executeSubActions();
		existed = target.exists();
		if (!existed)
			return;
		wasActive = target.isActive();
		if (target.isReference(false)) 
			oldReferenceTarget = target.getLocationResource();
		else if (target instanceof ValueResource)
			oldValue = ValueResourceUtils.getValue((ValueResource) target);
		oldReferences = target.getReferencingResources(Resource.class);
		oldMode = target.getAccessMode();
		oldPriority = target.getAccessPriority();
		target.delete();
	}

	@Override
	public void rollback() throws IllegalStateException {
		if (!done)
			throw new IllegalStateException("Transaction has not been executed yet, cannot set back");
		if (setBack)
			throw new IllegalStateException("Transaction has been rolled back already");
		setBack =true;
//		if (!existed) {
//			target.delete();
//			return;
//		}
		if (!existed) 
			return;
		// it may be that the reference target was deleted in the same transaction, and has not been recreated yet
		// in this case, the reference will be restored when the referenceTarget deletion action is rolled back (see below)
		if (oldReferenceTarget != null && oldReferenceTarget.exists()) 
				target.setAsReference(oldReferenceTarget);
		else {
			if (isTopLevel)
				rm.createResource(path, type);
			else
				target.create();
			try { // we probably still want to execute the sub-rollbacks 
				if (wasActive)
					target.activate(false);
				if (oldValue != null && target instanceof ValueResource) {
					ValueResourceUtils.setValue((ValueResource) target, oldValue);
				}
			} catch (Exception e) {
				// TODO
			}
		}
		target.requestAccessMode(oldMode, oldPriority);
		if (oldReferences != null) {
			for (Resource res: oldReferences) {
				try {
					if (res.getParent().exists()) 
						res.setAsReference(target);
				} catch (Exception e) {
					// ignore:
					// it may be that the referencing resource has been deleted in the same transaction, and its parent has not been recreated yet
					// in this case, the reference will be restored when the respective delete action for res is rolled back (see above)
				}
			}
			
		}
		rollbackSubactions();
	}

	@Override
	public boolean requiresStructureWriteLock() {
		return true;
	}

	@Override
	public boolean requiresCommitWriteLock() {
		return false; // XXX?
	}

	@Override
	public Type getType() {
		return Type.DELETE;
	}

	@Override
	public Resource getSource() {
		return target;
	}
	
	

}
