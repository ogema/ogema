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
import org.ogema.apps.graphgenerator.styles.NodeStyle;
import org.ogema.apps.graphgenerator.styles.EdgeStyle;
import java.util.List;
import org.ogema.apps.graphgenerator.styles.NodeStyleBlue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.SimpleResource;
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
public class GraphwizGenerator implements GraphGenerator {

	public static final String PATH_SEP = "__";
	//    public static final String FLAG_SCHEDULE = " shape=octagon ";

	final StringBuilder m_graph = new StringBuilder();

	public GraphwizGenerator() {
	}

	private final String getHeader() {
		return "digraph G {\n" + "node [style=filled, fontcolor=\"white\"]\n" + "edge [penwidth=2]\n\n";
	}

	private String getSimpleValue(SimpleResource resource) {
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
    public void addNode(Resource node, NodeStyle style) {
        final String resLocation = node.getLocation(PATH_SEP);
        final String resType = node.getResourceType().getSimpleName();
        m_graph.append(resLocation).append(" [ ");

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
                final String value = getSimpleValue((SimpleResource) node);
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
	public void finish() {
		m_graph.append("\n}\n");
	}

	@Override
	public String getGraph() {
		return m_graph.toString();
	}

	@Override
	public void addGroup(Collection<Resource> resources, String name) {
		final NodeStyle defaultNodeStyle = new NodeStyleBlue();
		m_graph.append("subgraph cluster_").append(name).append(" { style=filled color=gray90 ");
		m_graph.append("label=").append(name).append(" \n");
		for (Resource resource : resources) {
			addNode(resource, defaultNodeStyle);
		}
		m_graph.append("\n}\n");
	}
}
