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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ogema.apps.graphgenerator.generators.visjs.Color;
import org.ogema.apps.graphgenerator.generators.visjs.Edge;
import org.ogema.apps.graphgenerator.generators.visjs.Options;
import org.ogema.apps.graphgenerator.generators.visjs.TopLevelDummyNode;
import org.ogema.apps.graphgenerator.generators.visjs.Edge.VisJsEdgeStyle;
import org.ogema.apps.graphgenerator.generators.visjs.Node;
import org.ogema.apps.graphgenerator.styles.EdgeStyle;
import org.ogema.apps.graphgenerator.styles.NodeStyle;
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
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.tools.SerializationManager;

public class VisJsGenerator extends GraphGenerator {
	
	private List<Node> nodes = new ArrayList<>();
	private List<Edge> edges = new ArrayList<>();
	private SerializationManager serMan;
	private Options options = new Options();

	public VisJsGenerator(OgemaLogger logger, SerializationManager serMan) {
		super(logger);
		this.serMan = serMan;
	}

	@Override
	public void init() {
	}
	
	@Override
	public void addNode(Resource node) {
		NodeStyle style = new NodeStyleBlue();
		if (node instanceof ValueResource) {
			style.setShape("box");
		}
		
		addNode(node, style);
	}

	@Override
	public void addNode(Resource node, NodeStyle style) {
		final String resLocation = node.getLocation(PATH_SEP);
		final String resType = node.getResourceType().getSimpleName();

		// add the node label
		final List<String> labels = new ArrayList<>();
		if (style.addType()) {
			labels.add(node.getResourceType().getSimpleName());
		}
		if (style.addName()) {
			labels.add(node.getName());
		}
		if (style.addLocation()) {
			labels.add(node.getLocation());
		}
		if (style.addSimpleValues()) {
			if (node instanceof BooleanResource || node instanceof FloatResource || node instanceof IntegerResource || node instanceof IntegerResource || node instanceof StringResource || node instanceof TimeResource) {
				final String value = getSimpleValue((ValueResource) node);
				labels.add("value = " + value);
			}
		}
		Node newNode = new Node(new Color(), resLocation, "", style.getShape());
		for (String sublabel : labels) {
			newNode.setLabel(newNode.getLabel() + "\n" + sublabel);
		}
		
		nodes.add(newNode);
//		if (node.isActive()) {
//			m_graph.append(" color=").append(style.getActiveColor());
//		} else {
//			m_graph.append(" color=").append(style.getInactiveColor());
//		}
//		m_graph.append(" ]\n");
	}

	@Override
	public void addEdge(Resource start, Resource end, EdgeStyle style) {
		if(style == null) {
			style = new EdgeStyleBase();
		}

		final String loc1 = start.getLocation(PATH_SEP);
		final String loc2 = end.getLocation(PATH_SEP);
		
		Edge edge = new Edge(loc1, loc2, VisJsEdgeStyle.ARROW);
		
		if (end.isReference(false)) {
			edge.setStyle(VisJsEdgeStyle.DASH_LINE.getStyleIdentifier());
		}
		
		edges.add(edge);
	}

	@Override
	public void addGroup(Collection<Resource> resources, String name) {
		// ignoring groups for now ...
	}
	
	@Override
	public synchronized Object generateAllResourcesGraph(ResourceAccess resAcc) {
		addTopLevelDummy(resAcc);
		return super.generateAllResourcesGraph(resAcc);
	}

	@Override
	public synchronized Object generateConnectionsGraph(ResourceAccess resAcc) {
		// TODO ...
//		addTopLevelDummy(resAcc);
		return super.generateConnectionsGraph(resAcc);
	}

	@Override
	public Object finish() {
		return getGraph();
	}

	@Override
	public String getGraph() {
		// TODO add config options to html and set from servlet ...
		
		return serMan.toJson(new VisJsData(nodes, edges, options));
	}

	private void addTopLevelDummy(ResourceAccess resAcc) {
		TopLevelDummyNode topLevelDummy = new TopLevelDummyNode();
		addNode(topLevelDummy);
		List<Resource> toplevelResources = resAcc.getToplevelResources(Resource.class);
		for(Resource topLevelResource : toplevelResources) {
			addEdge(topLevelDummy, topLevelResource);
		}
	}
	
	public void setPhysConfigurationEnabled(boolean enabled) {
		options.setConfigurePhysics(enabled);
	}
}
