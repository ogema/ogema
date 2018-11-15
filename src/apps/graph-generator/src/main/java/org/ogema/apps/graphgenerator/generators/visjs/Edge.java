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
