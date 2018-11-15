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
