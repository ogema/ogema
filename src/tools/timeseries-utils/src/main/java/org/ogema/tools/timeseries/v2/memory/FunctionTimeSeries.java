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
package org.ogema.tools.timeseries.v2.memory;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeriesHorizon;

/**
 * A timeseries modeled on a {@link Function}. This time series provides values
 * in regular time steps, specified in terms of a {@link TemporalAmount}, e.g. 
 * one value every X years ({@link Period#ofYears(int)} or every X minutes 
 * (@link {@link Duration#ofMinutes(long)}). If the interval is date-based (days, weeks, months, years, ...), as 
 * opposed to time-based (seconds, minutes, hours, ...), then a time zone must be specified.
 * 
 * @param <T> value type
 */
public class FunctionTimeSeries<T extends Value> implements ReadOnlyTimeSeries, TimeSeriesHorizon {

	public static final Function<Long, Quality> DEFAULT_QUALITY = t -> Quality.GOOD;
	private final Function<Long, T> function;
	private final Function<Long, Quality> qualityFunction;
	private final long intervalMultiplier;
	private final TemporalUnit intervalUnit;
	private final long estimatedDuration;
	
	private final ZoneId zone; // may be null 
	
	private final long baseTime;
	private final long start;
	private final long end;
	
	/**
	 * Like {@link #FunctionTimeSeries(Function, Function, long, long, long, TemporalAmount, ZoneId)}, but with a default quality function
	 * which always returns {@link Quality#GOOD}.
	 * @param function
	 * 		maps a time stamp (in millis since epoch) to a value
	 * @param start
	 * 		start of the time domain this function is defined on
	 * @param end
	 * 		end of the time domain this function is defined on
	 * @param baseTime
	 * 		time at which the intervals start (i.e. there is a point defined at baseTime, another one at baseTime + interval, etc., 
	 * 		if these points lie within the interval defined by start and end).
	 * @param interval
	 * 		Time step between adjacent points. If this is date-based, then it must declare a single temporal unit.
	 * @param zone
	 * 		may be null, in which case interval must not be date-based
	 * @throws IllegalArgumentException
	 * 		if start > end or the interval is date based and consists of more than one unit
	 */
	public FunctionTimeSeries(Function<Long, T> function, long start, long end,	long baseTime, TemporalAmount interval, final ZoneId zone) {
		this(function, DEFAULT_QUALITY, start, end, baseTime, interval, zone);
	}
	
	/**
	 * 
	 * @param function
	 * 		maps a time stamp (in millis since epoch) to a value
	 * @param qualityFunction
	 * 		maps a time stamp (in millis since epoch) to a quality
	 * @param start
	 * 		start of the time domain this function is defined on
	 * @param end
	 * 		end of the time domain this function is defined on
	 * @param baseTime
	 * 		time at which the intervals start (i.e. there is a point defined at baseTime, another one at baseTime + interval, etc., 
	 * 		if these points lie within the interval defined by start and end).
	 * @param interval
	 * 		Time step between adjacent points. If this is date-based, then it must declare a single temporal unit.
	 * @param zone
	 * 		may be null, in which case interval must not be date-based
	 * @throws IllegalArgumentException
	 * 		if start > end or the interval is date based and consists of more than one unit
	 * 	
	 */
	public FunctionTimeSeries(Function<Long, T> function, Function<Long, Quality> qualityFunction, long start, long end, long baseTime, TemporalAmount interval, final ZoneId zone) {
		this.function = Objects.requireNonNull(function);
		this.qualityFunction = Objects.requireNonNull(qualityFunction);
		this.start = start;
		this.end = end;
		if (start > end)
			throw new IllegalArgumentException("Start &gt; end: " + start + ", " + end);
		this.zone = zone;
		Objects.requireNonNull(interval);
		final boolean isDateBased = interval.getUnits().get(0).isDateBased();
		if (isDateBased && interval.getUnits().stream().filter(u -> interval.get(u) != 0).count() != 1)
			throw new IllegalArgumentException("Invalid interval provided. Must consist of exactly one temporal unit, got " + interval.getUnits());
		this.intervalUnit = isDateBased ? interval.getUnits().stream().filter(u -> interval.get(u) != 0).findAny().get() : ChronoUnit.MILLIS;
		this.intervalMultiplier = isDateBased ? interval.get(intervalUnit) : 
			interval.getUnits().stream().mapToLong(u -> u.getDuration().multipliedBy(interval.get(u)).toMillis()).sum();
		this.estimatedDuration = intervalUnit.getDuration().toMillis() * intervalMultiplier;
		this.baseTime = baseTime;
	}
	
