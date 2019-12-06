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
package org.ogema.tools.timeseries.v2.tools;

import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.IteratorTimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ogema.tools.timeseries.v2.iterator.api.PeriodicIterator;
import org.ogema.tools.timeseries.v2.iterator.api.SampledValueDataPoint;
import org.ogema.tools.timeseries.v2.iterator.api.TransformationIterator;
import org.ogema.tools.timeseries.v2.iterator.api.TransformationMultiIterator;
import org.ogema.tools.timeseries.v2.iterator.api.IteratorTimeSeries.IteratorSupplier;
import org.ogema.tools.timeseries.v2.memory.ReadOnlyTreeTimeSeries;

/**
 * This class offers convenience methods for dealing with {@link ReadOnlyTimeSeries time series}.
 * For instance add multiple timeseries, calculate their average, or transform them in another
 * arbitrary way. For each of these methods there is an eager variant (e.g. {@link #addEagerly(List)})
 * which copies the points immediately, and a lazy variant (e.g. {@link #addLazily(List)}) which copies 
 * points only when needed. The former version is to be preferred when one expects to access the 
 * points in the time series multiple times and the number of points is not too large.
 * <br>
 * There are methods to adapt the sampling rate of a time series (more precisely an iterator over 
 * {@link SampledValue}s), see {@link #downsample(Iterator, long, long, InterpolationMode)} and
 * {@link #downsample(Iterator, long, long, TemporalAmount, ZoneId, InterpolationMode)} and to integrate
 * over a time series.
 */
public class TimeSeriesUtils {
	
	// no need to construct this
	private TimeSeriesUtils() {}
	
	/**
	 * @param timeseries
	 * @return
	 * 		integral, with time measured in ms
	 */
	public static double integrate(ReadOnlyTimeSeries timeseries) {
		return integrate(timeseries.iterator(), timeseries.getInterpolationMode());
	}
	
	/**
	 * @param timeseries
	 * @param start
	 * @param end
	 * @return
	 * 		integral, with time measured in ms
	 */
	public static double integrate(ReadOnlyTimeSeries timeseries, long start, long end) {
		final SampledValue lowerBoundary = timeseries.isEmpty(start, start) ? timeseries.getValue(start) : null;
		final SampledValue upperBoundary = timeseries.isEmpty(end, end) ? timeseries.getValue(end) : null;
		return integrate(timeseries.iterator(start, end), timeseries.getInterpolationMode(), 
					lowerBoundary, upperBoundary);
	}
	
