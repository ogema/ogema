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

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.tools.resource.visitor.ResourceVisitor;

public class StructureListenerRegistration implements ResourceVisitor {

	private final TransitiveStructureListenerImpl tsl;
	private final boolean register;
	/**
	 * false, if this is for registering or removing the listener only 
	 */
	private final boolean callback;
	
	/**
	 * 
	 * @param tsl
	 * @param register
	 * 		true: register value listener; false: unregister value listener
	 * @param callback
	 */
	public StructureListenerRegistration(TransitiveStructureListenerImpl tsl, boolean register, boolean callback) {
		this.tsl =tsl;
		this.register = register;
		this.callback = callback;
		
	}

	@Override
	public void visit(Resource resource) {
		if (register) {
			if (tsl.structureListeners.contains(resource.getLocation())) 
				return;
			tsl.structureListeners.add(resource.getLocation());
			resource.addStructureListener(tsl.helperListener);
			if (callback) {
				ResourceStructureEvent clone = new StructureEvent(EventType.SUBRESOURCE_ADDED, tsl.topNode, resource);
				tsl.listener.resourceStructureChanged(clone);
			}
		}
		else {
			if (!tsl.structureListeners.remove(resource.getLocation())) 
				return;
			resource.removeStructureListener(tsl.helperListener);
			if (callback) {
				ResourceStructureEvent clone = new StructureEvent(EventType.SUBRESOURCE_REMOVED, tsl.topNode, resource);
				tsl.listener.resourceStructureChanged(clone);
			}
		}
	}
	
}
