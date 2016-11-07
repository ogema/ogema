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

import java.util.HashSet;
import java.util.Set;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.listener.util.TransitiveValueListener;
import org.ogema.tools.resource.visitor.ResourceProxy;

// TODO handling of virtual resources
public class TransitiveValueListenerImpl<T extends Resource> implements TransitiveValueListener<T> {

	final Resource topNode;
	final Class<T> resourceType;
	final ResourceValueListener<T> listener;
	final boolean callOnEveryUpdate;
	final ValueHelperStructureListener<T> structureListener; 
	final Set<String> valueListenerPaths = new HashSet<>(); 
	
	public TransitiveValueListenerImpl(Resource topNode, ResourceValueListener<T> listener, Class<T> resourceType, boolean callOnEveryUpdate) {
		this.topNode = topNode; 
		this.resourceType = resourceType;
		this.listener = listener;
		this.callOnEveryUpdate= callOnEveryUpdate;
		this.structureListener = new ValueHelperStructureListener<T>(this);
		topNode.addStructureListener(structureListener);
		initValueListeners(topNode);
	}
	
	void initValueListeners(Resource resource) {
		ValueListenerRegistration<T> visitor = new ValueListenerRegistration<T>(this, true);
		ResourceProxy proxy = new ResourceProxy(resource);
		proxy.depthFirstSearch(visitor, true);
	}
		
	// FIXME this does not remove listener registrations from virtual resources 
	void removeValueListeners(Resource resource) {
		ValueListenerRegistration<T> visitor = new ValueListenerRegistration<T>(this, false);
		ResourceProxy proxy = new ResourceProxy(resource);
		// FIXME this is problematic... we cannot follow references, because it would potentially remove 
		// value listeners that are still required, on the other hand, we need to delete those that are no longer
		// required
		proxy.depthFirstSearch(visitor, false); 
	}


	// FIXME this does not remove listener registrations from virtual resources
	@Override
	public void destroy() {
		topNode.removeStructureListener(structureListener);
		ValueListenerRegistration<T> visitor = new ValueListenerRegistration<T>(this, false);
		ResourceProxy proxy = new ResourceProxy(topNode);
		proxy.depthFirstSearch(visitor, true);
	}

	@Override
	public ResourceValueListener<T> getValueListener() {
		return listener;
	}
	
}
