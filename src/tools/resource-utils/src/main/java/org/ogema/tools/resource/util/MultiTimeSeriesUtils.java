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
package org.ogema.tools.resource.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;
import org.ogema.tools.timeseries.iterator.api.DataPoint;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesBuilder;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImpl;

/**
 * Provides methods that take a collection of time series as arguments (whose value types
 * must be convertible to float), such as calculating the schedule of average values, the sum, etc. 
 */
// Like MultiTimeSeriesUtils, except that methods are implemented using the MultiIterator.
public class MultiTimeSeriesUtils {
	
	// use static methods to access this
	private MultiTimeSeriesUtils() {}
	
	/**
	 * Get the time series of average values of the provided time series arguments. This method uses a set of
	 * default parameters, which can be set explicitly in 
	 * {@link #getAverageTimeSeries(List, long, long, boolean, InterpolationMode, boolean)}.
	 * In particular, it does not fill the schedule with bad quality values at the start or end of the 
	 * requested interval, if they are not defined, it does not enforce a particular interpolation mode
	 * for the calculation, but rather uses the mode defined by the schedules themselves, and it does 
	 * not ignore definition gaps in individual schedules - they lead to bad quality values.
	 *
	 * Note: this copies all data points upon construction of the time series. In order to evaluate the underlying
	 * time series on demand, use a {@link MultiTimeSeriesBuilder} instead.
	 * 
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static FloatTimeSeries getAverageTimeSeries(List<ReadOnlyTimeSeries> schedules, long startTime, long endTime) {
		return genericTimeSeriesSum(schedules, startTime, endTime, false, true, null, false);
	}
	
	/**
	 * Get the time series of average values of the provided time series arguments.
	 * 
	 * Note: this copies all data points upon construction of the time series. In order to evaluate the underlying
	 * time series on demand, use a {@link MultiTimeSeriesBuilder} instead.
	 *  
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @param ignoreGaps
	 * 		if true, gaps in one of the schedules will be ignored (the value for the respective 
	 * 		interval will be the average of the remaining schedules); if false, the resulting 
	 * 		schedule will not be defined in this interval (quality BAD value). If none of the 
	 * 		passed schedules is defined in some interval, the resulting schedule will have a gap
	 * 		there in any case. Default is false.
	 * @param mode
	 * 		this can be null, in which case the interpolation mode of the passed schedules is 
	 * 		taken into account for the calculation of average values. If <tt>mode</tt> is set,
	 * 		all schedules will be treated as if they had interpolation mode <tt>mode</tt>.
	 * 		Default is null.
	 * @param generateNanForStartEndGaps
	 * 		In case the passed schedules do not define a point at the start or end of the requested
	 * 		interval, a Float.NaN value with Quality.BAD can be added. Default is false. 
	 * @return
	 */
	public static FloatTimeSeries getAverageTimeSeries(List<ReadOnlyTimeSeries> schedules, long startTime, long endTime, 
				boolean ignoreGaps, InterpolationMode mode, boolean generateNanForStartEndGaps) {
		return genericTimeSeriesSum(schedules, startTime, endTime, ignoreGaps, true, mode, generateNanForStartEndGaps);
	}
	
