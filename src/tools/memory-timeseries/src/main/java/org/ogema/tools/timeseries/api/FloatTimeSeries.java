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

import java.util.List;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Definition of a float-valued time series that is interpreted as a function f
 * : Z-&gt;R.<br>
 *
 * Please note that this class is experimental and may be subject to change. If
 * you need stable code, then use the base interface MemoryTimeSeries, instead.
 */
public interface FloatTimeSeries extends MemoryTimeSeries {

	/**
	 * Multiply all values by a constant factor.
	 */
	void multiplyBy(float factor);

	/**
	 * Multiply by another time series (whose value type must be convertible to float)
	 */
	void multiplyBy(final ReadOnlyTimeSeries factor);

	/**
	 * Return a time series that is a copy of this multiplied by the given
	 * factor.
	 */
	FloatTimeSeries times(float factor);

	/**
	 * Return a new time series that is the result of point-wise multiplication
	 * of this with the other time series (whose value type must be convertible to float).
	 */
	FloatTimeSeries times(final ReadOnlyTimeSeries other);

	/**
	 * Add the constant addend to all entries of this.
	 */
	void add(float addend);

	/**
	 * Add the time series other (which values must be convertible to float)
	 * to the values of this.
	 */
	void add(ReadOnlyTimeSeries other);

	/**
	 * Returns a new time series that equals this plus an addend added to all of
	 * this' entries.
	 */
	FloatTimeSeries plus(float addend);

	/**
	 * Returns a time series that is the sum of this and other (whose value type must be convertible to float). 
	 * Sum is to be understood point-wise. The domain of the sum equals the intersection of
	 * the domains of this and other.
	 */
	FloatTimeSeries plus(final FloatTimeSeries other);

	/**
	 * Integrate the function over the domain [t0;t1). If t1 is smaller than t0,
	 * return the negative the integration over [t1;t0), instead. The open
	 * interval on the integer range is to be understood as the equivalent to
	 * the open interval over the reals. I.e., the interval [0;1) has size one,
	 * despite containing only a single point.
	 *
	 * @return returns the result of integration as average value in interval
	 * times interval size. Interval size is in ms, not in s.
	 */
	float integrate(long t0, long t1);

	/**
	 * Integration over a time interval. Returns zero for non-existing time intervals.
	 * If an integration with reverse time order is required, use {@link #integrate(long, long)}, instead.
	 * 
	 * @see #integrate(long, long)
	 */
	float integrate(TimeInterval interval);

	/**
	 * Integrate the point-wise absolute of this function over the domain
	 * [t0;t1). The result can be negative if t1 is smaller than t0.
	 *
	 * @see #integrate(long, long)
	 */
	float integrateAbsolute(long t0, long t1);

	/**
	 * Returns zero for non-existing time intervals.
	 * If an integration with reverse time order is required, use {@link #integrateAbsolute(long, long)}, instead.
	 * @see #integrateAbsolute(long, long) 
	 */
	float integrateAbsolute(TimeInterval interval);

	/**
	 * Integrate the positive range of the function over the given domain
	 * @param interval domain to integrate over.
	 * @return returns the result of integration as average positive value in interval
	 * times interval size. Interval size is in ms, not in s. For non-existing time intervals, this returns zero.
	 */
	float integratePositive(TimeInterval interval);

	/**
	 * @see #integratePositive(org.ogema.tools.timeseries.api.TimeInterval) 
	 */
	float integratePositive(long t0, long t1);
	
	float getAverage(long t0, long t1);
	
	/**
	 * Returns a reduced set of points in the interval, obtained by downsampling 
	 * to the specified time interval. 
	 * This never increases the number of points in the interval, with respect to
	 * the original set of points returned by {@link #getValues(long, long)}.
	 * @param t0
	 * @param t1
	 * @param minimumInterval
	 * @return
	 */
	List<SampledValue> downsample(long t0, long t1, long minimumInterval);

	/**
	 * Gets the maximum value in the interval [t0;t1) for which the quality is
	 * good. The result's time stamp equals to the timestamp of the first
	 * maximum. If no maximum with a good value exists in the interval, or if
	 * the interval is not properly defined, the result's quality is BAD.
	 */
	SampledValue getMax(long t0, long t1);

	/**
	 * Gets the minimum value in the interval [t0;t1) for which the quality is
	 * good. The result's time stamp equals to the timestamp of the first
	 * minimum. If no minimum with a good value exists in the interval, or if
	 * the interval is not properly defined, the result's quality is BAD.
	 */
	SampledValue getMin(long t0, long t1);

	/**
	 * Gets function that equals a point-wise absolute value of this.
	 */
	FloatTimeSeries getAbsolute();

	/**
	 * Sets this to a TimeSeries that has the given value for all times.
	 */
	void setConstant(float value);

	/**
	 * Gets an ordered list of intervals on which the time series is defined an
	 * positive over a given search interval. The function may be zero for the
	 * starting times of the intervals and at individual timestamps within the
	 * interval (i.e. a linear zig-zag between zero and one would be a single interval).
	 * 
	 * FIXME currently this is implemented for {@link InterpolationMode} LINEAR, only.
	 *
	 * @param searchInterval Definition of the interval over which to search for
	 * positive values.
	 * @return A list of all intervals where the time series is positive,
	 * intersected by the search interval (to constrain possible infinities
	 * caused by {@link InterpolationMode}s STEPS and NEAREST.
	 */
	public List<TimeInterval> getPositiveDomain(TimeInterval searchInterval);

	/**
	 * Try to optimize this representation of a function while leaving the
	 * function that is represented unchanged. Examples can be removing leading
	 * and trailing entries with bad quality that leave the definition range
	 * unchanged or removing intermediate support points that would equal the
	 * interpolated value, anyways. The intended use is calling this after a
	 * calculation before the result is written back to a schedule to minimize
	 * the overhead that latter applications have with this time series.
	 */
	void optimizeRepresentation();
}
