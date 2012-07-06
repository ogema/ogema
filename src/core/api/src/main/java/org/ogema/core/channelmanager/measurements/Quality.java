/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.core.channelmanager.measurements;

/**
 * Quality of a {@link SampledValue} (e.g. a measurement). A Quality.GOOD inidcates
 * that the value reported is considered correct and reliable. In the context of schedules
 * Quality.BAD can be used to model definition gaps. In case of measurements reported,
 * it is up to the application how they treat a measurement that was reported as 
 * Quality.BAD. <br>
 * 
 * Usually, if one or more values involved in a calculation are Quality.BAD the result should
 * be considered of Quality.BAD, too.
 */
public enum Quality {

	/**
	 * Value is not considered reliable. Calculations based on this value are probably
	 * not reliable, either.
	 */
	BAD(0),
	/**
	 * Value is reliable and can be used for further calculations.
	 */
	GOOD(1);

	private final int quality;

	private Quality(int quality) {
		this.quality = quality;
	}

	public int getQuality() {
		return quality;
	}

	public static Quality getQuality(int quality) {
		switch (quality) {
		case 0:
			return Quality.BAD;
		case 1:
			return Quality.GOOD;
		default:
			return null;
		}
	}
}
