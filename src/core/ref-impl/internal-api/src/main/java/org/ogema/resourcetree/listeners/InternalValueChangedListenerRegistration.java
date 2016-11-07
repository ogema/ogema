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

import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.model.Resource;

public abstract class InternalValueChangedListenerRegistration implements RegisteredValueListener {
	
	public abstract void queueResourceChangedEvent(final Resource r, boolean valueChanged);

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
	
}
