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
package org.ogema.core.recordeddata;

/**
 * This Enum is to set up RecordedData which kind of value it will return from
 * the interval.
 * 
 */
public enum ReductionMode {
	/**
	 * calculate average of all values within the interval that shall be
	 * returned as one value
	 */
	AVERAGE,

	/** return maximum value within the interval */
	MAXIMUM_VALUE,

	/** return the minimum value within the interval */
	MINIMUM_VALUE,

	/**
	 * return two values for each interval. One giving the maximum value, one
	 * giving the minimum value
	 */
	MIN_MAX_VALUE,

	/** no reduction at all, will store current value! */
	NONE
}