	/**
	 * Add up a collection of time series. This method uses a set of
	 * default parameters, which can be set explicitly in 
	 * {@link #add(List, long, long, boolean, InterpolationMode, boolean)}.
	 * In particular, it does not fill the schedule with bad quality values at the start or end of the 
	 * requested interval, if they are not defined, it does not enforce a particular interpolation mode
	 * for the calculation, but rather uses the mode defined by the schedules themselves, and it does 
	 * not ignore definition gaps in individual schedules - they lead to bad quality values.
	 * 
	 * Note: this copies all data points upon construction of the time series. In order to evaluate the underlying
	 * time series on demand, use a {@link MultiTimeSeriesBuilder} instead.
	 * 
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static FloatTimeSeries add(List<ReadOnlyTimeSeries> schedules, long startTime, long endTime) {
		return genericTimeSeriesSum(schedules, startTime, endTime, false, false, null, false);
	}
	 
	/**
	 * Add up a collection of time series. 
	 * 
	 * Note: this copies all data points upon construction of the time series. In order to evaluate the underlying
	 * time series on demand, use a {@link MultiTimeSeriesBuilder} instead.
	 * 
	 * @param schedules
	 * @param startTime
	 * 		inclusive
	 * @param endTime
	 * 		inclusive
	 * @param ignoreGaps
	 * 		if true, gaps in one of the schedules will be ignored (the value for the respective 
	 * 		interval will be the sum of the remaining schedules); if false, the resulting 
	 * 		schedule will not be defined in this interval (quality BAD value). If none of the 
	 * 		passed schedules is defined in some interval, the resulting schedule will have a gap
	 * 		there in any case. Default is false.
	 * @param mode
	 * 		this can be null, in which case the interpolation mode of the passed schedules is 
	 * 		taken into account for the calculation of sums. If <tt>mode</tt> is set,
	 * 		all schedules will be treated as if they had interpolation mode <tt>mode</tt>. 
	 * 		Default is null.
	 * @param generateNanForStartEndGaps
	 * 		In case the passed schedules do not define a point at the start or end of the requested
	 * 		interval, a Float.NaN value with Quality.BAD can be added. Default is false. 
	 * @return
	 */
	public static FloatTimeSeries add(List<? extends ReadOnlyTimeSeries> schedules, long startTime, long endTime, 
				boolean ignoreGaps, InterpolationMode mode, boolean generateNanForStartEndGaps) {
		return genericTimeSeriesSum(schedules, startTime, endTime, ignoreGaps, false, mode, generateNanForStartEndGaps);
	}

	private static FloatTimeSeries genericTimeSeriesSum(List<? extends ReadOnlyTimeSeries> schedules, 
				long startTime, long endTime, boolean ignoreGaps, boolean doAverage, InterpolationMode forcedMode,
				boolean generateNaNForStartEndGaps) throws IllegalArgumentException {
		if ( startTime > endTime)
			throw new IllegalArgumentException("start time &gt; endTime: " + startTime + ", " + endTime);
		if (schedules.isEmpty())
			return new FloatTreeTimeSeries();
		InterpolationMode targetMode = forcedMode; 
		boolean allModesEqual = true;
		if (targetMode == null) {
			for (ReadOnlyTimeSeries schedule : schedules) {
				InterpolationMode mode = schedule.getInterpolationMode();
				if (targetMode != null && mode != targetMode)
					allModesEqual = false;
				switch (mode) {
				case NONE:
					if (targetMode != null && targetMode != InterpolationMode.NONE)
						throw new IllegalArgumentException("Schedules collection contains both time series with InterpolationMode NONE and others");
					targetMode = InterpolationMode.NONE;
					break;
				case NEAREST:
					throw new UnsupportedOperationException("InterpolationMode NEAREST not implemented yet");
				case STEPS:
				case LINEAR:
					if (targetMode == InterpolationMode.NONE) 
						throw new IllegalArgumentException("Schedules collection contains both time series with InterpolationMode NONE and others");
					if (targetMode != InterpolationMode.LINEAR)
						targetMode = mode;
				}
			}
		}
		final int sz = schedules.size();
		final List<InterpolationMode> modes = (forcedMode != null ? null : new ArrayList<InterpolationMode>());
		final Map<Integer,SampledValue> lowerBoundary = getBoundaryPoints(schedules, startTime, forcedMode);
		final Map<Integer,SampledValue> upperBoundary = getBoundaryPoints(schedules, endTime, forcedMode);
		final List<Iterator<SampledValue>> iterators = new ArrayList<>();
		for (ReadOnlyTimeSeries r : schedules) { 
			iterators.add(r.iterator(startTime, endTime));
			if (modes != null)
				modes.add(r.getInterpolationMode());
		}
		final MultiTimeSeriesIterator iterator = getMultiIterator(iterators, 0, lowerBoundary, upperBoundary);
		final List<SampledValue> values = genericSum(iterator, ignoreGaps, doAverage, sz, forcedMode, modes, 
					Math.max(schedules.iterator().next().size(startTime, endTime),8)); 
		final FloatTreeTimeSeries ts = new FloatTreeTimeSeries();
		ts.addValues(values, System.currentTimeMillis());
		if (generateNaNForStartEndGaps) {
			final SampledValue start = ts.getValue(startTime);
			final SampledValue end = ts.getValue(endTime);
			if (start == null)
				ts.addValue(new SampledValue(new FloatValue(Float.NaN), startTime, Quality.BAD));
			if (end == null)
				ts.addValue(new SampledValue(new FloatValue(Float.NaN), endTime, Quality.BAD));
		}
		ts.setInterpolationMode(targetMode);
		return ts;
	}
	
