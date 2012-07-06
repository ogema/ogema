/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
