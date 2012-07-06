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
	 * involves the qualities of x0 and/or x1. Thre type of the sampled values
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

	//    /**
	//     * Integrate over the abosolute from x0 to x1, assuming no further points lie between the two points.
	//     * Note that the result can still be negative if the timestamp of x1 preceeds that of x0.
	//     * @see {@link #integrate(org.ogema.channelmanager.measurements.SampledValue, org.ogema.channelmanager.measurements.SampledValue, java.lang.Class) }
	//     */
	//    Value integrateAbsolute(SampledValue x0, SampledValue x1, Class<? extends Value> valueType);
}
