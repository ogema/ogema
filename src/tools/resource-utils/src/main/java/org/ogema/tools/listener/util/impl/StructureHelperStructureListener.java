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
