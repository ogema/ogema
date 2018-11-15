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
