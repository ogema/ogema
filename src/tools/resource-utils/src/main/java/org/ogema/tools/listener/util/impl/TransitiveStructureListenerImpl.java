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

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.tools.listener.util.TransitiveStructureListener;
import org.ogema.tools.resource.visitor.ResourceProxy;

public class TransitiveStructureListenerImpl implements TransitiveStructureListener {

	final Resource topNode;
	final ResourceStructureListener listener;
	final StructureHelperStructureListener helperListener;
	// locations
	final Set<String> structureListeners = new HashSet<>();
	final ApplicationManager am;
	
	public TransitiveStructureListenerImpl(Resource topNode, ResourceStructureListener listener, ApplicationManager am) {
		this.topNode = topNode; 
		this.listener = listener;
		this.am = am;
		this.helperListener = new StructureHelperStructureListener(this);
		initStructureListener(topNode, false);
	}
	
	void initStructureListener(final Resource resource) {
		initStructureListener(resource, true);
	}
	
	void initStructureListener(final Resource resource, boolean callback) {
		StructureListenerRegistration visitor = new StructureListenerRegistration(this, true, callback);
		ResourceProxy proxy = new ResourceProxy(resource);
		proxy.depthFirstSearch(visitor, true);
	}
	
	
	void removeStructureListener(final Resource resource) {
		removeStructureListener(resource, true);
	}
	
	void removeStructureListener(final Resource resource, boolean callback) {
		StructureListenerRegistration visitor = new StructureListenerRegistration(this, false, callback);
		ResourceProxy proxy = new ResourceProxy(resource);
		// FIXME this is problematic... we cannot follow references, because it would potentially remove 
		// value listeners that are still required, on the other hand, we need to delete those that are no longer
		// required
		proxy.depthFirstSearch(visitor, false); 
	}

	@Override
	public void destroy() {
		removeStructureListener(topNode, false);
	}

	@Override
	public ResourceStructureListener getStructureListener() {
		return listener;
	}

}
