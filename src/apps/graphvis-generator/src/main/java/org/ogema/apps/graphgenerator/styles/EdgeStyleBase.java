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