	/**
	 * @param iterator
	 * @param mode
	 * @return
	 * 		integral, with time measured in ms
	 */
	public static double integrate(Iterator<SampledValue> iterator, InterpolationMode mode) {
		Objects.requireNonNull(mode);
		final MultiTimeSeriesIterator multiI = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(iterator))
				.setGlobalInterpolationMode(mode)
				.doIntegrate(true)
				.build();
		SampledValue last = null;
		while (multiI.hasNext()) {
			final SampledValue svdp = multiI.next().getElement(0);
			if (svdp.getQuality() == Quality.GOOD)
				last = svdp;
		}
		return last == null ? 0 : last.getValue().getDoubleValue();
	}
	
	/**
	 * 
	 * @param iterator
	 * @param mode
	 * @param lowerBoundary
	 * @param upperBoundary
	 * @return
	 * 		integral, with time measured in ms
	 */
	public static double integrate(Iterator<SampledValue> iterator, InterpolationMode mode, 
				SampledValue lowerBoundary, SampledValue upperBoundary) {
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder.newBuilder(Collections.singletonList(iterator))
				.setGlobalInterpolationMode(mode)
				.doIntegrate(true);
		if (lowerBoundary != null)
			builder.setLowerBoundaryValues(Collections.singletonMap(0, lowerBoundary));
		if (upperBoundary != null)
			builder.setUpperBoundaryValues(Collections.singletonMap(0, upperBoundary));
		final MultiTimeSeriesIterator multiI = builder.build();
		SampledValue last = null;
		while (multiI.hasNext()) {
			final SampledValue svdp = multiI.next().getElement(0);
			if (svdp.getQuality() == Quality.GOOD)
				last = svdp;
		}
		return last == null ? 0 : last.getValue().getDoubleValue();
	}

	/**
	 * Returns an average value for the time series on the specified interval; depending on the interpolation mode,
	 * this is either calculated as an integral divided by the length of the interval, or 
	 * simply as the arithmetic average of all points in the interval (if interpolation mode is NONE). <br>
	 * 
	 * If the interval does not contain any values (even by interpolation), then <code>Double.NaN</code> is returned.
	 * @param schedule
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static double getAverage(ReadOnlyTimeSeries schedule, long startTime, long endTime) {
		if (endTime == startTime) {
			SampledValue sv = schedule.getValue(startTime);
			if (sv == null || sv.getQuality() == Quality.BAD)
				return Double.NaN;
			else 
				return sv.getValue().getDoubleValue();
		}
		else if (startTime > endTime)
			return getAverage(schedule, endTime, startTime);
		if (schedule.getInterpolationMode() == InterpolationMode.NONE) {
			int count = 0;
			double val = 0;
			final Iterator<SampledValue> it = schedule.iterator(startTime, endTime);
			SampledValue sv;
			while (it.hasNext()) {
				sv = it.next();
				if (sv.getQuality() != Quality.BAD) {
					count++;
					val += sv.getValue().getDoubleValue();
				}
			}
			if (count == 0)
				return Double.NaN;
			else 
				return val / count; 
		}
		return integrate(schedule, startTime, endTime) / (endTime - startTime);
	}
	
	/**
	 * Like {@link ReadOnlyTimeSeries#getValues(long, long)}, except that the resulting list is guaranteed to contain 
	 * data points for the two boundary points <tt>startTime</tt> and <tt>endTime</tt>. If the time series is not defined at the boundaries,
	 * bad quality values are returned there.
	 * @param schedule
	 * @param startTime
	 * @param endTime
	 * @return
	 * 		a list with at least two entries (if <tt>endTime &gt; startTime</tt>) for the two boundary points
	 * @throws IllegalArgumentException 
	 * 		if <code>endTime &lt; startTime</code>
	 */
	public static List<SampledValue> getValuesWithBoundaries(ReadOnlyTimeSeries schedule, long startTime, long endTime) throws IllegalArgumentException {
		Objects.requireNonNull(schedule);
		if (startTime > endTime)
			throw new IllegalArgumentException("Start time smaller than end time: start time: " + startTime + ", end time: " + endTime);
		if (startTime == endTime) {
			SampledValue val = getValueSafe(schedule, startTime);
			return Collections.singletonList(val);
		}
		final List<SampledValue> values = schedule.getValues(startTime, endTime);
		if (values.isEmpty() || values.get(0).getTimestamp() > startTime) {
			values.add(getValueSafe(schedule, startTime));
			Collections.sort(values);
		}
		if (values.isEmpty() || values.get(values.size()-1).getTimestamp() < endTime) 
			values.add(getValueSafe(schedule, endTime));
		return values;
	}
	
	/**
	 * Like {@link ReadOnlyTimeSeries#getValue(long)}, but never returns null.
	 * If the schedule is not defined at <tt>time</tt>, a bad quality value is returned.
	 * @param schedule
	 * @param time
	 * @return
	 */
	public static SampledValue getValueSafe(ReadOnlyTimeSeries schedule, long time) {
		SampledValue val = schedule.getValue(time);
		if (val == null)
			val = new SampledValue(DoubleValue.NAN, time, Quality.BAD);
		return val;
	}
	
	/**
	 * Get the time series value at a certain point in time, as if it had interpolation mode <tt>mode</tt>
	 * @param timeSeries
	 * @param time
	 * @param mode
	 * @return
	 */
	public static SampledValue getValueForInterpolationMode(ReadOnlyTimeSeries timeSeries, long time, InterpolationMode mode) {
		if (mode == null || mode == timeSeries.getInterpolationMode())
			return timeSeries.getValue(time);
		if (!isInScheduleRange(timeSeries, time, mode)) 
			return null;
		switch (mode) {
		case NONE:
			List<SampledValue> values = timeSeries.getValues(time, time+1);
			if (values.isEmpty())
				return new SampledValue(DoubleValue.NAN, time, Quality.BAD);
			return values.get(0);
		case STEPS:
			SampledValue previous = timeSeries.getPreviousValue(time);
			return new SampledValue(new DoubleValue(previous.getValue().getDoubleValue()), time, previous.getQuality());
		case NEAREST:
			previous = timeSeries.getPreviousValue(time);
			SampledValue next = timeSeries.getNextValue(time);
			if (previous == null)
				return next;
			if (next == null)
				return previous;
			long d1 = time-previous.getTimestamp();
			long d2 = next.getTimestamp()-time;
			SampledValue toBeCopied = (d1 <= d2 ? previous: next);
			return new SampledValue(new DoubleValue(toBeCopied.getValue().getDoubleValue()), time, toBeCopied.getQuality());
		case LINEAR:
			previous = timeSeries.getPreviousValue(time);
			next = timeSeries.getNextValue(time);
			if (previous == null || next  == null)
				return null;
			long t0 = time - previous.getTimestamp();
			if (t0 == 0)
				return previous;
			long t1 = next.getTimestamp() - time;
			if (t1 == 0)
				return next;
			final double diff = next.getValue().getDoubleValue()-previous.getValue().getDoubleValue();
			Quality qual = (previous.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD);
			return new SampledValue(new DoubleValue(previous.getValue().getDoubleValue() + diff * t0 / (t0 + t1)), time, qual);
		default: 
			throw new RuntimeException();
		}
	}
	
	/**
	 * Check whether a point in time would be in the time series range if the latter had interpolation mode <tt>mode</tt>
	 * @param timeSeries
	 * @param time
	 * @param mode
	 * @return
	 */
	public static boolean isInScheduleRange(ReadOnlyTimeSeries timeSeries, long time, InterpolationMode mode) {
		if (timeSeries.isEmpty())
			return false;
		if (mode == null)
			mode = InterpolationMode.NONE;
		switch (mode) {
		case NEAREST:
			return true;
		case STEPS:
			return time >= timeSeries.getNextValue(Long.MIN_VALUE).getTimestamp();
		case LINEAR:
		case NONE:
			return time >= timeSeries.getNextValue(Long.MIN_VALUE).getTimestamp()
					&& time <= timeSeries.getPreviousValue(Long.MAX_VALUE).getTimestamp();
		default:
			throw new RuntimeException();
		}
	}
    
    /**
     * See {@link #interpolate(SampledValue, SampledValue, long, InterpolationMode, boolean)}.
     * The default value <tt>badQualityForUndefined = false</tt> is used here.
     * @param previous
     * @param next
     * @param t
     * @param mode
     * @return
     */
    public static SampledValue interpolate(SampledValue previous, SampledValue next, long t, InterpolationMode mode) {
    	return interpolate(previous, next, t, mode, false);
    }
    
    /**
     * Interpolate between two {@link SampledValue}s, based on the provided {@link InterpolationMode}.
     * @param previous
     * 		may be null
     * @param next
     * 		may be null
     * @param t
     * 		the timestamp for which the interpolated value is requested
     * @param mode
     * 		the interpolation mode
     * @param badQualityForUndefined
     * 		if true, a sampled value with bad quality is returned in case the requested value
     * 		is undefined (e.g. one of the two sampled values is null, but an interpolation is
     * 		required). If the argument is false, null is returned in this case.
     * @return
     */
    public static SampledValue interpolate(final SampledValue previous, final SampledValue next, final long t, 
    			final InterpolationMode mode, final boolean badQualityForUndefined) {
    	if (previous == null && next == null)
    		return badQualityForUndefined ? new SampledValue(DoubleValue.NAN, t, Quality.BAD) : null;
    	if (previous != null && previous.getTimestamp() == t)
    		return previous;
    	if (next != null && next.getTimestamp() == t)
    		return next;
    	switch (mode) {
		case STEPS:
			if (previous == null)
				return badQualityForUndefined ? new SampledValue(DoubleValue.NAN, t, Quality.BAD) : null;
			return new SampledValue(previous.getValue(), t, previous.getQuality());
		case LINEAR:
			if (previous == null || next == null)
				return badQualityForUndefined ? new SampledValue(DoubleValue.NAN, t, Quality.BAD) : null;
			final double p = previous.getValue().getDoubleValue();
			final double n = next.getValue().getDoubleValue();
			final long tp = previous.getTimestamp();
			final long tn = next.getTimestamp();
			double newV = p + (n-p)*(t-tp)/(tn-tp);
			return new SampledValue(new DoubleValue(newV), t, 
				previous.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD);
		case NEAREST:
			if (previous == null && next == null)
				return badQualityForUndefined ? new SampledValue(DoubleValue.NAN, t, Quality.BAD) : null;
			final Long tp2 = (previous != null ? previous.getTimestamp() : null);
			final Long tn2 = (next != null ? next.getTimestamp() : null);
			final SampledValue sv = (tp2 == null ? next : tn2 == null ? previous : (t-tp2)<=(tn2-t) ? previous : next);
			return new SampledValue(sv.getValue(), t, sv.getQuality());
		default: // NONE and null
			return badQualityForUndefined ? new SampledValue(DoubleValue.NAN, t, Quality.BAD) : null;
		}
    }
    
    
    /**
     * Find maximum value in time series
     * 
     * @param timeSeries
     * @param startTime
     * @param endTime
     * @return maximum value or null if no value is found
     */
    public static SampledValue getMax(ReadOnlyTimeSeries timeSeries, long startTime, long endTime) {
    	double max = -Float.MAX_VALUE;
    	Long t = null; 
    	final Iterator<SampledValue> it = timeSeries.iterator(startTime, endTime);
    	SampledValue val;
    	while (it.hasNext()) {
    		val = it.next();
    		if (val.getQuality() == Quality.BAD) continue;
    		double fval = val.getValue().getDoubleValue();
    		if (fval > max) {
    			max = fval;
    			t = val.getTimestamp();
    		}
    	}
    	return t != null ? new SampledValue(new DoubleValue(max), t, Quality.GOOD) : null;
    }
    
    /**
     * Find the minimum value in time series
     * 
     * @param timeSeries
     * @param startTime
     * @param endTime
     * @return maximum value or null if no value is found
     */
    public static SampledValue getMin(ReadOnlyTimeSeries timeSeries, long startTime, long endTime) {
    	double min = Float.MAX_VALUE;
    	Long t = null; 
    	final Iterator<SampledValue> it = timeSeries.iterator(startTime, endTime);
    	SampledValue val;
    	while (it.hasNext()) {
    		val = it.next();
    		if (val.getQuality() == Quality.BAD) 
    			continue;
    		double fval = val.getValue().getDoubleValue();
    		if (fval < min) { 
    			min = fval;
    			t = val.getTimestamp();
    		}
    	}
    	return t != null ? new SampledValue(new DoubleValue(min), t, Quality.GOOD) : null;
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

	public static ReadOnlyTimeSeries transformLazily(final ReadOnlyTimeSeries timeSeries, 
			final Function<Value, Value> transformation,
			final long startTime, final long endTime) {
		final IteratorSupplier supplier = (start, end) -> new TransformationIterator(timeSeries.iterator(start, end), transformation);
		return new IteratorTimeSeries(supplier, timeSeries.getInterpolationMode());
	}
 
	public static ReadOnlyTimeSeries transformEagerly(final ReadOnlyTimeSeries timeSeries, 
			final Function<Value, Value> transformation,
			final long startTime, final long endTime) {
		return transformEagerly(
				MultiTimeSeriesIteratorBuilder.newBuilder(Arrays.asList(timeSeries.iterator(startTime, endTime))),
				timeSeries.getInterpolationMode(), values -> transformation.apply(values.get(0)));
	}
	
	/**
	 * This can be used for instance to add timeseries.
	 * @param builder
	 * @param mode
	 * 		may be null
	 * @param transformation
	 * @return
	 */
	public static ReadOnlyTimeSeries transformLazily(final MultiTimeSeriesIteratorBuilder builder, final InterpolationMode mode, 
			final Function<List<Value>, Value> transformation) {
		return transformLazily(builder, mode, transformation, TransformationMultiIterator.DEFAULT_QUALITY_FUNCTION, null);
	}
	
	public static ReadOnlyTimeSeries transformLazily(final MultiTimeSeriesIteratorBuilder builder, final InterpolationMode mode, 
			final Function<List<Value>, Value> transformation, final Function<List<Quality>, Quality> qualityFunction,
			final List<SampledValue> additionalPoints) {
		final IteratorSupplier supplier = (start, end) -> new TransformationMultiIterator(builder.build(), transformation, qualityFunction); 
		final ReadOnlyTimeSeries ts = new IteratorTimeSeries(supplier, mode);
		if (additionalPoints == null || additionalPoints.isEmpty())
			return ts;
		final long first = additionalPoints.iterator().next().getTimestamp();
		final IteratorSupplier supp = (start, end) -> {
			final Iterator<SampledValue> base = ts.iterator(start, end);
			if (end < first)
				return base;
			return new Iterator<SampledValue>() {
				
				int i=0;

				@Override
				public boolean hasNext() {
					if (base.hasNext())
						return true;
					int k=i;
					while (k < additionalPoints.size()) {
						final SampledValue sv = additionalPoints.get(k++);
						if (sv.getTimestamp() < start)
							continue;
						else if (sv.getTimestamp() <= end)
							return true;
						else 
							return false;
					}
					return false;
				}

				@Override
				public SampledValue next() {
					if (base.hasNext())
						return base.next();
					while (i < additionalPoints.size()) {
						final SampledValue sv = additionalPoints.get(i++);
						if (sv.getTimestamp() < start)
							continue;
						if (sv.getTimestamp() <= end)
							return sv;
					}
					throw new NoSuchElementException();
				}
				
				
			};
		};
		return new IteratorTimeSeries(supp, mode);
	}
	
	public static ReadOnlyTimeSeries transformEagerly(final MultiTimeSeriesIteratorBuilder builder, final InterpolationMode mode, 
			final Function<List<Value>, Value> transformation) {
		return transformEagerly(builder, mode, transformation, TransformationMultiIterator.DEFAULT_QUALITY_FUNCTION, null);
	}
	
	/**
	 * @param builder
	 * @param mode
	 * @param transformation
	 * @param qualityFunction
	 * @param additionalPoints
	 * 		may be null, will not be transformed
	 * @return
	 */
	public static ReadOnlyTimeSeries transformEagerly(final MultiTimeSeriesIteratorBuilder builder, final InterpolationMode mode, 
			final Function<List<Value>, Value> transformation, final Function<List<Quality>, Quality> qualityFunction,
			final List<SampledValue> additionalPoints) {
		final NavigableSet<SampledValue> values = new TreeSet<>();
		final Iterator<SampledValue> it = new TransformationMultiIterator(builder.build(), transformation, qualityFunction);
		while (it.hasNext()) {
			values.add(it.next());
		}
		if (additionalPoints != null)
			values.addAll(additionalPoints);
		return new ReadOnlyTreeTimeSeries(values, mode);
	}
	
	/**
	 * See {@link #addLazily(List, InterpolationMode, long, long, boolean, boolean, boolean)}. Default values are 
	 * <ul>
	 *  <li>targetMode: null
	 * 	<li>start: Long.MIN_VALUE
	 *  <li>end: Long.MAX_VALUE
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul>
	 * @param ts
	 * @return
	 */
	public static ReadOnlyTimeSeries addLazily(final List<ReadOnlyTimeSeries> ts) {
		return addLazily(ts, null, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	/**
	 * See {@link #addLazily(List, InterpolationMode, long, long, boolean, boolean, boolean)}. Default values are 
	 * <ul>
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul>
	 * @param ts
	 * @param targetMode
	 * @param start
	 * @param end
	 * @return
	 */
	public static ReadOnlyTimeSeries addLazily(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode, final long start, final long end) {
		return addLazily(ts, targetMode, start, end, true, false, false);
	}
	
	/**
	 * Add time series lazily, i.e. add points only on demand.
	 * @param ts
	 * 		the set of timeseries to be added
	 * @param targetMode
	 * 		interpolation mode for the target time series
	 * @param start
	 * 		start time in millis 
	 * @param end
	 * 		end time in millis
	 * @param addBoundaryPoints
	 * 		if true then boundary points at the specified start and end times will be added, unless these are infinite (i.e. equal to Long.MIN_VALUE and
	 *      Long.MAX_VALUE respectively)
	 * @param ignoreGaps
	 * 		if true then a single good quality value will be enough to get a good quality value at a certain timestep, otherwise all need to be good
	 * @param addEndMarker
	 * 		if true adds a point of bad quality after the specified time interval to indicate the end of the timeseries
	 * @return
	 */
	public static ReadOnlyTimeSeries addLazily(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode,
			final long start, final long end, final boolean addBoundaryPoints, final boolean ignoreGaps, final boolean addEndMarker) {
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder
				.newBuilder(ts.stream().map(t -> t.iterator(start, end)).collect(Collectors.toList()))
				.setIndividualInterpolationModes(ts.stream().map(TimeSeriesUtils::getInterpolationModeSafe).collect(Collectors.toList()));
		if (addBoundaryPoints) {
			if (start != Long.MIN_VALUE)
				builder.setLowerBoundaryValues(getBoundaryValues(ts, start));
			if (end != Long.MAX_VALUE)
				builder.setUpperBoundaryValues(getBoundaryValues(ts, end));
		}
		final Function<List<Quality>, Quality> qualityFunction = ignoreGaps ? TransformationMultiIterator.INGORANT_QUALITY_FUNCTION : TransformationMultiIterator.DEFAULT_QUALITY_FUNCTION;
		final List<SampledValue> endMarker = addEndMarker && end != Long.MAX_VALUE ? 
				Collections.singletonList(new SampledValue(DoubleValue.ZERO, end + 1, Quality.BAD)) : null;
		return transformLazily(builder, targetMode != null ? targetMode : deduceMode(ts), 
				values -> new DoubleValue(values.stream().filter(Objects::nonNull).mapToDouble(Value::getDoubleValue).sum()),
				qualityFunction, endMarker);
	}

	/**
	 * See {@link #addEagerly(List, InterpolationMode, long, long, boolean, boolean, boolean)}. Default values are 
	 * <ul>
	 *  <li>targetMode: null
	 * 	<li>start: Long.MIN_VALUE
	 *  <li>end: Long.MAX_VALUE
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul>
	 * @param ts
	 * @return
	 */
	public static ReadOnlyTimeSeries addEagerly(final List<ReadOnlyTimeSeries> ts) {
		return addEagerly(ts, null, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	/**
	 * See {@link #addEagerly(List, InterpolationMode, long, long, boolean, boolean, boolean)}. Default values are 
	 * <ul>
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul>
	 * @param ts
	 * @param targetMode
	 * @param start
	 * @param end
	 * @return
	 */
	public static ReadOnlyTimeSeries addEagerly(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode, final long start, final long end) {
		return addEagerly(ts, targetMode, start, end, true, false, false);
	}
	
	/**
	 * Add time series eagerly, i.e. add points immediately.
	 * @param ts
	 * 		the set of timeseries to be added
	 * @param targetMode
	 * 		interpolation mode for the target time series. May be null, in which case the mode is deduced from the input time series
	 * @param start
	 * 		start time in millis 
	 * @param end
	 * 		end time in millis
	 * @param addBoundaryPoints
	 * 		if true then boundary points at the specified start and end times will be added, unless these are infinite (i.e. equal to Long.MIN_VALUE and
	 *      Long.MAX_VALUE respectively)
	 * @param ignoreGaps
	 * 		if true then a single good quality value will be enough to get a good quality value at a certain timestep, otherwise all need to be good
	 * @param addEndMarker
	 * 		if true adds a point of bad quality after the specified time interval to indicate the end of the timeseries
	 * @return
	 */
	public static ReadOnlyTimeSeries addEagerly(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode, 
			final long start, final long end, final boolean addBoundaryPoints, final boolean ignoreGaps, final boolean addEndMarker) {
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder
				.newBuilder(ts.stream().map(t -> t.iterator(start, end)).collect(Collectors.toList()))
				.setIndividualInterpolationModes(ts.stream().map(TimeSeriesUtils::getInterpolationModeSafe).collect(Collectors.toList()));
		if (addBoundaryPoints) {
			if (start != Long.MIN_VALUE)
				builder.setLowerBoundaryValues(getBoundaryValues(ts, start));
			if (end != Long.MAX_VALUE)
				builder.setUpperBoundaryValues(getBoundaryValues(ts, end));
		}
		final Function<List<Quality>, Quality> qualityFunction = ignoreGaps ? TransformationMultiIterator.INGORANT_QUALITY_FUNCTION : TransformationMultiIterator.DEFAULT_QUALITY_FUNCTION;
		final List<SampledValue> endMarker = addEndMarker && end != Long.MAX_VALUE ? 
				Collections.singletonList(new SampledValue(DoubleValue.ZERO, end + 1, Quality.BAD)) : null;
		return transformEagerly(builder, targetMode != null ? targetMode : deduceMode(ts), 
				values -> new DoubleValue(values.stream().filter(Objects::nonNull).mapToDouble(Value::getDoubleValue).sum()),
				qualityFunction, endMarker);
	}
	
	/**
	 * See {@link #averageLazily(List, InterpolationMode, long, long, boolean, boolean, boolean). Default values are 
	 * <ul>
	 *  <li>targetMode: null
	 * 	<li>start: Long.MIN_VALUE
	 *  <li>end: Long.MAX_VALUE
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul> 
	 * @param ts
	 * @return
	 */
	public static ReadOnlyTimeSeries averageLazily(final List<ReadOnlyTimeSeries> ts) {
		return averageLazily(ts, null, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	/**
	 * See {@link #averageLazily(List, InterpolationMode, long, long, boolean, boolean, boolean). Default values are 
	 * <ul>
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul> 
	 * @param ts
	 * @param targetMode
	 * @param start
	 * @param end
	 * @return
	 */
	public static ReadOnlyTimeSeries averageLazily(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode, final long start, final long end) {
		return averageLazily(ts, targetMode, start, end, true, false, false);
	}
	
	/**
	 * Calculate the average time series lazily, i.e. generate points only on demand.
	 * @param ts
	 * 		the set of timeseries to be averaged
	 * @param targetMode
	 * 		interpolation mode for the target time series. May be null, in which case the mode is deduced from the input time series
	 * @param start
	 * 		start time in millis 
	 * @param end
	 * 		end time in millis
	 * @param addBoundaryPoints
	 * 		if true then boundary points at the specified start and end times will be added, unless these are infinite (i.e. equal to Long.MIN_VALUE and
	 *      Long.MAX_VALUE respectively)
	 * @param ignoreGaps
	 * 		if true then a single good quality value will be enough to get a good quality value at a certain timestep, otherwise all need to be good
	 * @param addEndMarker
	 * 		if true adds a point of bad quality after the specified time interval to indicate the end of the timeseries
	 * @return
	 */
	public static ReadOnlyTimeSeries averageLazily(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode,
			final long start, final long end, final boolean addBoundaryPoints, final boolean ignoreGaps, final boolean addEndMarker) {
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder.newBuilder(ts.stream().map(t -> t.iterator(start, end)).collect(Collectors.toList()))
				.setIndividualInterpolationModes(ts.stream().map(TimeSeriesUtils::getInterpolationModeSafe).collect(Collectors.toList()));
		if (addBoundaryPoints) {
			if (start != Long.MIN_VALUE)
				builder.setLowerBoundaryValues(getBoundaryValues(ts, start));
			if (end != Long.MAX_VALUE)
				builder.setUpperBoundaryValues(getBoundaryValues(ts, end));
		}
		final Function<List<Quality>, Quality> qualityFunction = ignoreGaps ? TransformationMultiIterator.INGORANT_QUALITY_FUNCTION : TransformationMultiIterator.DEFAULT_QUALITY_FUNCTION;
		final List<SampledValue> endMarker = addEndMarker && end != Long.MAX_VALUE ? 
				Collections.singletonList(new SampledValue(DoubleValue.ZERO, end + 1, Quality.BAD)) : null;
		return transformLazily(builder, targetMode != null ? targetMode : deduceMode(ts), 
				values -> new DoubleValue(values.stream().filter(Objects::nonNull).mapToDouble(Value::getDoubleValue).average().orElse(0)),
				qualityFunction, endMarker);
	}

	/**
	 * See {@link #averageEagerly(List, InterpolationMode, long, long, boolean, boolean, boolean). Default values are 
	 * <ul>
	 *  <li>targetMode: null
	 * 	<li>start: Long.MIN_VALUE
	 *  <li>end: Long.MAX_VALUE
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul> 
	 * @param ts
	 * @return
	 */	
	public static ReadOnlyTimeSeries averageEagerly(final List<ReadOnlyTimeSeries> ts) {
		return averageEagerly(ts, null, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	/**
	 * See {@link #averageEagerly(List, InterpolationMode, long, long, boolean, boolean, boolean). Default values are 
	 * <ul>
	 *  <li>addBoundaryPoints: true
	 *  <li>ignoreGaps: false
	 *  <li>addEndMarker: false
	 * </ul> 
	 * @param ts
	 * @param targetMode
	 * @param start
	 * @param end
	 * @return
	 */
	public static ReadOnlyTimeSeries averageEagerly(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode, final long start, final long end) {
		return averageEagerly(ts, targetMode, start, end, true, false, false);
	}
	
	/**
	 * Calculate the average time series eagerly, i.e. generate points immediately.
	 * @param ts
	 * 		the set of timeseries to be averaged
	 * @param targetMode
	 * 		interpolation mode for the target time series. May be null, in which case the mode is deduced from the input time series
	 * @param start
	 * 		start time in millis 
	 * @param end
	 * 		end time in millis
	 * @param addBoundaryPoints
	 * 		if true then boundary points at the specified start and end times will be added, unless these are infinite (i.e. equal to Long.MIN_VALUE and
	 *      Long.MAX_VALUE respectively)
	 * @param ignoreGaps
	 * 		if true then a single good quality value will be enough to get a good quality value at a certain timestep, otherwise all need to be good
	 * @param addEndMarker
	 * 		if true adds a point of bad quality after the specified time interval to indicate the end of the timeseries
	 * @return
	 */
	public static ReadOnlyTimeSeries averageEagerly(final List<ReadOnlyTimeSeries> ts, final InterpolationMode targetMode, 
			final long start, final long end, final boolean addBoundaryPoints, final boolean ignoreGaps, final boolean addEndMarker) {
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder.newBuilder(ts.stream().map(t -> t.iterator(start, end)).collect(Collectors.toList()))
				.setIndividualInterpolationModes(ts.stream().map(TimeSeriesUtils::getInterpolationModeSafe).collect(Collectors.toList()));
		if (addBoundaryPoints) {
			if (start != Long.MIN_VALUE)
				builder.setLowerBoundaryValues(getBoundaryValues(ts, start));
			if (end != Long.MAX_VALUE)
				builder.setUpperBoundaryValues(getBoundaryValues(ts, end));
		}
		final Function<List<Quality>, Quality> qualityFunction = ignoreGaps ? TransformationMultiIterator.INGORANT_QUALITY_FUNCTION : TransformationMultiIterator.DEFAULT_QUALITY_FUNCTION;
		final List<SampledValue> endMarker = addEndMarker && end != Long.MAX_VALUE ? 
				Collections.singletonList(new SampledValue(DoubleValue.ZERO, end + 1, Quality.BAD)) : null;
		return transformEagerly(builder, targetMode != null ? targetMode : deduceMode(ts), 
				values -> new DoubleValue(values.stream().filter(Objects::nonNull).mapToDouble(Value::getDoubleValue).average().orElse(0)),
				qualityFunction, endMarker);
	}
	
	
	/**
	 * Transform the passed iterator to an iterator at fixed sampling rate
	 * @param it
	 * @param startTime
	 * @param interval
	 * @param mode
	 * @return
	 */
	public static Iterator<SampledValue> downsample(final Iterator<SampledValue> it, final long startTime, final long interval, 
			final InterpolationMode mode) {
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder
				.newBuilder(Collections.singletonList(it))
				.setGlobalInterpolationMode(mode)
				.setStepSize(startTime, interval)
				.build();
		return new Iterator<SampledValue>() {

			@Override
			public boolean hasNext() {
				return multiIt.hasNext();
			}

			@Override
			public SampledValue next() {
				final SampledValueDataPoint svdp = multiIt.next();
				return svdp.getElement(0);
			}
		};
	}
	
	/**
	 * Convert an iterator over SampledValue data points
	 * to an equivalent iterator with equidistant time intervals, 
	 * specified in terms of a {@link TemporalAmount}.
	 * @param it
	 * 		iterator to be converted
	 * @param startTime
	 * @param endTime
	 * @param interval
	 * @param zone
	 * 		may be null if interval is time-based not date-based.
	 * @param mode
	 * @return
	 */
	public static Iterator<SampledValue> downsample(final Iterator<SampledValue> it, final long startTime, final long endTime,
			final TemporalAmount interval, final ZoneId zone, final InterpolationMode mode) {
		if (endTime < startTime)
			return Collections.emptyIterator();
		final PeriodicIterator aux = new PeriodicIterator(Collections.singletonList(DoubleValue.ZERO), startTime, endTime, startTime, interval, zone);
		final MultiTimeSeriesIterator multiIt = MultiTimeSeriesIteratorBuilder
				.newBuilder(Arrays.asList(it, aux))
				.setGlobalInterpolationMode(mode)
				.stepSizeAsInSchedules(new int[] {1})
				.build();
		return new Iterator<SampledValue>() {
			
			private SampledValue next;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (multiIt.hasNext()) {
					final SampledValue sv = multiIt.next().getElement(0);
					if (sv != null) {
						next = sv;
						return true;
					}
				}
				return false;
			}

			@Override
			public SampledValue next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final SampledValue sv = next;
				next = null;
				return sv;
			}
		};
	}
	
	/**
	 * Get a {@link MultiTimeSeriesIterator} for a set of time series over the requested interval. Boundary 
	 * points will be added for all time series that do not have a defined data point at the boundary time stamps.
	 *  
	 * @param timeSeries
	 * @param storedHistoricalData
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IllegalArgumentException if startTime &gt endTime
	 */
	public static MultiTimeSeriesIterator getMultiIterator(List<ReadOnlyTimeSeries> timeSeries, long startTime, long endTime) {
		return getMultiIterator(timeSeries, 0, startTime, endTime);
	}
	
	/**
	 * Like {@link #getMultiIterator(List, long, long)} except that historical data access may be enabled by setting
	 * storedHistoricalData &gt 0.
	 * 
	 * @param timeSeries
	 * @param storedHistoricalData
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IllegalArgumentException if startTime &gt endTime
	 */
	public static MultiTimeSeriesIterator getMultiIterator(List<ReadOnlyTimeSeries> timeSeries, int storedHistoricalData,
			long startTime, long endTime) {
		if (endTime < startTime)
			throw new IllegalArgumentException("startTime > endTime: " + startTime + ", " + endTime);
		final List<Iterator<SampledValue>> iterators = new ArrayList<>();
		final boolean needsLowerBoundary = startTime != Long.MIN_VALUE;
		final boolean needsUpperBoundary = endTime != Long.MAX_VALUE;
		final Map<Integer,SampledValue> lowerBoundaryValues = needsLowerBoundary ? new HashMap<>() : null; 
		final Map<Integer,SampledValue> upperBoundaryValues = needsUpperBoundary ? new HashMap<>() : null;
		for (int n=0;n<timeSeries.size();n++) {
			final ReadOnlyTimeSeries ts = timeSeries.get(n);
			if (needsLowerBoundary && ts.isEmpty(startTime, startTime)) { // sic!
				final SampledValue start = ts.getValue(startTime);
				if (start != null)
					lowerBoundaryValues.put(n, start);
			}
			if (needsUpperBoundary && ts.isEmpty(endTime,endTime)) {
				final SampledValue end = ts.getValue(endTime);
				if (end != null)
					upperBoundaryValues.put(n, end);
			}
			iterators.add(ts.iterator(startTime, endTime));
		}
		final MultiTimeSeriesIteratorBuilder builder = MultiTimeSeriesIteratorBuilder.newBuilder(iterators)
			.setMaxNrHistoricalValues(storedHistoricalData);
		if (lowerBoundaryValues != null && !lowerBoundaryValues.isEmpty())
			builder.setLowerBoundaryValues(lowerBoundaryValues);
		if (upperBoundaryValues != null && !upperBoundaryValues.isEmpty())
			builder.setUpperBoundaryValues(upperBoundaryValues);
		return builder.build();
	}
	
	public static ReadOnlyTimeSeries emptyTimeseries() {
		return EmptyTimeseries.INSTANCE;
	}
	
	public static TimeSeries synchronizedTimeseries(TimeSeries timeSeries) {
		return new SynchronizedTimeseries(timeSeries);
	}
	
	/**
	 * Never returns null
	 * @param ts
	 * @return
	 * @throws NullPointerException
	 */
	public static InterpolationMode getInterpolationModeSafe(final ReadOnlyTimeSeries ts) {
		final InterpolationMode mode = ts.getInterpolationMode();
		return mode == null ? InterpolationMode.NONE : mode;
	}
	
	/**
	 * Get iterator values as a stream
	 * @param values
	 * @return
	 */
	public static Stream<SampledValue> getValuesAsStream(final Iterator<SampledValue> values) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(values, Spliterator.ORDERED), false);
	}
	
	/**
	 * Get timeseries values as a stream
	 * @param ts
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static Stream<SampledValue> getValuesAsStream(final ReadOnlyTimeSeries ts, final long startTime, final long endTime) {
		return getValuesAsStream(ts.iterator(startTime, endTime));
	}
	
	private static InterpolationMode deduceMode(final Collection<ReadOnlyTimeSeries> ts) {
		return deduceMode(ts.stream().map(ReadOnlyTimeSeries::getInterpolationMode));
	}
	
	private static InterpolationMode deduceMode(final Stream<InterpolationMode> modes) {
		return modes.sorted((i1, i2) -> -Integer.compare(i1.getInterpolationMode(), i2.getInterpolationMode()))
			.findFirst()
			.orElse(InterpolationMode.NONE);
	}
	
	private static Map<Integer, SampledValue> getBoundaryValues(final List<ReadOnlyTimeSeries> ts, final long t) {
		final Map<Integer, SampledValue> boundaries = new HashMap<>(ts.size());
		for (int i=0; i<ts.size(); i++) {
			final ReadOnlyTimeSeries el = ts.get(i);
			if (el.isEmpty(t, t)) {
				final SampledValue sv = el.getValue(t);
				if (sv != null)
					boundaries.put(i, sv);
			}
		}
		return boundaries;
	}
	
	
}
