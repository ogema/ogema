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
package org.ogema.tools.timeseries.v2.iterator.api;

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
	
	/**Get next element for the given index
	 * 
	 * @param idx
	 * @return null if the current/last element is the last one or no data exists for the timeseries
	 */
	SampledValue getNextElement(int idx);
	/**Get previous element for the given index
	 * 
	 * @param idx
	 * @return null if the current/last element is the first one or no data exists for the timeseries
	 */
	SampledValue getPreviousElement(int idx);
	
	@Override
	SampledValueDataPoint getPrevious(int stepsBack) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * Get the sum of all time series at the given point, applying a global interpolation mode
	 * @param ignoreMissingPoints
	 * 		If this is false and one of the time series is not defined a this point (bad quality value or no value at all), Double.NaN is returned.
	 * 		If this is true, the sum of all defined time series is returned.  
	 * @param mode
	 * 		The interpolation mode used to calculate the values of time series which do not have a data point at the current timestamp.
	 * @return
	 * 		Double.NaN if the value is not defined
	 */
	double getSum(boolean ignoreMissingPoints, InterpolationMode mode);
	
}
