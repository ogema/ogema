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
import org.ogema.tools.listener.util.RegisteredStructureValueListener;
import org.ogema.tools.listener.util.StructureValueListener;

public class RegisteredStructureValueListenerImpl<T extends Resource> implements RegisteredStructureValueListener<T>, ResourceStructureListener {

	private final StructureValueListener<T> listener;
	private final boolean reportAllChanges;
	private final Resource resource;
	
	/**
	 * 
	 * @param listener
	 * @param valueType
	 * @param reportAllChanges
	 * 		If this is false, only the following {@see EventType}s are reported to the structure listener:
	 * 		<ul>
	 * 			<li>RESOURCE_ACTIVATED
	 * 			<li>RESOURCE_DEACTIVATED
	 * 			<li>RESOURCE_DELETED
	 * 		</ul>
	 * 		This should be thought of as an enhanced value listener.
	 */
	public RegisteredStructureValueListenerImpl(Resource resource, StructureValueListener<T> listener, boolean reportAllChanges) {
		this.listener = listener;
		this.reportAllChanges = reportAllChanges;
		this.resource = resource;
		if (reportAllChanges)
			resource.addStructureListener(listener);
		else
			resource.addStructureListener(this);
		resource.addValueListener(listener);
	}
	
	@Override
	public void destroy() {
		if (reportAllChanges)
			resource.removeStructureListener(listener);
		else
			resource.removeStructureListener(this);
		resource.removeValueListener(listener);
	}

	@Override
	public StructureValueListener<T> getListener() {
		return listener;
	}

	@Override
	public void resourceStructureChanged(ResourceStructureEvent event) {
		switch(event.getType()) {
		case RESOURCE_ACTIVATED:
		case RESOURCE_DEACTIVATED:
		case RESOURCE_DELETED:
			listener.resourceStructureChanged(event);
			break;
		default:
			return;
		}
		
	}
	
	

}
