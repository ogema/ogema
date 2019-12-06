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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.TimeSeriesHorizon;
import org.ogema.tools.timeseries.v2.iterator.api.IteratorTimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.PeriodicIterator;
import org.ogema.tools.timeseries.v2.tools.TimeSeriesUtils;

/**
 * A time series that repeatedly provides the same values, at a fixed interval. 
 */
public class PeriodicTimeSeries extends IteratorTimeSeries implements TimeSeriesHorizon {

	private final long intervalMultiplier;
	private final TemporalUnit intervalUnit;
	
	private final ZoneId zone; // may be null 
	private final int nrValues;
	
	/**
	 * @param values
	 * 		a list that must not be changed after creation
	 * @param baseTime
	 * @param mode
	 * @param interval
	 * 		Must consist of exactly one temporal unit
	 * @param zone
	 * 		may be null; in this case the interval must not be date based
	 * @throws IllegalArgumentException if an invalid interval is passed
	 */
	public PeriodicTimeSeries(List<Value> values, long baseTime, InterpolationMode mode, TemporalAmount interval, final ZoneId zone) {
		// we use the same list of values for every iterator without copying them...
		super((start, end) -> new PeriodicIterator(values, start, end, baseTime, interval, zone), mode);
		Objects.requireNonNull(values);
		Objects.requireNonNull(interval);
		final boolean isDateBased = interval.getUnits().get(0).isDateBased();
		if (isDateBased && interval.getUnits().stream().filter(u -> interval.get(u) != 0).count() != 1)
			throw new IllegalArgumentException("Invalid interval provided. Must consist of exactly one temporal unit, got " + interval.getUnits());
		this.intervalUnit = isDateBased ? interval.getUnits().stream().filter(u -> interval.get(u) != 0).findAny().get() : ChronoUnit.MILLIS;
		this.intervalMultiplier = isDateBased ? interval.get(intervalUnit) : 
			interval.getUnits().stream().mapToLong(u -> u.getDuration().multipliedBy(interval.get(u)).toMillis()).sum();
		this.zone = zone;
		this.nrValues = values.size();
	}
	
	@Override
	public SampledValue getValue(long time) {
		final SampledValue previous = getPreviousValue(time);
		if (previous != null && previous.getTimestamp() == time)
			return previous;
		final SampledValue next = getNextValue(time);
		if (next != null && next.getTimestamp() == time) 
			return next;
		if (previous == null && next == null)
			return null;
		return TimeSeriesUtils.interpolate(previous, next, time, getInterpolationMode());
	}
	
	// getNextValue on the other hand already has an efficient impl in the parent class IteratorTimeSeries
	@Override
	public SampledValue getPreviousValue(long time) {
		final Instant iteratorStart;
		if (zone == null) {
			iteratorStart = Instant.ofEpochMilli(time).minus(2 * intervalMultiplier, intervalUnit);
		} else {
			iteratorStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), zone).minus(2 * intervalMultiplier, intervalUnit).toInstant();
		}
		final Iterator<SampledValue> it = iterator(iteratorStart.toEpochMilli(), time);
		if (!it.hasNext())
			return null;
		SampledValue last = it.next();
		if (last.getTimestamp() == time)
			return last;
		if (last.getTimestamp() > time)
			return null;
		while (it.hasNext()) {
			final SampledValue next = it.next();
			if (next.getTimestamp() == time)
				return next;
			if (next.getTimestamp() > time)
				return last;
			last = next;
		}
		return last;
	}
	
	/**
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IllegalArgumentException if the time interval is too large
	 */
	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		if (startTime == Long.MIN_VALUE || endTime == Long.MAX_VALUE)
			throw new IllegalArgumentException("Time interval too large");
		return super.getValues(startTime, endTime);
	}
	
	/**
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IllegalArgumentException if the start time is too small or too large
	 * 		(outside range Instant.MIN - Instant.MAX)
	 */
	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		// the second condition is heuristic, but it only serves to catch the very etchy case
		// that endTime is close to Long.MIN_VALUE too
		if (startTime == Long.MIN_VALUE && endTime > startTime + 100 * intervalMultiplier * intervalUnit.getDuration().toMillis())
			throw new IllegalArgumentException("Time interval too large");
		return super.iterator(startTime, endTime);
	}
	
	@Override
	public long getProposedHorizon(long t) {
		if (zone == null)
			return Instant.ofEpochMilli(t).plus(nrValues * intervalMultiplier, intervalUnit).toEpochMilli();
		else
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), zone).plus(nrValues * intervalMultiplier, intervalUnit).toInstant().toEpochMilli();
	}
	
}
