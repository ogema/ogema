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
package org.ogema.core.channelmanager.measurements;

/**
 * Quality of a {@link SampledValue} (e.g. a measurement). A Quality.GOOD indicates
 * that the value shall be used for further processing. This means it is considered
 * correct and reliable.<br>
 * In the context of schedules and most of the OGEMA TimeSeries API Quality.BAD is
 * used to model definition gaps. If, in case of measurements reported, applications
 * provide data where Quality.BAD does not indicate a gap but just doubtful measurement
 * quality and the values marked with Quality.BAD shall still be used in a standard API
 * method set the QualityBadEffect.IGNORE_QUALITY flag of the method.
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
