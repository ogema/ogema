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
package org.ogema.resourcetree.listeners;

import java.util.Objects;

import org.ogema.core.administration.RegisteredStructureListener;
import org.ogema.core.application.ApplicationManager;
import org.ogema.resourcetree.TreeElement;

public abstract class InternalStructureListenerRegistration implements RegisteredStructureListener {
	
	private volatile boolean active = true;
	
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
		if (!(obj instanceof InternalStructureListenerRegistration))
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
	
	public boolean isActive() {
		return active;
	}
	
	public void dispose() {
		this.active = false;
	}

}
