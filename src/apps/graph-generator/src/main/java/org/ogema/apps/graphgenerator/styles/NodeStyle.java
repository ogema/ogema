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
