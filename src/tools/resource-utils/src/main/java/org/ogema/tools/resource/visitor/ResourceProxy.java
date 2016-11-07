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
