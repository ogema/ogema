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
package org.ogema.core.model.units;

import org.ogema.core.model.array.FloatArrayResource;

/**
 * Represents colour as an RGB value. 
 */
public interface ColourResource extends FloatArrayResource {

	/**
	 * Returns the colour in RGB representation, i.e. 
	 * an array of three values, each in the range [0,1].
	 */
	@Override
	public float[] getValues();
	
	/**
	 * Set the colour as an RGB value (red, green, blue).
	 * The array must contain exactly three entries (RGB), in 
	 * the range [0,1].
	 */
	@Override
	public boolean setValues(float[] values);
	
	/**
	 * Returns the colour in HSL representation 
	 * (hue, saturation, lightness). Value ranges are
	 * [0, 360), [0,1], [0,1].
	 */
	public float[] getHSL();
	
	/**
	 * Returns the colour in HSV representation 
	 * (hue, saturation, value). Value ranges are
	 * [0, 360), [0,1], [0,1].
	 */
	public float[] getHSV();
	
	/**
	 * Returns a hexadecimal RGB representation of the colour, consisting of six characters, 
	 * such as "FF88AA", used e.g. to represent colour in HTML. 
	 * Note that the return value does not contain a leading '#'.
	 */
	public String getHexadecimal();
	
	/**
	 * Set the colour value in HSL representation 
	 * (hue, saturation, lightness). Allowed value ranges are
	 * [0, 360), [0,1], [0,1].<br>
	 * Note the values are converted to RGB representation internally, 
	 * so that in particular the {@link #getValues()} method will still return
	 * the values as RGB.
	 */
	public boolean setHSL(float[] hsl);
	
	/**
	 * Set the colour value in HSV representation 
	 * (hue, saturation, value). Allowed value ranges are
	 * [0, 360), [0,1], [0,1].<br>
	 * Note the values are converted to RGB representation internally, 
	 * so that in particular the {@link #getValues()} method will still return
	 * the values as RGB.
	 */
	public boolean setHSV(float[] hsv);
	
	/**
	 * Set the colour value in hexadecimal RGB representation, e.g.
	 * "FF88AA", or "#FF88AA".<br>
	 * Note the values are converted to ordinary RGB representation internally, 
	 * so that in particular the {@link #getValues()} method will still return
	 * the values as RGB.
	 */
	public boolean setHexadecimal(String hexadecimalColour);
	
}
