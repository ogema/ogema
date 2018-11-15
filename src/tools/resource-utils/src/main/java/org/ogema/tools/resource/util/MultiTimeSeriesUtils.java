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
import org.ogema.tools.timeseries.iterator.api.IteratorTimeSeries;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesBuilder;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;
import org.ogema.tools.timeseries.iterator.api.IteratorTimeSeries.IteratorSupplier;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ogema.tools.timeseries.iterator.api.ReductionIterator;
import org.ogema.tools.timeseries.iterator.api.ReductionIteratorBuilder;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImpl;

import com.google.common.base.Function;

/**
 * Provides methods that take a collection of time series as arguments (whose value types
 * must be convertible to float), such as calculating the schedule of average values, the sum, etc. 
 */
// TODO clean up
// TODO pure iterator sum, etc
public class MultiTimeSeriesUtils {
	
	// use static methods to access this
	private MultiTimeSeriesUtils() {}
	
	private static final Function<Collection<SampledValue>, SampledValue> AVERAGE = new Function<Collection<SampledValue>, SampledValue>() {

		@Override
		public SampledValue apply(Collection<SampledValue> input) {
			float v = 0f;
			int cnt = 0;
			for (SampledValue sv: input) {
				if (sv != null && sv.getQuality() == Quality.GOOD) {
					v += sv.getValue().getFloatValue();
					cnt++;
				}
			}
			return new SampledValue(new FloatValue(v/cnt), input.iterator().next().getTimestamp(), cnt > 0 ? Quality.GOOD : Quality.BAD);
		}
	};
	
	private static final Function<Collection<SampledValue>, SampledValue> SUM = new Function<Collection<SampledValue>, SampledValue>() {

		@Override
		public SampledValue apply(Collection<SampledValue> input) {
			float v = 0f;
			boolean qualityGood = false;
			for (SampledValue sv: input) {
				if (sv != null && sv.getQuality() == Quality.GOOD) {
					v += sv.getValue().getFloatValue();
					qualityGood  = true;
				}
			}
			return new SampledValue(new FloatValue(v), input.iterator().next().getTimestamp(), qualityGood ? Quality.GOOD : Quality.BAD);
		}
	};
	
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
	 * Similar to {@link #getAverageTimeSeries(List, long, long)}, but here the data points are evaluated when needed
	 * and not copied at construction time.
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static ReadOnlyTimeSeries getAverageTimeSeriesLazy(List<ReadOnlyTimeSeries> schedules, long startTime, long endTime) {
		return genericTimeSeriesSumLazy(schedules, startTime, endTime, false, true, null, false);
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
	 * Similar to {@link #getAverageTimeSeries(List, long, long, boolean, InterpolationMode, boolean)}, 
	 * but here the data points are evaluated when needed
	 * and not copied at construction time.
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @param ignoreGaps
	 * @param mode
	 * @param addStartEndValues
	 * @return
	 */
	 
	public static ReadOnlyTimeSeries getAverageTimeSeriesLazy(List<ReadOnlyTimeSeries> schedules, long startTime, long endTime, 
				boolean ignoreGaps, InterpolationMode mode, boolean addStartEndValues) {
		return genericTimeSeriesSumLazy(schedules, startTime, endTime, ignoreGaps, true, mode, addStartEndValues);
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
	 * Similar to {@link #add(List, long, long)}, but here the data points are evaluated when needed
	 * and not copied at construction time.
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static ReadOnlyTimeSeries addLazy(List<ReadOnlyTimeSeries> schedules, long startTime, long endTime) {
		return genericTimeSeriesSumLazy(schedules, startTime, endTime, false, false, null, false);
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
	
	/**
	 * Similar to {@link #addLazy(List, long, long, boolean, InterpolationMode, boolean)},
	 * but here the data points are evaluated when needed and not copied at construction time.
	 * @param schedules
	 * @param startTime
	 * @param endTime
	 * @param ignoreGaps
	 * @param mode
	 * @param addStartEndValues
	 * @return
	 */
	public static ReadOnlyTimeSeries addLazy(List<? extends ReadOnlyTimeSeries> schedules, long startTime, long endTime, 
			boolean ignoreGaps, InterpolationMode mode, boolean addStartEndValues) {
		return genericTimeSeriesSumLazy(schedules, startTime, endTime, ignoreGaps, false, mode, addStartEndValues);
	}

	private static ReadOnlyTimeSeries genericTimeSeriesSumLazy(final List<? extends ReadOnlyTimeSeries> schedules, 
			final long startTime, final long endTime, final boolean ignoreGaps, final boolean doAverage, final InterpolationMode forcedMode,
			final boolean addStartEndValues) throws IllegalArgumentException {
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
		final InterpolationMode forcedMode1 = forcedMode;
		final IteratorSupplier supplier = new IteratorSupplier() {
			
			@Override
			public Iterator<SampledValue> get(long startTime, long endTime) {
				final List<InterpolationMode> modes = (forcedMode != null ? null : new ArrayList<InterpolationMode>());
				final Map<Integer,SampledValue> lowerBoundary = getBoundaryPoints(schedules, startTime, forcedMode);
				final Map<Integer,SampledValue> upperBoundary = getBoundaryPoints(schedules, endTime, forcedMode);
				final List<Iterator<SampledValue>> iterators = new ArrayList<>(schedules.size());
				for (ReadOnlyTimeSeries r : schedules) { 
					iterators.add(r.iterator(startTime, endTime));
					if (modes != null) {
						InterpolationMode m = r.getInterpolationMode();
						if (m == null || m == InterpolationMode.NONE)
							m = InterpolationMode.LINEAR;
						modes.add(m);
					}
				}
				final MultiTimeSeriesIteratorBuilder iteratorBuilder = MultiTimeSeriesIteratorBuilder.newBuilder(iterators);
				if (addStartEndValues) {
					iteratorBuilder
						.setLowerBoundaryValues(lowerBoundary)
						.setUpperBoundaryValues(upperBoundary);
				}
				if (forcedMode != null)
					iteratorBuilder.setGlobalInterpolationMode(forcedMode);
				else
					iteratorBuilder.setIndividualInterpolationModes(modes);
				final ReductionIterator redIt = ReductionIteratorBuilder.newBuilder(iteratorBuilder.build(), doAverage ? AVERAGE : SUM)
					.setIgnoreGaps(ignoreGaps)
					.setGlobalMode(forcedMode1)
					.build();
				return redIt;
			}
		};
		return new IteratorTimeSeries(supplier, targetMode);
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
