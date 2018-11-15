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
package org.ogema.tools.listener.util;

import org.ogema.core.resourcemanager.ResourceStructureListener;

/**
 * Represents a {@link ResourceStructureListener} that is registered for all suitable subresources
 * of a given resource. More precisely, besides the usual resourceStructureChanged callbacks, it 
 * receives in addition callbacks of type EventType#SUBRESOURCE_ADDED and 
 * EventType#SUBRESOURCE_REMOVED if a subresource somewhere below in the resource tree 
 * is added or removed.  
 */
public interface TransitiveStructureListener {

	/**
	 * unregister all value listener registrations
	 */
	public void destroy();
	
	/**
	 * Get the structure listener that is registered for the resourceStructureChanged callbacks 
	 * @return
	 */
	public ResourceStructureListener getStructureListener();
	
}
