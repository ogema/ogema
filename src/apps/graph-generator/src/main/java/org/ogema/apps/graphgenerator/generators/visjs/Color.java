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
