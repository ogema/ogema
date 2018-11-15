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
import org.ogema.core.resourcemanager.ResourceStructureListener;

public class ValueHelperStructureListener<T extends Resource> implements ResourceStructureListener {

	private final TransitiveValueListenerImpl<T> transitiveListener;
	
	public ValueHelperStructureListener(TransitiveValueListenerImpl<T> transitiveListener) {
		this.transitiveListener = transitiveListener;
	}
	
	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		// FIXME
		//System.out.println("   xxx structure event " + event.getType().name() + ", changed: " + event.getChangedResource());
		Resource changed = event.getChangedResource();
		switch (event.getType()) {
		case SUBRESOURCE_ADDED:
			transitiveListener.initValueListeners(changed); 
			break;
		case SUBRESOURCE_REMOVED:
			transitiveListener.removeValueListeners(changed);
			break;
		default:
		}
	}

}
