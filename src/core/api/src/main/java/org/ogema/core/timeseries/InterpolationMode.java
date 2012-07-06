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
package org.ogema.core.timeseries;

/**
 * Possible settings for the interpretation of a schedule, i.e. how a set of pairs of time and value {(t,x)} is
 * interpreted. Defines if and how values for times are calculated, for which no timestamp explicitly exists (i.e. how
 * the values are interpolated).
 */
public enum InterpolationMode {
	/**
	 * Schedules is considered to be a step-function. The value chosen for a time t is that of the entry which is
	 * closest in time to t. If t is smaller the time entry of any schedule entry, no valid value exists.
	 */
	STEPS(0),
	/**
	 * Values for t are linearly interpolated between adjacent entries. If t is outside the time interval spanned by the
	 * schedule entries, no valid value is defined.
	 */
	LINEAR(1),
	/**
	 * Schedule is a step function with the entries being centered at the steps. The value chosen for a time t is that
	 * of the entry which is closest in time to t.
	 */
	NEAREST(2),
	/**
	 * Schedule is defined only at points given. No valid value exists for any other timestamp.
	 */
	NONE(3);

	private final int mode;

	private InterpolationMode(int mode) {
		this.mode = mode;
	}

	public int getInterpolationMode() {
		return mode;
	}

	public static InterpolationMode getInterpolationMode(int mode) {
		switch (mode) {
		case 0:
			return InterpolationMode.STEPS;
		case 1:
			return InterpolationMode.LINEAR;
		case 2:
			return InterpolationMode.NEAREST;
		case 3:
			return InterpolationMode.NONE;
		default:
			return null;
		}
	}

}
