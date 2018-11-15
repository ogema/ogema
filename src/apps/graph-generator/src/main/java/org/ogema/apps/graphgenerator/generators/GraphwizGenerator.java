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
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;

/**
 * The GraphwizGenerator as an object that is subsequently filled.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class GraphwizGenerator extends GraphGenerator {

	//    public static final String FLAG_SCHEDULE = " shape=octagon ";

	private final StringBuilder m_graph = new StringBuilder();
	private final ApplicationManager am;

	private final NodeStyle defaultNodeStyle = new NodeStyleBlue();
	private final NodeStyle thermalNodeStyle = new NodeStyleBlue();

	public GraphwizGenerator(ApplicationManager am) {
		super(am.getLogger());
		this.am = am;
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
			File file = am.getDataFile("resources.graph");
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