	// calculate sum or average
	private static List<SampledValue> genericSum(final MultiTimeSeriesIterator iterator, final boolean ignoreGaps, final boolean doAverage, 
			final int nrSchedules, final InterpolationMode forcedMode, final List<InterpolationMode> modes, final int estimatedNr) {
		final List<SampledValue> values = new ArrayList<>(estimatedNr);
		SampledValueDataPoint dp;
		SampledValue sv;
		int cnt;
		float result;
		while (iterator.hasNext()) {
			dp = iterator.next();
			cnt = 0;
			result = 0;
			for (int n=0;n<nrSchedules;n++) {
				sv = dp.getElement(n, forcedMode != null ? forcedMode : modes.get(n));
				if (sv == null || sv.getQuality() == Quality.BAD) {
					if (!ignoreGaps) {
						 result = Float.NaN;
						 break;
					}
					continue;
				}
				cnt++;
				result += sv.getValue().getFloatValue();
			}
			if (cnt == 0) // FIXME is this correct? 
				result = Float.NaN;
			else if (doAverage)
				result = result/cnt;
			if (Float.isNaN(result)) {
				values.add(new SampledValue(FloatValue.NAN, dp.getTimestamp(), Quality.BAD));
				continue;
			} 
			values.add(new SampledValue((result == 0F ? FloatValue.ZERO : new FloatValue(result)), dp.getTimestamp(), Quality.GOOD));
		}
		return values;
	}
	
	
	private static Map<Integer,SampledValue> getBoundaryPoints(List<? extends ReadOnlyTimeSeries> timeSeries, long t, InterpolationMode forcedMode) {
		if (t == Long.MIN_VALUE || t == Long.MAX_VALUE)
			return null;
		final Map<Integer,SampledValue> map = new HashMap<>(timeSeries.size());
		for (int i = 0;i<timeSeries.size(); i++) {
			final ReadOnlyTimeSeries ts = timeSeries.get(i);
			if (!ts.isEmpty(t, t))
				continue;
			final SampledValue sv = (forcedMode != null ? ValueResourceUtils.getValueForInterpolationMode(ts, t, forcedMode) : ts.getValue(t));
			if (sv != null) 
				map.put(i, sv);
		}
		return map;
	}
	
