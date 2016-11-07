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
package org.ogema.tools.listener.util.impl;

import org.ogema.core.model.Resource;
import org.ogema.tools.resource.visitor.ResourceVisitor;

public class ValueListenerRegistration<T extends Resource> implements ResourceVisitor {

	private final TransitiveValueListenerImpl<T> tvl;
	private final boolean register;
	
	/**
	 * 
	 * @param tvl
	 * @param register
	 * 		true: register value listener; false: unregister value listener
	 */
	public ValueListenerRegistration(TransitiveValueListenerImpl<T> tvl, boolean register) {
		this.tvl =tvl;
		this.register = register;
	}

	@Override
	public void visit(Resource resource) {
		if (register) {
			if (tvl.valueListenerPaths.contains(resource.getLocation())) 
				return;
			tvl.valueListenerPaths.add(resource.getLocation());
			resource.addStructureListener(tvl.structureListener);
			if (tvl.resourceType.isAssignableFrom(resource.getResourceType())) {
				resource.addValueListener(tvl.listener, tvl.callOnEveryUpdate);
			}
		}
		else {
			if (!tvl.valueListenerPaths.remove(resource.getLocation())) 
				return;
			resource.removeStructureListener(tvl.structureListener);
			if (tvl.resourceType.isAssignableFrom(resource.getResourceType())) {
				resource.removeValueListener(tvl.listener);
			}
		}
	}
	
}
