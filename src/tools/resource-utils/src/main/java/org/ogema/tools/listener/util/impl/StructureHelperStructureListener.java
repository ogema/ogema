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

public class StructureHelperStructureListener implements ResourceStructureListener {

	private final TransitiveStructureListenerImpl transitiveListener;
	
	public StructureHelperStructureListener(TransitiveStructureListenerImpl transitiveListener) {
		this.transitiveListener = transitiveListener;
	}
	
	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		Resource changed = event.getChangedResource();
		switch (event.getType()) {
		case SUBRESOURCE_ADDED:
			transitiveListener.initStructureListener(changed); 
			return; // event will be re-created by visitor
		case SUBRESOURCE_REMOVED: 
			transitiveListener.removeStructureListener(changed);
			return;
		default:
			 // RESOURCE_CREATED, RESOURCE_DELETE, REFERENCE_ADDED, etc., are only passed to the structure listener if they derive from the top-level node
			if (!event.getSource().equals(transitiveListener.topNode))
				return;
		}
		// simply pass this through, with a new source
		ResourceStructureEvent clone = new StructureEvent(event.getType(), transitiveListener.topNode, event.getChangedResource());
		// submit a task instead?
		try {
			transitiveListener.listener.resourceStructureChanged(clone);
		} catch (Throwable e) {
			transitiveListener.am.getLogger().error("Error in structure listener callback",e);
		}
	}

}
