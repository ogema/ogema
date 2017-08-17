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
package org.ogema.tools.timeseries.iterator.api;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;

/**
 * A special data point for time series. 
 */
public interface SampledValueDataPoint extends DataPoint<SampledValue> {

	/**
	 * Get the timestamp for this DataPoint.
	 * @return
	 */
	long getTimestamp();
	
	/**
	 * @return
	 * 		The timestamp of the previous data point(s), or Long.MIN_VALUE if no
	 * 		previous point exists.
	 */
	long getPreviousTimestamp();
	
	/**
	 * @return
	 * 		The timestamp of the following data point(s), or Long.MAX_VALUE if no
	 * 		further point exists.
	 */
	long getNextTimestamp();
	
	/**
	 * Retrieve the value for a time series that is not necessarily defined by a data point
	 * at the given time stamp, but has a previous and next value, and an interpolation mode has
	 * been defined for it. The interpolation mode can be set using
	 * {@link MultiTimeSeriesIteratorBuilder#setGlobalInterpolationMode(InterpolationMode)} (same
	 * interpolation mode for all time series) or
	 * {@link MultiTimeSeriesIteratorBuilder#setIndividualInterpolationModes(java.util.List)}.
	 * If no interpolation mode is set, and the time series does not have a defined data point at
	 * this time stamp, then null is returned
	 * @param idx
	 * 		The time series index.
	 * @return
	 */
	SampledValue getElement(int idx);
	
	/**
	 * Retrieve a value for a time series that is not necessarily defined by a data point at 
	 * the given time stamp, but has a previous and next value, so interpolation
	 * can be applied.  
	 * @param idx
	 * @param interpolationMode
	 * @return
	 * 		null if the data point is not defined, a potentially interpolated data point otherwise
	 */
	SampledValue getElement(int idx, InterpolationMode interpolationMode);
	
	@Override
	SampledValueDataPoint getPrevious(int stepsBack) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * Get the sum of all time series at the given point, applying a global interpolation mode
	 * @param ignoreMissingPoints
	 * 		If this is false and one of the time series is not defined a this point (bad quality value or no value at all), Float.NaN is returned.
	 * 		If this is true, the sum of all defined time series is returned.  
	 * @param mode
	 * 		The interpolation mode used to calculate the values of time series which do not have a data point at the current timestamp.
	 * @return
	 * 		Float.NaN if the value is not defined
	 */
	float getSum(boolean ignoreMissingPoints, InterpolationMode mode);
	
}
