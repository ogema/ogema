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
package org.ogema.core.timeseries;

/**
 * Possible settings for the interpretation of a schedule, i.e. how a set of pairs of time and value {(t,x)} is
 * interpreted. Defines if and how values for times are calculated, for which no timestamp explicitly exists (i.e. how
 * the values are interpolated). The default interpolation mode is NONE.
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