	private final long step(final long t, final long nrSteps) {
		if (zone == null)
			return Instant.ofEpochMilli(t).plus(intervalMultiplier * nrSteps, intervalUnit).toEpochMilli();
		else
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), zone).plus(intervalMultiplier * nrSteps, intervalUnit).toInstant().toEpochMilli();
	}
	
	private final long stepBack(final long t, final long nrSteps) {
		if (zone == null)
			return Instant.ofEpochMilli(t).minus(intervalMultiplier * nrSteps, intervalUnit).toEpochMilli();
		else
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), zone).minus(intervalMultiplier * nrSteps, intervalUnit).toInstant().toEpochMilli();
	}

	@Override
	public SampledValue getValue(long time) {
		if (time < start || time > end)
			return null;
		return getValueInternal(time);
	}
	
	private final SampledValue getValueInternal(final long time) {
		return new SampledValue(function.apply(time), time, qualityFunction.apply(time));
	}

	@Override
	public SampledValue getNextValue(long time) {
		if (time > end)
			return null;
		if (time < start)
			time = start;
		final long diff = time - baseTime;
		final long estimatedNrSteps = diff / estimatedDuration;
		long tStart = step(baseTime, estimatedNrSteps);
		long previous = stepBack(tStart, 1);
		while (previous >= time) {
			tStart = previous;
			previous = stepBack(tStart, 1);
		}
		while (tStart < time) {
			tStart = step(tStart, 1);
		}
		if (tStart > end || tStart < start)
			return null;
		return getValueInternal(tStart);
	}

	@Override
	public SampledValue getPreviousValue(long time) {
		if (time < start)
			return null;
		if (time > end)
			time = end;
		final long diff = time - baseTime;
		final long estimatedNrSteps = diff / estimatedDuration;
		long tStart = step(baseTime, estimatedNrSteps);
		long next = step(tStart, 1);
		while (next <= time) {
			tStart = next;
			next = step(tStart, 1);
		}
		while (tStart > time) {
			tStart = stepBack(tStart, 1);
		}
		if (tStart > end || tStart < start)
			return null;
		return getValueInternal(tStart);
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return getValues(startTime, Long.MAX_VALUE);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		if (startTime > end || endTime < start)
			return Collections.emptyList();
		final Iterator<SampledValue> it = iterator(startTime, endTime);
		final List<SampledValue> values = new ArrayList<>();
		while (it.hasNext()) {
			values.add(it.next());
		}
		return values;
	}

	/**
	 * Override if required
	 */
	@Override
	public InterpolationMode getInterpolationMode() {
		return InterpolationMode.LINEAR;
	}

	@Override
	public boolean isEmpty() {
		return isEmpty(start, end);
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		return !iterator(startTime, endTime).hasNext();
	}

	/**
	 * returns an estimated result (lower bound)
	 */
	@Override
	public int size() {
		return size(start, end);
	}

	/**
	 * returns an estimated result (lower bound)
	 */
	@Override
	public int size(long startTime, long endTime) {
		startTime = Math.max(startTime, start);
		endTime = Math.min(endTime, end);
		if (startTime > endTime)
			return 0;
		long sz = (endTime-startTime) / estimatedDuration;
		if (sz == 0) { // in this case we better provide an exact result
			final SampledValue next = getNextValue(startTime);
			if (next != null && next.getTimestamp() <= endTime)
				sz = 1;
		}
		return sz > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sz;
	}

	@Override
	public Iterator<SampledValue> iterator() {
		return iterator(start, end);
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return new FunctionIterator<T>(Math.max(startTime, start), Math.min(endTime, end));
	}
	
	/**
	 * Default: provide a time horizon containing 30 points. Override if required.
	 * @param t
	 * @return
	 */
	@Override
	public long getProposedHorizon(long t) {
		return step(t, 30);
	}

	@Override
	public Long getTimeOfLatestEntry() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class FunctionIterator<S extends Value> implements Iterator<SampledValue> {
		
		private final long endTime;
		// state
		private long tNext;
		
		public FunctionIterator(long startTime, long endTime) {
			this.endTime = endTime;
			final long diff = startTime - baseTime;
			final long estimatedNrSteps = diff / estimatedDuration;
			long tStart = step(baseTime, estimatedNrSteps);
			long previous = stepBack(tStart, 1);
			while (previous >= startTime) {
				tStart = previous;
				previous = stepBack(tStart, 1);
			}
			while (tStart < startTime) {
				tStart = step(tStart, 1);
			}
			this.tNext = tStart;
		}

		@Override
		public boolean hasNext() {
			return tNext <= endTime;
		}
		
		@Override
		public SampledValue next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final long t = tNext;
			tNext = step(tNext, 1);
			return getValueInternal(t);
		}
		
	}
	
}
