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

import java.util.List;

import org.ogema.apps.graphgenerator.generators.visjs.Edge;
import org.ogema.apps.graphgenerator.generators.visjs.Node;
import org.ogema.apps.graphgenerator.generators.visjs.Options;

class VisJsData {
	private List<Node> nodes;
	private List<Edge> edges;
	private Options options;

	public VisJsData(List<Node> nodes, List<Edge> edges, Options options) {
		this.nodes = nodes;
		this.edges = edges;
		this.options = options;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}
}
