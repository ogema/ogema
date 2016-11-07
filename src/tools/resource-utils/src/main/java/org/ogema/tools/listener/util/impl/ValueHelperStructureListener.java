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

public class ValueHelperStructureListener<T extends Resource> implements ResourceStructureListener {

	private final TransitiveValueListenerImpl<T> transitiveListener;
	
	public ValueHelperStructureListener(TransitiveValueListenerImpl<T> transitiveListener) {
		this.transitiveListener = transitiveListener;
	}
	
	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		// FIXME
		System.out.println("   xxx structure event " + event.getType().name() + ", changed: " + event.getChangedResource());
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
