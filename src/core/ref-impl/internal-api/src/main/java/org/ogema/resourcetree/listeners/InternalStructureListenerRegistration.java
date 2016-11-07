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
package org.ogema.resourcetree.listeners;

import java.util.Objects;

import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.application.ApplicationManager;
import org.ogema.resourcetree.TreeElement;

public abstract class InternalStructureListenerRegistration implements RegisteredStructureListener {
	
	public abstract boolean isVirtualRegistration();
	
	public abstract void queueActiveStateChangedEvent(final boolean active);
	
	public abstract void queueResourceCreatedEvent(String path);
	
	public abstract void queueResourceDeletedEvent();
	
	public abstract void queueSubResourceAddedEvent(final TreeElement subresource);
	
	public abstract void queueSubResourceRemovedEvent(final TreeElement subresource);
	
	public abstract void queueReferenceChangedEvent(final TreeElement referencingElement, final boolean added);
	
	public abstract ApplicationManager getApplicationManager();
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(getResource().getPath());
	}

    /* implemented as final: different subclasses exist and must be treated equally by
     collections or add / remove operations */
	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !InternalStructureListenerRegistration.class.isAssignableFrom(obj.getClass()))
				return false;
		final InternalStructureListenerRegistration other = (InternalStructureListenerRegistration) obj;
		if (!Objects.equals(this.getResource(), other.getResource())) {
			return false;
		}
		if (this.getListener() != other.getListener()) {
			return false;
		}
		return Objects.equals(this.getApplicationManager(), other.getApplicationManager());
	}

}