	// XXX ?
	/**Get time series that determine the long-time behaviour after the interval. If this is not relevant
	 * (no long-time behaviour defined by schedule itself return null. If an end marker with quality bad
	 * is required return an empty list
	 */
//	private static boolean isEndMarkerRequired(Collection<ReadOnlyTimeSeries> schedules, 
//				long endTime, boolean ignoreGaps, InterpolationMode targetMode, boolean allModesEqual) {
//		if(targetMode == InterpolationMode.NONE) return false;
//		if(!ignoreGaps) return false;
//		//TODO: If all schedules have an end marker themselves the last value should always be an end marker as no
//		//values can be found for the last input value? // XXX ?
//		for (ReadOnlyTimeSeries schedule : schedules) {
//			final SampledValue last = schedule.getPreviousValue(endTime);
//			if (last == null)
//				continue;
//			if (last.getQuality() == Quality.GOOD) { 
//				return false;
//			}
//		}
//		return true;
//	}
	/**
	 * Returns the sum of the values of the passed time series at the requested point in time. If any of the time series is not defined 
	 * (value is null or has bad quality), then <tt>Float.NaN</tt> is returned in case <tt>ignoreGaps</tt>
	 * is false, and the sum of the remaining schedule values if it is true. If none of the passed schedules is 
	 * well defined, Float.NaN is returned as well. 
	 * 
	 * @param schedules
	 * @param timestamp
	 * @param ignoreGaps
	 * 		if true, gaps in one of the schedules will be ignored (the value for the respective 
	 * 		interval will be the sum of the remaining schedules); if false, a gap in one of the schedules
	 * 		will cause<tt>Float.NaN</tt> to be returned.
	 * 		If none of the passed schedules is defined at <tt>timestamp</tt>, then <tt>Float.NaN</tt> is
	 * 		defined in any case.
	 * @param mode
	 * 		this can be null, in which case the interpolation mode of the passed schedules is 
	 * 		taken into account for the calculation of sums. If <tt>mode</tt> is set,
	 * 		all schedules will be treated as if they had interpolation mode <tt>mode</tt>. 
	 * @return
	 * 		the sum, or <tt>Float.NaN</tt>, if none of the schedules is defined at the timestamp,
	 * 		or <tt>ignoreGaps</tt> is false and at least a single schedule has a gap at the time.
	 */
	public static float getSum(Collection<ReadOnlyTimeSeries> schedules, long timestamp, boolean ignoreGaps, InterpolationMode mode) {
		return genericSum(schedules, timestamp, ignoreGaps, false, mode);
	}
	
	/**
	 * Get the average value of a set of time series at a specific point in time.
	 * 
	 * @param schedules
	 * @param timestamp
	 * @param ignoreGaps
	 * 		if true, gaps in one of the schedules will be ignored (the value for the respective 
	 * 		interval will be the sum of the remaining schedules); if false, a gap in one of the schedules
	 * 		will cause<tt>Float.NaN</tt> to be returned.
	 * 		If none of the passed schedules is defined at <tt>timestamp</tt>, then <tt>Float.NaN</tt> is
	 * 		defined in any case.
	 * @param mode
	 * 		this can be null, in which case the interpolation mode of the passed schedules is 
	 * 		taken into account for the calculation of averages. If <tt>mode</tt> is set,
	 * 		all schedules will be treated as if they had interpolation mode <tt>mode</tt>. 
	 * @return
	 * 		the average value, or <tt>Float.NaN</tt>, if none of the schedules is defined at the timestamp,
	 * 		or <tt>ignoreGaps</tt> is false and at least a single schedule has a gap at the time.
	 */
	public static float getAverage(Collection<ReadOnlyTimeSeries> schedules, long timestamp, boolean ignoreGaps, InterpolationMode mode) {
		return genericSum(schedules, timestamp, ignoreGaps, true, mode);
	}

	// calculate sum or average
	private static float genericSum(Collection<ReadOnlyTimeSeries> schedules, long timestamp, boolean ignoreGaps, boolean doAverage, InterpolationMode mode) {
		int cnt = 0;
		float result = 0;
		for (ReadOnlyTimeSeries schedule: schedules) {
			final SampledValue sv;
			if (mode == null) 
				sv = schedule.getValue(timestamp);
			else
				sv = ValueResourceUtils.getValueForInterpolationMode(schedule, timestamp, mode);
			if (sv == null || sv.getQuality() == Quality.BAD) {
				if (!ignoreGaps)
					return Float.NaN;
				continue;
			}
			cnt++;
			result += sv.getValue().getFloatValue();
		}
		if (cnt == 0)
			return Float.NaN;
		return (doAverage ? result/cnt : result);
	}
	
	/**
	 * Returns the next timestamp greater than or equal to <tt>start</tt> for which a data point is 
	 * defined for one of the schedules, or <tt>Long.MAX_VALUE</tt>, if no such timestamp exists.<br>
	 * Note: this method may be considerably more inefficient than using a {@link MultiTimeSeriesIterator},
	 * in particular on log data.
	 * @param schedules
	 * @param start
	 * @return
	 */
	public static long getNextTimestamp(Collection<ReadOnlyTimeSeries> schedules, long start) {
		long t = Long.MAX_VALUE;
		for (ReadOnlyTimeSeries schedule: schedules) {
			SampledValue sv = schedule.getNextValue(start);
			if (sv == null || sv.getTimestamp() >= t)
				continue;
			t = sv.getTimestamp();
		}
		return t;
	}
	
