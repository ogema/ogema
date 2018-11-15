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
package org.ogema.apps.graphgenerator.styles;

/**
 * Configuration object that determines how resources are written. Determines
 * color, shape and text details.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface EdgeStyle {

	/**
	 * Gets the name of the color.
	 */
	String getColor();

	/**
	 * Sets the color to a new name.
	 */
	void setColor(String color);

	/**
	 * Gets the style that arrows are drawn for references.
	 */
	String getReferenceStyle();

	/**
	 * Sets the plotstyle for references
	 */
	void setReferenceStyle(String style);

	/**
	 * Gets the default arrow style for non-reference subresources.
	 */
	String getDefaultStyle();

	/**
	 * Sets the plot style for non-reference sub-resources.
	 */
	void setDefaultStyle(String style);
}
