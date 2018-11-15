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
package org.ogema.tools.resource.visitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ogema.core.model.Resource;

/**
 * A utility class allowing to traverse the resource graph using
 * a {@link ResourceVisitor}. 
 */
public class ResourceProxy {

	private final Resource resource;
	
	public ResourceProxy(Resource resource) {
		this.resource = resource;
	}

	public final Resource getResource() {
		return resource;
	}

	// if it returns false, the subresources of this resource will not be visited
	public boolean accept(ResourceVisitor visitor) {
		visitor.visit(resource);
		return true;
	}

	public void depthFirstSearch(ResourceVisitor visitor, boolean followReferences) {
		depthFirstSearch(visitor, followReferences, new HashSet<String>());
	}

	public void depthFirstSearch(ResourceVisitor visitor, boolean followReferences, Set<String> visitedLocations) {
		String location = resource.getLocation();
		if (!followReferences && resource.isReference(false))
			return;
		if (visitedLocations.contains(location))
			return;
		visitedLocations.add(location);
		if (!accept(visitor)) 
			return;
		List<Resource> subResources = resource.getSubResources(false);
		for (Resource subRes : subResources) {
			ResourceProxy proxy = new ResourceProxy(subRes);
			proxy.depthFirstSearch(visitor, followReferences, visitedLocations);
		}
	}

}
