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
package org.ogema.apps.graphgenerator.styles.gviz;

import org.ogema.apps.graphgenerator.styles.EdgeStyle;

/**
 * Base class for edge styles. Other styles may inherit from this and replace
 * their defaults in the constructor.
 * @author Timo Fischer, Fraunhofer IWES
 */
public class EdgeStyleBase implements EdgeStyle {

	protected String color, referenceStyle, defaultStyle;

	public EdgeStyleBase() {
		color = "black";
		referenceStyle = "dashed";
		defaultStyle = "solid";
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public String getReferenceStyle() {
		return referenceStyle;
	}

	@Override
	public void setReferenceStyle(String style) {
		referenceStyle = style;
	}

	@Override
	public String getDefaultStyle() {
		return defaultStyle;
	}

	@Override
	public void setDefaultStyle(String style) {
		defaultStyle = style;
	}

}
