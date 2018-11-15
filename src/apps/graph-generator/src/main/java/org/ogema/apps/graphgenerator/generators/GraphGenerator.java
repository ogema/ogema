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
package org.ogema.apps.graphgenerator.generators;

import java.util.ArrayList;
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
// FIXME synchronization in a servlet should be avoided; + remove finish method
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

	public Object generateAllResourcesGraph(ResourceAccess resAcc) {
		return generateResourcesGraph(Resource.class, resAcc);
	}
	
	public synchronized Object generateResourcesGraph(Class<? extends Resource> topLevelType, ResourceAccess resAcc) {
		init();

		// Write the groups
		logger.debug("Writing the groups and the resources for type " + topLevelType.getName());
		final List<? extends Resource> topLevelResources = resAcc.getToplevelResources(topLevelType);
		for (Resource root : topLevelResources) {
			final List<Resource> groupElements = root.getDirectSubResources(true);
			groupElements.add(root);
			if (groupElements.size() > 2) {
				addGroup(groupElements, root.getLocation("_"));  // does  nothing in case of visJS
			}
		}
		//        graph.writeDeviceGroups(resAcc.getToplevelResources(Resource.class), "");

		// write all resources, including their style
		final List<? extends Resource> resources = topLevelResources;
		List<String> nodeLocations = new ArrayList<>();
		for (Resource resource : resources) {
			addNode(resource);
			nodeLocations.add(resource.getLocation());
		}

		// write the edges
		logger.debug("Writing edges.");
		for (Resource resource : resources) {
			addSubresources(resource, nodeLocations);
		}
		return finish();
	}
	
	private void addSubresources(Resource parent, List<String> nodeLocations) {
		final List<Resource> subresources = parent.getSubResources(false);
		for (Resource subres : subresources) {
			addEdge(parent, subres);
			String loc = subres.getLocation();
			if (!nodeLocations.contains(loc)) {
				addNode(subres);
				nodeLocations.add(loc);
				addSubresources(subres, nodeLocations);
			}
		}
		
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
