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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.ogema.core.model.units.PhysicalUnitResource;

/**
 * The GraphwizGenerator as an object that is subsequently filled.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class GraphwizGenerator extends GraphGenerator {

	//    public static final String FLAG_SCHEDULE = " shape=octagon ";

	private final StringBuilder m_graph = new StringBuilder();

	private final NodeStyle defaultNodeStyle = new NodeStyleBlue();
	private final NodeStyle thermalNodeStyle = new NodeStyleBlue();

	public GraphwizGenerator(OgemaLogger logger) {
		super(logger);
		thermalNodeStyle.setActiveColor("red");
	}

	private final String getHeader() {
		return "digraph G {\n" + "node [style=filled, fontcolor=\"white\"]\n" + "edge [penwidth=2]\n\n";
	}

	@Override
	public String toString() {
		return m_graph.toString() + "\n}\n";
	}

	@Override
	public void init() {
		m_graph.append(getHeader());
	}

	// TODO implement using the style instead of the hard-coded styling.
	@Override
	public void addNode(Resource node) {
		NodeStyle style = new NodeStyleBlue();
		if (node instanceof ValueResource) {
			style.setShape("rectangle");
		}

		addNode(node, style);
	}

	@Override
	public void addNode(Resource node, NodeStyle style) {
		final String resLocation = node.getLocation(PATH_SEP);
		final String resType = node.getResourceType().getSimpleName();
		m_graph.append(resLocation).append(" [ ");

		// add the node label
		final List<String> labels = new ArrayList<>();
		if (style.addType()) {
			labels.add(resType);
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
		StringBuilder label = new StringBuilder();
		label.append("label=\"");
		for (String sublabel : labels) {
			label.append(sublabel).append("\\n");
		}
		label.append("\"");
		m_graph.append(label);
		m_graph.append(" shape=").append(style.getShape());
		if (node.isActive()) {
			m_graph.append(" color=").append(style.getActiveColor());
		} else {
			m_graph.append(" color=").append(style.getInactiveColor());
		}
		m_graph.append(" ]\n");
	}

	@Override
	public void addEdge(Resource start, Resource end, EdgeStyle style) {
		if (style == null) {
			style = new EdgeStyleBase();
		}

		final String loc1 = start.getLocation(PATH_SEP);
		final String loc2 = end.getLocation(PATH_SEP);
		m_graph.append(loc1).append(" -> ").append(loc2);
		m_graph.append(" [ color=").append(style.getColor());
		if (end.isReference(false)) {
			m_graph.append(" style=").append(style.getReferenceStyle()).append(" ]");
		}
		else {
			m_graph.append(" style=").append(style.getDefaultStyle()).append(" ]");
		}
		m_graph.append("\n");
	}

	@Override
	public Object finish() {
		m_graph.append("\n}\n");
		
		PrintWriter writer;
		try {
			File file = new File("resources.graph");
			writer = new PrintWriter(file, "UTF-8");
			writer.println(getGraph());
			writer.close();
			return file;
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			logger.error("Could not write to file.");
		}
		
		return null;
	}

	@Override
	public String getGraph() {
		return m_graph.toString();
	}

	@Override
	public void addGroup(Collection<Resource> resources, String name) {
		m_graph.append("subgraph cluster_").append(name).append(" { style=filled color=gray90 ");
		m_graph.append("label=").append(name).append(" \n");
		for (Resource resource : resources) {
			addNode(resource);
		}
		m_graph.append("\n}\n");
	}
}
