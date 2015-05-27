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
package org.ogema.apps.graphgenerator.generators.visjs;

public class Edge {
	private String from;
	private String to;
	private String style = VisJsEdgeStyle.ARROW.getStyleIdentifier();

	public Edge() {
	}

	public Edge(String from, String to, VisJsEdgeStyle style) {
		this.from = from;
		this.to = to;
		this.style = style.getStyleIdentifier();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public enum VisJsEdgeStyle {
		// TODO update if vis.js 4.0 is released ... dash-line with arrow should be available then for references
		ARROW("arrow"), ARROW_CENTER("arrow-center"), DASH_LINE("dash-line");

		private String name;

		private VisJsEdgeStyle(String name) {
			this.name = name;
		}

		public String getStyleIdentifier() {
			return name;
		}
	}
}
