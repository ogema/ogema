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
package org.ogema.apps.graphgenerator.styles.gviz;

import org.ogema.apps.graphgenerator.styles.NodeStyle;

/**
 * Base class for node styles. Different styles may only need to overwrite the
 * initial settings in their constructor
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class NodeStyleBase implements NodeStyle {

	protected String activeColor, inactiveColor, shape;
	protected boolean addType, addName, addLocation, addSimpleValues;

	public NodeStyleBase() {
		activeColor = "black";
		inactiveColor = "gray";
		shape = "ellipse";
		addType = true;
		addName = true;
		addLocation = false;
		addSimpleValues = false;
	}

	@Override
	public String getActiveColor() {
		return activeColor;
	}

	@Override
	public void setActiveColor(String color) {
		activeColor = color;
	}

	@Override
	public String getInactiveColor() {
		return inactiveColor;
	}

	@Override
	public void setInactiveColor(String color) {
		inactiveColor = color;
	}

	@Override
	public boolean addType() {
		return addType;
	}

	@Override
	public void setAddType(boolean flag) {
		addType = flag;
	}

	@Override
	public boolean addName() {
		return addName;
	}

	@Override
	public void setAddName(boolean flag) {
		addName = flag;
	}

	@Override
	public boolean addLocation() {
		return addLocation;
	}

	@Override
	public void setAddLocations(boolean flag) {
		addLocation = flag;
	}

	@Override
	public boolean addSimpleValues() {
		return addSimpleValues;
	}

	@Override
	public void setAddSimpleValues(boolean flag) {
		addSimpleValues = flag;
	}

	@Override
	public String getShape() {
		return shape;
	}

	@Override
	public void setShape(String shape) {
		this.shape = shape;
	}

}
