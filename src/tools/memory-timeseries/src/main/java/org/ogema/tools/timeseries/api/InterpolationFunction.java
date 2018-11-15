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
package org.ogema.tools.timeseries.api;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * Defines a function that interpolates between two SampledValues of type
 * T (which defines the actual types of the sampled values).
 */
public interface InterpolationFunction {

	/**
	 * Interpolate between x0 and x1 to find the value at time t. Timestamp
	 * of the result is given by t, value determined by interpolation. Quality
	 * is good exactly if a sensible interpolation could be performed, which usually
	 * involves the qualities of x0 and/or x1. The type of the sampled values
	 * is given by valueType. It is assume that the timestamp of t0 preceeds or
	 * equals t and that the timestamp of x1 equals of succeeds t.
	 */
	SampledValue interpolate(SampledValue x0, SampledValue x1, long t, Class<? extends Value> valueType);

	/**
	 * Integrate from x0 to x1, assuming no further points lie between the two points. Result has
	 * units of domain type times ms. Integration is defined only for integer and floating point
	 * values. If the sampled values define an invalid integration range (e.g. either value has
	 * a bad quality in linear interpolation), this returns zero. The timestamp of x0 must not be
	 * larger than the timestamp of x1.
	 */
	Value integrate(SampledValue x0, SampledValue x1, Class<? extends Value> valueType);

	/**
	 * Gets the interval on which the interpolation between the two support points is positive.
	 * The value at the lower timestamp may be zero for linear interpolation if none of the other
	 * points is zero. The larger timestamp is exclusive in the resulting interval. For the
	 * interpolation mode NONE, the 2nd argument is ignored (since this can return only a single
	 * interval). Returns an empty interval if no suitable positive domain is found.
	 */
	TimeInterval getPositiveInterval(SampledValue x0, SampledValue x1, Class<? extends Value> valueType);
}
