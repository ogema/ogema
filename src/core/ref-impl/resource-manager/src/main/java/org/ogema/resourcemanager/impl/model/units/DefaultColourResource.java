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
package org.ogema.resourcemanager.impl.model.units;

import org.ogema.core.model.units.ColourResource;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.model.array.DefaultFloatArrayResource;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 * @author cnoelle
 * 
 * See https://en.wikipedia.org/wiki/HSL_and_HSV
 */
public class DefaultColourResource extends DefaultFloatArrayResource implements ColourResource {
	
	private static final String HSL_HSV_ERROR_MESSAGE = "Argument must be an array of three elements, with ranges [0, 360), [0,1], [0,1].";
	private static final String HEXA_ERROR_MESSAGE = "Not a valid hexadecimal colour string: ";
	
	public DefaultColourResource(VirtualTreeElement el, String path, ApplicationResourceManager resMan) {
		super(el, path, resMan);
		if (el.getData().getFloatArr() == null || el.getData().getFloatArr().length != 3)
			el.getData().setFloatArr(new float[3]);
	}

	@Override
	public boolean setValues(float[] value) {
		if (!isValid(value))
			throw new IllegalArgumentException("Argument must be an array of three elements in the range [0,1].");
		return super.setValues(value);
	}
	
	@Override
	public boolean setHSL(float[] hsl) {
		float[] values = getRgbFromHsl(hsl);
		if (!isValid(values))
			throw new IllegalArgumentException(HSL_HSV_ERROR_MESSAGE);
		return super.setValues(values);
	}
	
	@Override
	public boolean setHSV(float[] hsv) {
		float[] values = getRgbFromHsv(hsv);
		if (!isValid(values))
			throw new IllegalArgumentException(HSL_HSV_ERROR_MESSAGE);
		return super.setValues(values);
	}
	
	@Override
	public float[] getHSL() {
		float[] rgb = getValues();
		return new float[]{ getHue(rgb), getSaturationHSL(rgb), getLightness(rgb) };
	}
	
	@Override
	public float[] getHSV() {
		float[] rgb = getValues();
		return new float[]{ getHue(rgb), getSaturationHSV(rgb), getValue(rgb) };
	}
	

	@Override
	public String getHexadecimal() {
		float[] rgb = getValues();
		short r = (short) (255 * rgb[0]);
		short g = (short) (255 * rgb[1]);
		short b = (short) (255 * rgb[2]);
		return getStringRep(r) + getStringRep(g) + getStringRep(b);
	}
	
	@Override
	public boolean setHexadecimal(String hexadecimalColour) {
		if (hexadecimalColour == null || hexadecimalColour.isEmpty())
			throw new IllegalArgumentException(HEXA_ERROR_MESSAGE + hexadecimalColour);
		if (hexadecimalColour.charAt(0) == '#') 
			hexadecimalColour = hexadecimalColour.substring(1);
		if (hexadecimalColour.length() == 3) {
			char[] arr = new char[] {hexadecimalColour.charAt(0), '0', 
					hexadecimalColour.charAt(1), '0',hexadecimalColour.charAt(2), '0'};
			hexadecimalColour = new String(arr);
		}
		if (hexadecimalColour.length() != 6) 
			throw new IllegalArgumentException(HEXA_ERROR_MESSAGE + hexadecimalColour);
		short r; short g; short b;
		try {
			r = Short.parseShort(hexadecimalColour.substring(0, 2), 16);
			g = Short.parseShort(hexadecimalColour.substring(2, 4), 16);
			b = Short.parseShort(hexadecimalColour.substring(4, 6), 16);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(HEXA_ERROR_MESSAGE + hexadecimalColour);
		}
		float rf = ((float) r) / 255;
		float gf = ((float) g) / 255;
		float bf = ((float) b) / 255;
		return super.setValues(new float[] {rf, gf, bf});
	}
	
	private static String getStringRep(short value) {
		String str = Integer.toHexString(value);
		if (str.length() == 1) 
			str = "0" + str;
		return str;
	}
	
