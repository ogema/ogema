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

public class Color {
	private String background = "#97C2FC";
	private String border = "#2B7CE9";
	private String fontColor = "black";
	private String fontFace = "verdana";
	private Highlight highlight = new Highlight();

	public Color() {
	}

	public Color(String bg, String border, String fontColor, String fontFace, Highlight highlight) {
		this.background = bg;
		this.border = border;
		this.fontColor = fontColor;
		this.fontFace = fontFace;
		this.highlight = highlight;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getBorder() {
		return border;
	}

	public void setBorder(String border) {
		this.border = border;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public String getFontFace() {
		return fontFace;
	}

	public void setFontFace(String fontFace) {
		this.fontFace = fontFace;
	}

	class Highlight {
		private String border = "yellow";
		private String background = "orange";
	}
}
