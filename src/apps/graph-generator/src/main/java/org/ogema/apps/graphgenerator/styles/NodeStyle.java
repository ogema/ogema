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
package org.ogema.apps.graphgenerator.styles;

/**
 * Configuration object that determines how resources are written. Determines
 * color, shape and text details.
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface NodeStyle {

	/**
	 * Gets the color used for active nodes.
	 */
	String getActiveColor();

	/**
	 * Sets the color used for active nodes.
	 */
	void setActiveColor(String color);

	/**
	 *  Gets the color used for inactive nodes.
	 */
	String getInactiveColor();

	/**
	 * Sets the color used for inactive resources.
	 */
	void setInactiveColor(String color);

	/**
	 * Is the type of the node written?
	 */
	boolean addType();

	void setAddType(boolean flag);

	/**
	 * Is the name of the node written?
	 */
	boolean addName();

	void setAddName(boolean flag);

	/**
	 * Is the location of the resource written?
	 */
	boolean addLocation();

	void setAddLocations(boolean flag);

	/**
	 * Indicates if simple resources are printed with their current value.
	 */
	boolean addSimpleValues();

	void setAddSimpleValues(boolean flag);

	/**
	 * Gets the shape of the node.
	 */
	String getShape();

	/**
	 * Sets the shape of the node to a new shape.
	 */
	void setShape(String shape);
}
