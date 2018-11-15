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
