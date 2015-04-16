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
package org.ogema.apps.graphgenerator.generators;

import java.util.Collection;
import org.ogema.apps.graphgenerator.styles.NodeStyle;
import org.ogema.apps.graphgenerator.styles.EdgeStyle;
import org.ogema.core.model.Resource;

/**
 * Interface for a class that supports writing the desired part of the resource
 * graph into a string. Different GraphGenerators may support different graph
 * languages.
 */
public interface GraphGenerator {

	/**
	 * Initialize collecting nodes and edges. Should be called before the first
	 * node or edge is being added.
	 */
	void init();

	/**
	 * Adds a new node to the graph.
	 * @param node Resource that is to be represented as a node.
	 * @param style Display style for this node.
	 */
	void addNode(Resource node, NodeStyle style);

	/**
	 * Adds a new, directed edge to be graph.
	 * @param start Start node of the edge.
	 * @param end End node of the edge.
	 * @param style draw style used to display the edge.
	 */
	void addEdge(Resource start, Resource end, EdgeStyle style);

	/**
	 * Creates a group of resources.
	 * @param resources Set of resources to add to the group.
	 * @param name Name of the group.
	 */
	void addGroup(Collection<Resource> resources, String name);

	/**
	 * Finalize the graph. Should be called after the last or edge has been added.
	 */
	void finish();

	/**
	 * Gets the constructed graph. Should be called only after {@link #finish()}
	 * has been invoked.
	 * @return The graph as a string in the graph language this generator
	 * represents or is configured to.
	 */
	String getGraph();
}
