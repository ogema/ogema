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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import org.ogema.apps.graphgenerator.styles.NodeStyle;
import org.ogema.apps.graphgenerator.styles.EdgeStyle;
import org.ogema.apps.graphgenerator.styles.gviz.EdgeStyleBase;
import org.ogema.apps.graphgenerator.styles.gviz.NodeStyleBlue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.PhysicalUnitResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.model.connections.ThermalConnection;
import org.ogema.model.prototypes.Connection;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Interface for a class that supports writing the desired part of the resource
 * graph into a string. Different GraphGenerators may support different graph
 * languages.
 */
public abstract class GraphGenerator {

	public static final String PATH_SEP = "__";

	protected OgemaLogger logger;

	public GraphGenerator(OgemaLogger logger) {
		this.logger = logger;
	}

	/**
	 * Initialize collecting nodes and edges. Should be called before the first
	 * node or edge is being added.
	 */
	public abstract void init();

	/**
	 * Adds a new node to the graph.
	 * @param node Resource that is to be represented as a node.
	 */
	public abstract void addNode(Resource node);

	/**
	 * Adds a new node to the graph.
	 * @param node Resource that is to be represented as a node.
	 * @param style Display style for this node.
	 */
	public abstract void addNode(Resource node, NodeStyle style);

	/**
	 * Adds a new, directed edge to be graph. Same as invoking {@link #addEdge(Resource, Resource, EdgeStyle)}
	 * with style = <code>null</code>.
	 * @param start Start node of the edge.
	 * @param end End node of the edge.
	 */
	public void addEdge(Resource start, Resource end) {
		addEdge(start, end, null);
	}

	/**
	 * Adds a new, directed edge to be graph.
	 * @param start Start node of the edge.
	 * @param end End node of the edge.
	 * @param style draw style used to display the edge.
	 */
	public abstract void addEdge(Resource start, Resource end, EdgeStyle style);

	/**
	 * Creates a group of resources.
	 * @param resources Set of resources to add to the group.
	 * @param name Name of the group.
	 */
	public abstract void addGroup(Collection<Resource> resources, String name);

	/**
	 * Finalize the graph. Should be called after the last or edge has been added.
	 */
	public abstract Object finish();

	/**
	 * Gets the constructed graph. Should be called only after {@link #finish()}
	 * has been invoked.
	 * @return The graph as a string in the graph language this generator
	 * represents or is configured to.
	 */
	public abstract String getGraph();

	public synchronized Object generateAllResourcesGraph(ResourceAccess resAcc) {
		init();

		// Write the groups
		logger.debug("Writing the groups and the resources");
		final List<Resource> topLevelResources = resAcc.getToplevelResources(Resource.class);
		for (Resource root : topLevelResources) {
			final List<Resource> groupElements = root.getDirectSubResources(true);
			groupElements.add(root);
			if (groupElements.size() > 2) {
				addGroup(groupElements, root.getLocation("_"));
			}
		}
		//        graph.writeDeviceGroups(resAcc.getToplevelResources(Resource.class), "");

		// write all resources, including their style
		final List<Resource> resources = resAcc.getResources(Resource.class);
		for (Resource resource : resources) {
			addNode(resource);
		}

		// write the edges
		logger.debug("Writing edges.");
		for (Resource resource : resources) {
			final List<Resource> subresources = resource.getSubResources(false);
			for (Resource subres : subresources) {
				addEdge(resource, subres);
			}
		}
		return finish();
	}

	public synchronized <T extends Resource> Object generateGraph(ResourceAccess resAcc, Class<T> clazz) {
		init();
		final NodeStyle defaultNodeStyle = new NodeStyleBlue();
		final EdgeStyle defaultEdgeStyle = new EdgeStyleBase();

		final List<T> resources = resAcc.getResources(clazz);
		for (T res : resources) {
			addNode(res);

			final Resource directParent = res.getParent();
			final List<Resource> parents = res.getReferencingResources(Resource.class);
			if (directParent != null)
				parents.add(directParent);
			for (Resource parent : parents) {
				addNode(parent, defaultNodeStyle);
				addEdge(parent, res, defaultEdgeStyle);
			}
		}

		return finish();
	}

	public synchronized Object generateConnectionsGraph(ResourceAccess resAcc) {
		init();
		final NodeStyle defaultNodeStyle = new NodeStyleBlue();
		final NodeStyle thermalNodeStyle = new NodeStyleBlue();
		thermalNodeStyle.setActiveColor("red");
		final EdgeStyle defaultEdgeStyle = new EdgeStyleBase();

		final List<Connection> connections = resAcc.getResources(Connection.class);
		for (Connection connection : connections) {
			final NodeStyle nodeStyle = (connection instanceof ThermalConnection) ? thermalNodeStyle : defaultNodeStyle;
			addNode(connection, nodeStyle);

			final PhysicalElement in = resAcc.getResource(connection.input().getLocation());
			if (in.exists()) {
				addNode(in, defaultNodeStyle);
				addEdge(connection, in, defaultEdgeStyle);
			}

			final PhysicalElement out = resAcc.getResource(connection.output().getLocation());
			if (out.exists()) {
				addNode(out, defaultNodeStyle);
				addEdge(connection, out, defaultEdgeStyle);
			}

			final Resource directParent = connection.getParent();
			final List<Resource> parents = connection.getReferencingResources(Resource.class);
			if (directParent != null)
				parents.add(directParent);
			for (Resource parent : parents) {
				addNode(parent, defaultNodeStyle);
				addEdge(parent, connection, defaultEdgeStyle);
			}
		}

		return finish();
	}

	protected String getSimpleValue(ValueResource resource) {
		final String resType = resource.getResourceType().getSimpleName();
		if (resource instanceof PhysicalUnitResource) {
			final PhysicalUnitResource res = (PhysicalUnitResource) resource;
			return res.getValue() + " " + res.getUnit();
		}
		else if (resource instanceof FloatResource) {
			final FloatResource res = (FloatResource) resource;
			return (new Float(res.getValue())).toString();
		}
		else if (resource instanceof BooleanResource) {
			final BooleanResource res = (BooleanResource) resource;
			return (res.getValue()) ? "true" : "false";
		}
		else if (resource instanceof IntegerResource) {
			final IntegerResource res = (IntegerResource) resource;
			return (new Integer(res.getValue())).toString();
		}
		else if (resource instanceof StringResource) {
			StringResource res = (StringResource) resource;
			return res.getValue();
		}
		else if (resource instanceof TimeResource) {
			TimeResource res = (TimeResource) resource;
			return (new Long(res.getValue())).toString();
		}
		throw new RuntimeException("Unknown or unimplemented simple resource type " + resType.toString());
	}
}
