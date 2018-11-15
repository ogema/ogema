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
