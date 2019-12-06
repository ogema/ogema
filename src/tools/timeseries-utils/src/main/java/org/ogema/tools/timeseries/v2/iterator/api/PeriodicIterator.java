package org.ogema.tools.timeseries.v2.iterator.api;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * An iterator that repeats a specified set of values over and over again, at a fixed time interval.
 */
public class PeriodicIterator implements Iterator<SampledValue> {
	
	// exactly one of the following two is non-null!
	private final Instant baseInstant;
	private final ZonedDateTime baseZdt;
	private final List<Value> values;
	private final ZoneId zone;
	private boolean empty; // quasi-final
	private final int size;
	private final Instant endTimeInstant;
	private final TemporalAmount interval; // interval between two data points
	private final long intervalMultiplier;
	private final TemporalUnit intervalUnit;
	
	// state
	// exactly one of the following two is non-null!
	private ZonedDateTime nextZdt;
	private Instant nextInstant;
	private int nextIdx;
	
	/**
	 * @param values
	 * @param startTime
	 * @param endTime
	 * @param baseTime
	 * 		One timestamp for the first value in the list; other timestamps will be computed started from this value.
	 * @param duration Must consist of exactly one temporal unit
	 * @param zone
	 * @throws IllegalArgumentException if an invalid duration is passed
	 */
	public PeriodicIterator(List<Value> values, long startTime, long endTime, long baseTime, TemporalAmount duration, ZoneId zone) {
		this.values = Objects.requireNonNull(values);
		this.size = values.size();
		this.interval = Objects.requireNonNull(duration);
		final boolean isDateBased = duration.getUnits().get(0).isDateBased();
		if (isDateBased && duration.getUnits().stream().filter(u -> duration.get(u) != 0).count() != 1)
			throw new IllegalArgumentException("Invalid duration provided. Must consist of exactly one temporal unit, got " + duration.getUnits());
		this.intervalUnit = isDateBased ? duration.getUnits().stream().filter(u -> duration.get(u) != 0).findAny().get() : ChronoUnit.MILLIS;
		this.intervalMultiplier = isDateBased ? duration.get(intervalUnit) : 
			duration.getUnits().stream().mapToLong(u -> u.getDuration().multipliedBy(duration.get(u)).toMillis()).sum();
		this.zone = zone;
		this.empty = endTime < startTime || values.isEmpty();
		this.endTimeInstant = Instant.ofEpochMilli(endTime);
		if (empty) {
			this.baseInstant = null;
			this.baseZdt = null;
			this.nextInstant = null;
		} else if (zone == null) {
			this.baseInstant = getFirstStartTime(baseTime, startTime);
			this.baseZdt = null;
		} else {
			this.baseInstant = null;
			this.baseZdt = ZonedDateTime.ofInstant(getFirstStartTime(baseTime, startTime), zone);
		}
		if (!empty)
			initialAdvance(startTime);
	}
	
	/**
	 * Get first start time corresponding to index 0
	 * @param baseTime
	 * @param startTime
	 * @return
	 */
	private final Instant getFirstStartTime(final long baseTime, final long startTime) {
		final long durationMillis = interval.getUnits().stream()
			.mapToLong(u -> u.getDuration().multipliedBy(interval.get(u)).toMillis())
			.sum()
			* size;
		final long nrIntervals = (startTime - baseTime) / durationMillis;
		if (zone == null) {
			final Instant b = Instant.ofEpochMilli(baseTime);
			final Instant s = Instant.ofEpochMilli(startTime);
			Instant cand = b.plus(nrIntervals * size * intervalMultiplier, intervalUnit);
			while (cand.compareTo(s) < 0)
				cand = cand.plus(size * intervalMultiplier, intervalUnit);
			while (cand.compareTo(s) > 0)
				cand = cand.minus(size * intervalMultiplier, intervalUnit);
			return cand;
		} else {
			final ZonedDateTime b = ZonedDateTime.ofInstant(Instant.ofEpochMilli(baseTime), zone);
			final ZonedDateTime s = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime), zone);
			ZonedDateTime cand = b.plus(nrIntervals * size * intervalMultiplier, intervalUnit);
			while (cand.compareTo(s) < 0)
				cand = cand.plus(size * intervalMultiplier, intervalUnit);
			while (cand.compareTo(s) > 0)
				cand = cand.minus(size * intervalMultiplier, intervalUnit);
			return cand.toInstant();
		}
	}
	
	private final void initialAdvance(final long startTime) {
		if (zone == null) {
			final Instant start = Instant.ofEpochMilli(startTime);
			Instant cand = baseInstant;
			while (cand.compareTo(start) < 0) {
				cand = cand.plus(interval);
				nextIdx = (nextIdx + 1) % size; 
			}
			if (cand.compareTo(endTimeInstant) > 0)
				this.empty = true;
			else
				this.nextInstant = cand;
		} else {
			final ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime), zone);
			ZonedDateTime cand = baseZdt;
			while (cand.compareTo(start) < 0) {
				cand = cand.plus(interval);
				nextIdx = (nextIdx + 1) % size; 
			}
			if (cand.toInstant().compareTo(endTimeInstant) > 0)
				this.empty = true;
			else
				this.nextZdt = cand;
		}
	}
	
	@Override
	public boolean hasNext() {
		if (empty)
			return false;
		try {
			final Instant next = nextInstant != null ? nextInstant : nextZdt.toInstant();
			return next.compareTo(endTimeInstant) <= 0; 
		} catch (DateTimeException | ArithmeticException e) {
			return false;
		}
	}
	
	@Override
	public SampledValue next() {
		if (!hasNext())
			throw new NoSuchElementException();
		final Instant thisInstant = nextInstant != null ? nextInstant : nextZdt.toInstant();
		final Value v = values.get(nextIdx);
		nextIdx = (nextIdx + 1) % size;
		if (nextInstant != null)
			nextInstant = nextInstant.plus(interval);
		else
			nextZdt = nextZdt.plus(interval);
		return new SampledValue(v, thisInstant.toEpochMilli(), Quality.GOOD);
	}
	
}
