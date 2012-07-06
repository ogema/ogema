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

}
