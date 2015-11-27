package org.ogema.tools.resource.visitor;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.model.Resource;

/**
 * A utility class allowing to traverse the resource graph using
 * a {@link ResourceProxy}. 
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
		depthFirstSearch(visitor, followReferences, new ArrayList<String>());
	}

	private void depthFirstSearch(ResourceVisitor visitor, boolean followReferences, List<String> visitedLocations) {
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
