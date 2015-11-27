package org.ogema.tools.resource.visitor;

import org.ogema.core.model.Resource;

/**
 * Implement this interface in order to perform tasks that need to
 * traverse the resource graph. A depth-first-search on the graph is
 * started using {@link ResourceProxy#depthFirstSearch(ResourceVisitor)}.
 */
public interface ResourceVisitor {

	public void visit(Resource resource);

}
