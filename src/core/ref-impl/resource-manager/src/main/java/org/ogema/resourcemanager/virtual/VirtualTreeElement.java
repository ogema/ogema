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
package org.ogema.resourcemanager.virtual;

import org.ogema.core.model.Resource;
import org.ogema.resourcetree.TreeElement;

/**
 *
 * @author jlapp
 */
public interface VirtualTreeElement extends TreeElement {

	boolean isVirtual();

	boolean create();

	/**
	 * Return child with given name and type, allows navigation across vritual
	 * decorators.
	 * 
	 * @param name
	 * @param type
	 * @return child element
	 */
	VirtualTreeElement getChild(String name, Class<? extends Resource> type);

	@Override
	VirtualTreeElement getChild(String name);
    
    /**
     * Returns only non-virtual subresources, does not create virtual child elements.
     * @param name
     * @return a non-virtual child element or null
     */
    VirtualTreeElement getExistingChild(String name);

	void delete();
    
    /**
     * Replace the type of this TreeElement with a sub type of the current type.
     * Only works on a virtual element.
     * @throws IllegalStateException if thus element already exists and has a different type.
     */
    void constrainType(Class<? extends Resource> type);
    
}
