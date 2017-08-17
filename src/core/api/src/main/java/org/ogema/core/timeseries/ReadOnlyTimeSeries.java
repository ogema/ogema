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
package org.ogema.core.timeseries;

import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.ValueResource;

/**
 * Function over time that is read-only for OGEMA applications. The function is defined by a set of support points
 * (t,x,q) that define the function to have value x and quality q at time t and an interpolation mode that defines how
 * values for intermediate times are inferred from the support points. Bad qualities can be used to model gaps in the
 * range of the function.<br>
 * 
 * Entries in a ReadOnlyTimeSeries can not be modified, but values may be written by the OGEMA framework, e.g. if this
 * represents log data.
 */
public interface ReadOnlyTimeSeries {

	/**
	 * Gets a value for given time, taking into account the interpolation mode.
	 * 
	 * @param time
	 *            time for which the value shall be returned
	 * @return a newly-created SampledValue object for the given time. Due to interpolation a valid return can be
	 *         created even for times for which no schedule entry exists - as long as time is within the schedule range.
	 *         If time is outside the schedule range, null is returned. If any of the schedule entries used in the
	 *         interpolation has a bad quality, the result's quality is bad. Otherwise, the result's quality is good.
	 */
	SampledValue getValue(long time);

	/**
	 * Gets the first entry in the schedule for which {@code timestamp >= time}. Since this method only works against the
	 * defined entries its result does not depend on the interpolation mode.
	 * <br>
	 * In order to retrieve the first value in the schedule, pass {@value Long#MIN_VALUE} as argument.
	 * 
	 * @param time
	 *            Minimum time for the schedule entry.
	 * @return Gets a copy of the schedule entry. Returns null if no schedule entry satisfies the time constraint.
	 */
	SampledValue getNextValue(long time);
	
	/**
	 * Gets the first entry in the schedule for which {@code timestamp <= time}. Since this method only works against the
	 * defined entries its result does not depend on the interpolation mode.
	 * <br>
	 * In order to retrieve the last value in the schedule, pass {@value Long#MAX_VALUE} as argument.
	 * 
	 * @param time
	 *            Maximum time for the schedule entry.
	 * @return Gets a copy of the schedule entry. Returns null if no schedule entry satisfies the time constraint.
	 */
	SampledValue getPreviousValue(long time);

	/**
	 * Gets all values (time series) from startTime. 
	 * 
	 * @param startTime
	 *            Time of the first value in the time series in ms since epoche. inclusive
	 * @return A List of value objects or an empty list if no matching object has been found.
	 */
	List<SampledValue> getValues(long startTime);

	/**
	 * Gets all values (time series) from startTime until endTime.
	 * 
	 * @param startTime
	 *            Time of the first value in the time series in ms since epoche. inclusive
	 * @param endTime
	 *            Time of the last value in the time series in ms since epoche. exclusive
	 * @return A List of value objects or an empty list if no matching objects have been found.
	 */
	List<SampledValue> getValues(long startTime, long endTime);

	/**
	 * Reads how the schedule entries are to be interpreted to yield a function over time.
	 * 
	 * @return returns the current interpolation mode set for the time series.
	 */
	InterpolationMode getInterpolationMode();
	
	/**
	 * Returns true if and only if this time series contains no points.
	 * @return
	 */
	boolean isEmpty();
	
	/**
	 * Return true if and only if this time series contains no points in the specified 
	 * interval (both time stamps included)
	 * @param startTime	
	 * @param endTime
	 * @return
	 */
	boolean isEmpty(long startTime, long endTime);
	
	/**
	 * Returns the number of data points in the time series.
	 */
	int size();
	
	/**
	 * Returns the number of data points within the specified interval (both time stamps
	 * included)
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	int size(long startTime, long endTime);
	
	/**
	 * Get an iterator over all points in the time series
	 * @return
	 */
	Iterator<SampledValue> iterator();
	
	/**
	 * Get an iterator over all points in the requested interval
	 * @param startTime 
	 * 			Start time of the interval. Inclusive. 
	 * @param endTime 
	 * 			End time of the interval. Inclusive.
	 * @return
	 */
	Iterator<SampledValue> iterator(long startTime, long endTime);

	/**
	 * Get time of last write operation into time series.
	 * 
	 * @return time of the last write operation into the time series. TimeSeries
	 * implementations that cannot sensibly support this, as well as instances of
	 * implementations that support it but have never been written to (virtual 
	 * schedules), return null.
	 * @deprecated Only schedules can sensibly support this. For getting the time
	 * of the last write entry into a schedule, use {@link ValueResource#getLastUpdateTime() }, instead.
	 * If you want to access the entry of the schedule with the latest time stamp, use
	 * getPreviousValue(Long.MAX_VALUE).
	 */
	@Deprecated
	Long getTimeOfLatestEntry();
}
