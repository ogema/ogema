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
