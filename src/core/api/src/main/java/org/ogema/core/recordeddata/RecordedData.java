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

import java.util.List;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * A set of data points recorded by the framework. Technically, this is a ReadOnlyTimeSeries with
 * {@link InterpolationMode} NONE. Possible filters to effectively read out this are configured using
 * {@link ReductionMode}s. RecordedData are collected only for active resources that have been explicitly configured to
 * be logged.
 */
public interface RecordedData extends ReadOnlyTimeSeries {

	/**
	 * Configure automated writing of values from the parent resource into the database. Data logging for this
	 * RecordedData object will be activated after the first call to this method.
	 * 
	 * The configuration will be stored persistently. Changes in a configuration object held by the API client will only
	 * come into effect after a call to this method.
	 * 
	 * Data logging can be disabled by calling this method with a "null" reference. In this case the persistently stored
	 * configuration will be deleted, but the existing logging information will be kept in storage.
	 * 
	 * An application needs special permissions to invoke this method.
	 * 
	 * @param configuration
	 *            a configuration object or null to disable data logging
	 */
	public void setConfiguration(RecordedDataConfiguration configuration);

	/**
	 * Get a copy of the current configuration object of this RecordedData instance. If such a copy does not exist a new
	 * instance with default values will be created.
	 * 
	 * @return copy of the current configuration object or null, if logging is disabled.
	 */
	public RecordedDataConfiguration getConfiguration();

	/**
	 * Get all values (time series) from startTime until endTime interpolated to a fixed interval. The timestamp of the
	 * SampleValue objects in the result list contains the start time of the interval. If {@link ReductionMode#NONE}
	 * is given then this method returns the same as {@link ReadOnlyTimeSeries#getValues(long, long)}. If
	 * {@link ReductionMode#MIN_MAX_VALUE} is given then the returned list will contain for each
	 * interval two values. The first value is the minimum and the second the maximum. E.g. if
	 * interval is 2 then list.get(0) contains the minimum and list.get(1) the maximum of the first interval and
	 * respectively list.get(2) the minimum and list.get(3) the maximum of the second interval.<br>
	 * If for any reason there is no value available in an interval then a {@link SampledValue} with
	 * {@link Quality#BAD} is returned.
	 * 
	 * @param startTime
	 *            Time of the first value in the time series in ms since epoche. inclusive
	 * @param endTime
	 *            Time of the last value in the time series in ms since epoche. exclusive
	 * @param interval
	 *            fixed interval between individual timestamps in ms
	 * @param mode
	 *            reduction mode to be used for the values
	 * @return A List of value objects or an empty list if now matching object have been found.
	 */
	public List<SampledValue> getValues(long startTime, long endTime, long interval, ReductionMode mode);
	
	/**
	 * The resource path of the logged resource.
	 * 
	 * @return
	 * 		The resource path this belongs to.
	 */
	public String getPath();

}
