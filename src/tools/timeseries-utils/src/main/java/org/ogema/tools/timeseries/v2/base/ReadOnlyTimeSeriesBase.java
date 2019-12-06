/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.timeseries.v2.base;

import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Provides default implementations for many methods in {@link ReadOnlyTimeSeries}. 
 * Note that the default implementations may be quite inefficient.
 */
public interface ReadOnlyTimeSeriesBase extends ReadOnlyTimeSeries {

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
	default SampledValue getValue(long time) {
		final Iterator<SampledValue> it0 = iterator(time, time);
		if (it0.hasNext())
			return it0.next();
		final InterpolationMode mode = getInterpolationMode();
		if (mode == null || mode == InterpolationMode.NONE) {
			return null;
		}
		final SampledValue prev = getPreviousValue(time);
		final SampledValue next = getNextValue(time);
		if (prev == null)
			return next != null && mode == InterpolationMode.NEAREST ? new SampledValue(next.getValue(), time, next.getQuality()) : null;
		if (next == null) {
			if (mode == InterpolationMode.NEAREST || mode == InterpolationMode.STEPS)
				return new SampledValue(prev.getValue(), time, prev.getQuality());
			return null;
		}
		switch (mode) { // here both prev and next are != null
		case STEPS:
			return new SampledValue(prev.getValue(), time, prev.getQuality());
		case NEAREST:
			final SampledValue nearest = Math.abs(prev.getTimestamp() - time) <= Math.abs(next.getTimestamp() - time) ? prev: next;
			return new SampledValue(nearest.getValue(), time, nearest.getQuality());
		case LINEAR:
			final double v1 = prev.getValue().getDoubleValue();
			final double v2 = next.getValue().getDoubleValue();
			final double value = v1 + (v2-v1) * (time - prev.getTimestamp()) / (next.getTimestamp() - prev.getTimestamp());
			final Quality q = prev.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD;
			return new SampledValue(new DoubleValue(value), time, q);
		default:
			return null;
		}
	}
	

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
	default SampledValue getNextValue(long time) {
		final Iterator<SampledValue> it = iterator(time, Long.MAX_VALUE);
		if (it.hasNext()) {
			return it.next();
		}
		return null;	
	}
	
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
	default SampledValue getPreviousValue(long time) {
		long startTime = time;
		long interval = 10;
		while (true) {
			final Iterator<SampledValue> it0 = iterator(startTime,time);
			if (!it0.hasNext()) {
				if (startTime == Long.MIN_VALUE)
					break;
				interval = interval * 1000;
				startTime = startTime <= Long.MIN_VALUE + interval ? Long.MIN_VALUE : startTime - interval;
				continue;
			}
			SampledValue last = it0.next();
			while (it0.hasNext()) {
				final SampledValue sv = it0.next();
				if (sv.getTimestamp() > time) // should not happen, safety measure
					break;
				last = sv;
			}
			return last;
		}
		return null;
	}

	/**
	 * Gets all values (time series) from startTime. 
	 * 
	 * @param startTime
	 *            Time of the first value in the time series in ms since epoche. inclusive
	 * @return A List of value objects or an empty list if no matching object has been found.
	 */
	default List<SampledValue> getValues(long startTime) {
		return getValues(startTime, Long.MAX_VALUE);
	}
	
	/**
	 * Returns true if and only if this time series contains no points.
	 * @return
	 */
	default boolean isEmpty() {
		return isEmpty(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	/**
	 * Return true if and only if this time series contains no points in the specified 
	 * interval (both time stamps included)
	 * @param startTime	
	 * @param endTime
	 * @return
	 */
	default boolean isEmpty(long startTime, long endTime) {
		return !iterator(startTime, endTime).hasNext();
	}
	
	/**
	 * Returns the number of data points in the time series.
	 */
	default int size() {
		final Iterator<SampledValue> it = iterator();
		int cnt = 0;
		while (it.hasNext()) {
			it.next();
			cnt++;
		}
		return cnt;
	}
	
	/**
	 * Returns the number of data points within the specified interval (both time stamps
	 * included)
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	default int size(long startTime, long endTime) {
		final Iterator<SampledValue> it = iterator(startTime, endTime);
		int cnt = 0;
		while (it.hasNext()) {
			it.next();
			cnt++;
		}
		return cnt;
	}
	
	/**
	 * Get an iterator over all points in the time series
	 * @return
	 */
	default Iterator<SampledValue> iterator() {
		return iterator(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	/**
	 * Get an iterator over all points in the requested interval
	 * @param startTime 
	 * 			Start time of the interval. Inclusive. 
	 * @param endTime 
	 * 			End time of the interval. Inclusive.
	 * @return
	 */
	default Iterator<SampledValue> iterator(long startTime, long endTime) {
		return getValues(startTime, endTime == Long.MAX_VALUE ? endTime : endTime + 1).iterator();
	}

	/**
	 * Get time of last write operation into time series.
	 * 
	 * @return time of the last write operation into the time series. TimeSeries
	 * implementations that cannot sensibly support this, as well as instances of
	 * implementations that support it but have never been written to (virtual 
	 * schedules), return null.
	 * @deprecated 
	 */
	@Deprecated
	default Long getTimeOfLatestEntry() {
		return null;
	}
	
}
