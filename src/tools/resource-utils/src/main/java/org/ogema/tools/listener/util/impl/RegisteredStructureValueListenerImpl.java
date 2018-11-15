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
import org.ogema.tools.listener.util.RegisteredStructureValueListener;
import org.ogema.tools.listener.util.StructureValueListener;

public class RegisteredStructureValueListenerImpl<T extends Resource> implements RegisteredStructureValueListener<T>, ResourceStructureListener {

	private final StructureValueListener<T> listener;
	private final boolean reportAllChanges;
	private final Resource resource;
	
	/**
	 * @param resource
	 * @param listener
	 * @param reportAllChanges
	 * 		If this is false, only the following EventTypes are reported to the structure listener:
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