	/**
	 * Returns the next timestamp smaller than or equal to <tt>start</tt> for which a data point is 
	 * defined for one of the schedules, or <tt>Long.MIN_VALUE</tt>, if no such timestamp exists.
	 * @param schedules
	 * @param start
	 * @return
	 */
	public static long getPreviousTimestamp(Collection<ReadOnlyTimeSeries> schedules, long start) {
		long t = Long.MIN_VALUE;
		for (ReadOnlyTimeSeries schedule: schedules) {
			SampledValue sv = schedule.getPreviousValue(start);
			if (sv == null || sv.getTimestamp() <= t)
				continue;
			t = sv.getTimestamp();
		}
		return t;
	}
	
	/**
	 * Retrieve a joint iterator over the set of time series defined by some iterators.
	 * @param iterators
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static MultiTimeSeriesIterator getMultiIterator(List<Iterator<SampledValue>> iterators) {
		return new TimeSeriesMultiIteratorImpl(iterators, 0);
	}
	
	/**
	 * Retrieve a joint iterator oer the set of time series defined by some iterators. This differs from
	 * {@link #getMultiIterator(List)} only in that the data points retrieved from the iterator also remember
	 * their preceding points - which can be retrieved via {@link DataPoint#getPrevious(int)}.
	 * 
	 * @param iterators
	 * @param storedHistoricalData
	 * 		A non-negative integer. If the value is 0, this is equivalent to {@link #getMultiIterator(List)}.
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static MultiTimeSeriesIterator getMultiIterator(List<Iterator<SampledValue>> iterators, int storedHistoricalData) {
		return new TimeSeriesMultiIteratorImpl(iterators, storedHistoricalData);
	}
	
	/**
	 * 
	 * @param iterators
	 * @param storedHistoricalData
	 * @param lowerBoundaryValues
	 * @param upperBoundaryValues
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static MultiTimeSeriesIterator getMultiIterator(List<Iterator<SampledValue>> iterators, int storedHistoricalData, 
			Map<Integer,SampledValue> lowerBoundaryValues, Map<Integer,SampledValue> upperBoundaryValues) {
		return new TimeSeriesMultiIteratorImpl(iterators, storedHistoricalData, lowerBoundaryValues, upperBoundaryValues);
	}
	
	/**
	 * Get a {@link MultiTimeSeriesIterator} for a set of time series over the requested interval. Note that boundary 
	 * points will be added for all time series that do not have a defined data point at the boundary time stamps.
	 *  
	 * @param timeSeries
	 * @param storedHistoricalData
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static MultiTimeSeriesIterator getMultiIterator(List<ReadOnlyTimeSeries> timeSeries, int storedHistoricalData,
			long startTime, long endTime) {
		final List<Iterator<SampledValue>> iterators = new ArrayList<>();
		final Map<Integer,SampledValue> lowerBoundaryValues = new HashMap<>(); 
		final Map<Integer,SampledValue> upperBoundaryValues = new HashMap<>();
		for (int n=0;n<timeSeries.size();n++) {
			final ReadOnlyTimeSeries ts = timeSeries.get(n);
			if (ts.isEmpty(startTime, startTime)) { // sic!
				final SampledValue start = ts.getValue(startTime);
				if (start != null)
					lowerBoundaryValues.put(n, start);
			}
			if (ts.isEmpty(endTime,endTime)) {
				final SampledValue end = ts.getValue(endTime);
				if (end != null)
					upperBoundaryValues.put(n, end);
			}
			iterators.add(ts.iterator(startTime, endTime));
		}
		return new TimeSeriesMultiIteratorImpl(iterators, storedHistoricalData, 
				!lowerBoundaryValues.isEmpty() ? lowerBoundaryValues : null, !upperBoundaryValues.isEmpty() ? upperBoundaryValues : null);
	}
	
}
