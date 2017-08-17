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
package org.ogema.tools.timeseries.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;

/**
 * An object behaving like OGEMA schedules but that is not an OGEMA resource. Can
 * be copied from and to real schedules. Intended use is for being used in complicated
 * calculations by OGEMA application that do not want to expose their intermediate
 * schedule calculation steps to the framework.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface MemoryTimeSeries extends TimeSeries, Cloneable {
	
	/**
	 * Returns the type of Value that this schedule takes. Note that there is
	 * no setter for this method. The type must be defined on construction.
	 * @return 
	 */
	Class<? extends Value> getValueType();

	/**
	 * Gets the value at time t. If the time series is not defined at the given
	 * point, including the case that t is outside of the range of defined values,
	 * this returns a result with bad quality (in contrast to {@link #getValue(long)},
	 * which can return null).
	 * @param t Timestamp for which the value is requested.
	 * @return Value at the time t with good quality, or a result with bad quality if the time series is not defined at t.
	 */
	SampledValue getValueSecure(long t);

	/**
	 * Write this to another time series. Overwrites all previous content in the
	 * schedule.
	 */
	void write(TimeSeries timeSeries);

	/**
	 * Write this to another time series. Overwrites all previous content in the
	 * schedule.
	 * @param from - time of the first value in the time series in ms since epoche (inclusive).
	 * @param to - time of the last value in the time series in ms since epoche (exclusive).
	 */
	void write(TimeSeries timeSeries, long from, long to);

	/**
	 * Copies another TimeSeries into this (schedules, recorded data, time series). 
	 * Overwrites all previous content of this.
	 * @return returns a reference to itself after operation.
	 */
	MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries);

	/**
	 * Copies part of another TimeSeries into this. Overwrites all previous content of this.
	 * @param start - time of the first value in the time series in ms since epoch (inclusive).
	 * @param end - time of the last value in the time series in ms since epoch (exclusive).
	 * @return returns a reference to itself after operation.
	 */
	MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries, long start, long end);
	
	/**
	 * Like {@link MemoryTimeSeries#read(ReadOnlyTimeSeries, long, long)}, except that two points
	 * for the boundary values are added, if no explicit values are provided by the schedule. The
	 * values for the boundary points are determined according to the interpolation mode of the
	 * provided time series. If no interpolation mode is set, or a boundary timestamp is outside
	 * the domain of the time series, no point is added for this boundary.  
	 * @param timeSeries
	 * @param start - start time
	 * @param end - end time (inclusive, deviating from {@link #read(ReadOnlyTimeSeries, long, long)}).
	 * @return this
	 */
	MemoryTimeSeries readWithBoundaries(ReadOnlyTimeSeries timeSeries, long start, long end);

	/**
	 * Adds a copy of a SampledValue to schedule. The value type of the sampled
	 * value must fit to the type of values represented by this.
	 * @param value new value to add to schedule.
	 */
	void addValue(SampledValue value);

	/**
	 * Shifts the timestamps of all entries by dT.
	 */
	void shiftTimestamps(long dt);

	/**
	 * Create a copy of this.
	 */
	MemoryTimeSeries clone();
	
	/*
	 *******************************
	 */
	
	/**
	 * An empty MemoryTimeSeries. All write operations throw {@link UnsupportedOperationException}. 
	 */
	public final static MemoryTimeSeries EMPTY_TIME_SERIES = new MemoryTimeSeries() {
		
		@Override
		public int size(long startTime, long endTime) {
			return 0;
		}
		
		@Override
		public int size() {
			return 0;
		}
		
		@Override
		public Iterator<SampledValue> iterator(long startTime, long endTime) {
			return Collections.emptyIterator();
		}
		
		@Override
		public Iterator<SampledValue> iterator() {
			return Collections.emptyIterator();
		}
		
		@Override
		public boolean isEmpty(long startTime, long endTime) {
			return true;
		}
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public List<SampledValue> getValues(long startTime, long endTime) {
			return Collections.emptyList();
		}
		
		@Override
		public List<SampledValue> getValues(long startTime) {
			return Collections.emptyList();
		}
		
		@Override
		public SampledValue getValue(long time) {
			return null;
		}
		
		@Override
		public Long getTimeOfLatestEntry() {
			return null;
		}
		
		@Override
		public SampledValue getPreviousValue(long time) {
			return null;
		}
		
		@Override
		public SampledValue getNextValue(long time) {
			return null;
		}
		
		@Override
		public InterpolationMode getInterpolationMode() {
			return InterpolationMode.NONE;
		}

		@Override
		public boolean addValue(long timestamp, Value value) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean addValues(Collection<SampledValue> values) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean addValue(long timestamp, Value value, long timeOfCalculation) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public Long getLastCalculationTime() {
			return null;
		}

		@Override
		public boolean deleteValues() {
			return false;
		}

		@Override
		public boolean deleteValues(long endTime) {
			return false;
		}

		@Override
		public boolean deleteValues(long startTime, long endTime) {
			return false;
		}

		@Override
		public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize,
				long timeOfCalculation) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public boolean setInterpolationMode(InterpolationMode mode) {
			throw new UnsupportedOperationException("Empty time series does not support setting the interpolation mode");
		}

		@Override
		public Class<? extends Value> getValueType() {
			return Value.class;
		}

		@Override
		public SampledValue getValueSecure(long t) {
			return null;
		}

		@Override
		public void write(TimeSeries timeSeries) {
			throw new UnsupportedOperationException("Empty time series does not support write");
		}

		@Override
		public void write(TimeSeries timeSeries, long from, long to) {
			throw new UnsupportedOperationException("Empty time series does not support write");
		}

		@Override
		public MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries) {
			throw new UnsupportedOperationException("Empty time series does not support read");
		}

		@Override
		public MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries, long start, long end) {
			throw new UnsupportedOperationException("Empty time series does not support read");
		}

		@Override
		public MemoryTimeSeries readWithBoundaries(ReadOnlyTimeSeries timeSeries, long start, long end) {
			throw new UnsupportedOperationException("Empty time series does not support read");
		}

		@Override
		public void addValue(SampledValue value) {
			throw new UnsupportedOperationException("Empty time series does not support adding points");
		}

		@Override
		public void shiftTimestamps(long dt) {}

		@Override
		public MemoryTimeSeries clone() {
			return this;
		}
		
	};
}
