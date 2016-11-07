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
	 * @param tvl
	 * @param register
	 * 		true: register value listener; false: unregister value listener
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
