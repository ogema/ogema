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
package org.ogema.resourcemanager.impl.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ogema.core.model.units.ColourResource;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ColourTest extends OsgiTestBase {
	
	@Test
	public void hexaConversionWorks() {
		ColourResource color = resMan.createResource("color1", ColourResource.class);
		String[] testStrings = new String[] {"FFFFFF", "#000000", "A47B3F", "#1E0F9A", "123456", "FEEFFE"}; // use random strings?
		for (String testString: testStrings) {
			String converted = convertBackAndForth(color, testString);
			assertEquals("Error in converting hexadecimal colour string",cleanHexa(testString.toLowerCase()), converted.toLowerCase());
		}
		color.delete();
	}

	@Test
	public void hslAndHsvConversionWorks() {
		ColourResource color = resMan.createResource("color1", ColourResource.class);
		float epsilon = 0.05F;
		float[] testValues0 = new float[]{ 0, 0, 0 };
		float[] testValues1 = new float[]{ 180, 0.6F, 0.7F };
		float[] testValues2 = new float[]{ 312.34F, 0.23F, 0.78F };
		float[] testValues3 = new float[]{ 64F, 0.5F, 0.2F };
		float[] testValues4 = new float[]{ 5F, 0.1F, 0.1F };
		assertConversionWorked(testValues0, convertBackAndForthHsl(color, testValues0), epsilon);
		assertConversionWorked(testValues1, convertBackAndForthHsl(color, testValues1), epsilon);
		assertConversionWorked(testValues2, convertBackAndForthHsl(color, testValues2), epsilon);
		assertConversionWorked(testValues3, convertBackAndForthHsl(color, testValues3), epsilon);
		assertConversionWorked(testValues4, convertBackAndForthHsl(color, testValues4), epsilon);
		assertConversionWorked(testValues0, convertBackAndForthHsv(color, testValues0), epsilon);
		assertConversionWorked(testValues1, convertBackAndForthHsv(color, testValues1), epsilon);
		assertConversionWorked(testValues2, convertBackAndForthHsv(color, testValues2), epsilon);
		assertConversionWorked(testValues3, convertBackAndForthHsv(color, testValues3), epsilon);
		assertConversionWorked(testValues4, convertBackAndForthHsv(color, testValues4), epsilon);
		color.delete();
	}
	
	private static void assertConversionWorked(float[] in, float[] out, float epsilon) {
		// FIXME
		StringBuilder t = new StringBuilder("[");
		for (float i : in ) {
			t.append(i + ", ");
		}
		t.append("], [");
		for (float i : out ) {
			t.append(i + ", ");
		}
		t.append("]");
		System.out.println("   ooo  in, out: " + t);
		
		assertEquals("Converted array has unexepected length",in.length, out.length);
		for (int i=0;i < in.length; i++) {
			assertEquals("Converted value not equal to input at position " + i, in[i], out[i], epsilon);
		}
	}
	
	private static String convertBackAndForth(ColourResource color, String hexaIn) {
		color.setHexadecimal(hexaIn);
		return color.getHexadecimal();
	}
	
	private static float[] convertBackAndForthHsl(ColourResource color, float[] values) {
		color.setHSL(values);
		return color.getHSL();
	}
	
	private static float[] convertBackAndForthHsv(ColourResource color, float[] values) {
		color.setHSV(values);
		return color.getHSV();
	}
	
	private static String cleanHexa(String in) {
		if (in.startsWith("#"))
			in = in.substring(1);
		return in;
	}
	
}