	private static float getHue(float[] rgb) {
		float chroma = getChroma(rgb);
		if (chroma == 0) return 0;
		float r = rgb[0]; float g = rgb[1]; float b = rgb[2];
		float hinit;
		if (r >= g && r >= b)
			hinit = ((g-b)/chroma);
		else if (g >= r && g >= b)
			hinit = ((b-r)/chroma) + 2;
		else
			hinit = ((r-g)/chroma) + 4;
		while (hinit < 0) 
			hinit += 6;
		return (hinit % 6) * 60; 
	}
	
	private static float getLightness(float[] rgb) {
		return (getMax(rgb) + getMin(rgb))/2;
	}
	
	private static float getValue(float[] rgb) {
		return getMax(rgb);
	}
	
	private static float getSaturationHSV(float[] rgb) {
		float chroma = getChroma(rgb);
		if (chroma == 0) return 0;
		return chroma / getMax(rgb);
	}
	
	private static float getSaturationHSL(float[] rgb) {
		float chroma = getChroma(rgb);
		if (chroma == 0) return 0;
		return chroma / (1 - Math.abs(2*getLightness(rgb)-1));
	}
	
	private static float getChroma(float[] rgb) {
		return getMax(rgb) - getMin(rgb);
	}
	
	private static float getMax(float[] rgb) {
		return Math.max(Math.max(rgb[0], rgb[1]),rgb[2]);
	}
	
	private static float getMin(float[] rgb) {
		return Math.min(Math.min(rgb[0], rgb[1]),rgb[2]);
	}
	
	private static float[] getRgbFromHsv(float[] hsv) {
		float[] xchprime = getXchprimeFromHsv(hsv);
		float[] rgbPrimes = getRgbPrimes(xchprime);
		float min = hsv[2] - xchprime[1];
		return new float[] { rgbPrimes[0] + min, rgbPrimes[1] + min, rgbPrimes[2] + min };
	}
	
	private static float[] getRgbFromHsl(float[] hsl) {
		float[] xchprime = getXchprimeFromHsl(hsl);
		float[] rgbPrimes = getRgbPrimes(xchprime);
		float  min = hsl[2] - xchprime[1]/2;
		return new float[] { rgbPrimes[0] + min, rgbPrimes[1] + min, rgbPrimes[2] + min };
	}
	
	private static float[] getXchprimeFromHsv(float[] hsv) {
		float c = hsv[2] * hsv[1];
		float hprime = hsv[0] / 60;
		float x = c * (1 - Math.abs(hprime % 2 - 1) );
		return new float[]{x,c,hprime};
	}
	
	private static float[] getXchprimeFromHsl(float[] hsl) {
		float c = (1 - Math.abs(2 * hsl[2] - 1)) * hsl[1];
		float hprime = hsl[0] / 60;
		float x = c * (1 - Math.abs(hprime % 2 - 1) );
		return new float[]{x,c,hprime};
	}
	
	private static float[] getRgbPrimes(float[] xchprime) {
		float x = xchprime[0]; float c = xchprime[1]; float hprime = xchprime[2];
		float r; float g; float b;
		if (0 <= hprime && hprime < 1) { 
			r = c; g= x; b= 0; 
		} else if (1 <= hprime && hprime < 2) {
			r = x; g = c; b = 0;
		} else if (2 <= hprime && hprime < 3) {
			r = 0; g = c; b = x;
		} else if (3 <= hprime && hprime < 4) {
			r = 0; g = x; b = c;
		} else if (4 <= hprime && hprime < 5) {
			r = x; g = 0; b = c;
		} else if (5 <= hprime && hprime < 6) {
			r= c; g = 0; b = x;
		}
		else 
			throw new IllegalArgumentException("Argument must be an array of three elements, with ranges [0, 360), [0,1], [0,1].");
		return new float[]{ r,g,b };
	}
	
	private static boolean isValid(float[] rgb) {
		if (rgb == null || rgb.length != 3)
			return false;
		if (Float.isNaN(rgb[0]) || Float.isNaN(rgb[1]) || Float.isNaN(rgb[2]))
			return false;
		if (0 > rgb[0] || 1 < rgb[0] || 0 > rgb[1] || 1 < rgb[1] ||0 > rgb[2] || 1 < rgb[2])
			return false;
		return true;
	}


}
