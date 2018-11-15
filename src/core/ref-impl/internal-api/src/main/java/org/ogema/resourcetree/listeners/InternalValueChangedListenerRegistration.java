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

import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.model.Resource;

public abstract class InternalValueChangedListenerRegistration implements RegisteredValueListener {
	
	public abstract void queueResourceChangedEvent(final Resource r, boolean valueChanged);
	
	private volatile boolean active = true;

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !InternalValueChangedListenerRegistration.class.isAssignableFrom(obj.getClass()))
			return false;
		
		InternalValueChangedListenerRegistration other = (InternalValueChangedListenerRegistration) obj;
		return other.getValueListener() == getValueListener() && other.getResource().equals(this.getResource()); 
	}

	@Override
	public int hashCode() {
		return getResource().hashCode();
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void dispose() {
		this.active = false;
	}
	
}
