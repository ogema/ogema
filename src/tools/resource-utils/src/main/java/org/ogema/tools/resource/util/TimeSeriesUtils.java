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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.api.TimeInterval;
import org.ogema.tools.timeseries.implementations.FloatTreeTimeSeries;

import com.google.common.base.Function;

/**
 * Util functions for the creation of time series. See also {@link ValueResourceUtils} and {@link MultiTimeSeriesUtils}.
 */
public class TimeSeriesUtils {
	
	/**
	 * Get an unmodifiable MemoryTimeSeries representation of the input time series.
	 * All operations that modify the time series, such as adding or deleting points, throw
	 * UnsupportedOperationException.
	 * 
	 * @param timeSeries
	 * @return
	 */
	public static MemoryTimeSeries unmodifiableTimeSeries(final ReadOnlyTimeSeries timeSeries) {
		return new UnmodifiableMemoryTimeSeries(timeSeries);
	}
	
	/**
	 * Get an unmodifiable FloatTimeSeries representation of the input time series.
	 * All operations that modify the time series, such as adding or deleting points, throw
	 * UnsupportedOperationException.
	 * 
	 * Note: while FloatTimeSeries provides several numerical convenience functions, wrapping a 
	 * time series in a FloatTimeSeries and applying these methods is typically less efficient than
	 * directly using the corresponding utility functions of {@link ValueResourceUtils}. 
	 * 
	 * @param timeSeries
	 * @return
	 */
	public static FloatTimeSeries unmodifiableFloatTimeSeries(final ReadOnlyTimeSeries timeSeries) {
		return new UnmodifiableFloatTimeSeries(timeSeries);
	}
	
	/**
	 * Create a time series with random float values, in the value range 0..1.
	 * @param nrPoints
	 * @param startTime
	 * @param avInterval
	 * @param randomizeTimeSteps
	 * @return
	 */
	public final static List<SampledValue> createRandomTimeSeries(final int nrPoints, final long startTime, final long avInterval, final boolean randomizeTimeSteps) {
		return createRandomTimeSeries(nrPoints, startTime, avInterval, randomizeTimeSteps, 1F, 0F);
	}

	/**
	 * Create a time series with random values.
	 * @param nrPoints
	 * @param startTime
	 * @param avInterval
	 * @param randomizeTimeSteps
	 * @param valueRange
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @param offset
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @return
	 */
	public final static List<SampledValue> createRandomTimeSeries(final int nrPoints, final long startTime, final long avInterval, final boolean randomizeTimeSteps, 
				final Number valueRange, final Number offset) {
		final List<SampledValue> values = new ArrayList<>();
		long t;
		Number n;
		for (int i=0;i <nrPoints; i++) {
			t = startTime + i*avInterval;
			if (randomizeTimeSteps)
				t += (Math.random()* 0.9 * avInterval) - avInterval/2;
			n = addAndRandomize(valueRange, offset, true);
			values.add(new SampledValue(createValue(n), t, Quality.GOOD));
		}
		return values;
	}
	
	/**
	 * Create a step function.
	 * @param nrPoints
	 * @param start
	 * @param interval
	 * @param startValue
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @param valueStep
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @return
	 */
	public final static List<SampledValue> createStepFunction(final int nrPoints, final long start, final long interval, 
			final Number startValue, final Number valueStep) {
		return createStepFunction(nrPoints, start, interval, false, startValue, valueStep, false);
	}
	
	/**
	 * Create a step function.
	 * @param nrPoints
	 * @param start
	 * @param avInterval
	 * @param randomizeTimeSteps
	 * @param startValue
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @param valueStep
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @param randomizeValueSteps
	 * @return
	 */
	public final static List<SampledValue> createStepFunction(final int nrPoints, final long start, final long avInterval, final boolean randomizeTimeSteps, 
			final Number startValue, final Number valueStep, final boolean randomizeValueSteps) {
		final List<SampledValue> values = new ArrayList<>();
		Number n = startValue;
		long t;
		for (int i=0;i <nrPoints; i++) {
			t = start + i*avInterval;
			if (randomizeTimeSteps)
				t += (Math.random()* 0.9 * avInterval) - avInterval/2;
			values.add(new SampledValue(createValue(n), t, Quality.GOOD));
			n = addAndRandomize(valueStep, n, randomizeValueSteps);
		}
		return values;
	}
	
	/**
	 * Create a time series based on a {@link Function}.
	 * For Java 7 compatibility this uses Guava's function interface over java.util.Function.
	 * @param function
	 * @param nrPoints
	 * @param start
	 * @param interval
	 * @return
	 */
	public final static <N extends Number> List<SampledValue> createFunction(final Function<Long, N> function, final int nrPoints, final long start, final long interval) {
		Objects.requireNonNull(function);
		final List<SampledValue> values = new ArrayList<>();
		SampledValue sv;
		long t = start;
		for (int i=0;i <nrPoints; i++) {
			sv = new SampledValue(createValue(function.apply(t)), t, Quality.GOOD);
			values.add(sv);
			t += interval;
		}
		return values;
	}

	/**
	 * Create a {@link Value} from a {@link Number}.
	 * @param value
	 * 		either a {@link Float}, {@link Double}, {@link Integer}, {@link Long}, {@link Short} or {@link Byte}.
	 * @throws IllegalArgumentException
	 * 		if <tt>value</tt> is not of admissible type
	 * @return
	 */
	public static Value createValue(final Number value) throws IllegalArgumentException {
		if (value instanceof Float) {
			final float f = value.floatValue();
			return f == 0F ? FloatValue.ZERO : Float.isNaN(f) ? FloatValue.NAN : new FloatValue(f);
		} 
		if (value instanceof Integer || value instanceof Short)
			return new IntegerValue(value.intValue());
		if (value instanceof Long)
			return new LongValue(value.longValue());
		if (value instanceof Double)
			return new DoubleValue(value.doubleValue());
		if (value instanceof Byte)
			return new ByteArrayValue(new byte[]{value.byteValue()});
		throw new IllegalArgumentException("Value type " + value.getClass() + " not supported");
	}

	private static Number addAndRandomize(final Number size, final Number offset, final boolean randomize) {
		if (size instanceof Double || offset instanceof Double) 
			return offset.doubleValue() + (randomize ? Math.random() * size.doubleValue() : size.doubleValue());
		if (size instanceof Float || offset instanceof Float) 
			return offset.floatValue() + (randomize ? ((float) Math.random()) * size.floatValue() : size.floatValue());
		if (size instanceof Long || offset instanceof Long) 
			return offset.longValue() + (randomize ? (long) (Math.random() * size.longValue()) : size.longValue());
		if (size instanceof Integer || size instanceof Short) 
			return offset.intValue() + (randomize ? (int) (Math.random() * size.intValue()) : size.intValue());
		throw new IllegalArgumentException("Value type " + size.getClass() + " not supported");
	}
	
	private static class UnmodifiableMemoryTimeSeries implements MemoryTimeSeries {
		
		protected final ReadOnlyTimeSeries input;
		private final boolean isMemoryTimeseries;
		
		public UnmodifiableMemoryTimeSeries(ReadOnlyTimeSeries input) {
			this.input = input;
			this.isMemoryTimeseries = input instanceof MemoryTimeSeries;
		}

		@Override
		public boolean addValue(long timestamp, Value value) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean addValues(Collection<SampledValue> values) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean addValue(long timestamp, Value value, long timeOfCalculation) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public Long getLastCalculationTime() {
			return isMemoryTimeseries ? ((MemoryTimeSeries) input).getLastCalculationTime() : null;
		}

		@Override
		public boolean deleteValues() {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean deleteValues(long endTime) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean deleteValues(long startTime, long endTime) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize,
				long timeOfCalculation) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public boolean setInterpolationMode(InterpolationMode mode) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public SampledValue getValue(long time) {
			return input.getValue(time);
		}

		@Override
		public SampledValue getNextValue(long time) {
			return input.getNextValue(time);
		}

		@Override
		public SampledValue getPreviousValue(long time) {
			return input.getPreviousValue(time);
		}

		@Override
		public List<SampledValue> getValues(long startTime) {
			return input.getValues(startTime);
		}

		@Override
		public List<SampledValue> getValues(long startTime, long endTime) {
			return input.getValues(startTime, endTime);
		}

		@Override
		public InterpolationMode getInterpolationMode() {
			return input.getInterpolationMode();
		}

		@Override
		public boolean isEmpty() {
			return input.isEmpty();
		}

		@Override
		public boolean isEmpty(long startTime, long endTime) {
			return input.isEmpty(startTime, endTime);
		}

		@Override
		public int size() {
			return input.size();
		}

		@Override
		public int size(long startTime, long endTime) {
			return input.size(startTime, endTime);
		}

		@Override
		public Iterator<SampledValue> iterator() {
			// assuming that the input iterator does not allow removal... otherwise we'd need to wrap it
			return input.iterator();
		}

		@Override
		public Iterator<SampledValue> iterator(long startTime, long endTime) {
			// assuming that the input iterator does not allow removal... otherwise we'd need to wrap it
			return input.iterator(startTime, endTime);
		}

		@SuppressWarnings("deprecation")
		@Override
		public Long getTimeOfLatestEntry() {
			return input.getTimeOfLatestEntry();
		}

		@Override
		public Class<? extends Value> getValueType() {
			if (isMemoryTimeseries)
				return ((MemoryTimeSeries) input).getValueType();
			if (input instanceof Schedule) 
				return ValueResourceUtils.getValueType((Schedule) input);
			if (input instanceof RecordedData) 
				return FloatValue.class; // best guess... we'd actually need a ResourceAccess to determine the resource type 
			return null;
		}

		@Override
		public SampledValue getValueSecure(long t) {
			if (isMemoryTimeseries)
				return ((MemoryTimeSeries) input).getValueSecure(t);
			final SampledValue result = getValue(t);
	        return (result!=null) ? result : new SampledValue(FloatValue.NAN, t, Quality.BAD);
		}

		@Override
		public void write(TimeSeries schedule) {
			if (isMemoryTimeseries)
				((MemoryTimeSeries) input).write(schedule);
			else {
				final List<SampledValue> values = getValues(Long.MIN_VALUE);
				schedule.replaceValues(Long.MIN_VALUE, Long.MAX_VALUE, values);
				schedule.setInterpolationMode(this.getInterpolationMode());
			}
		}

		@Override
		public void write(TimeSeries schedule, long from, long to) {
			if (isMemoryTimeseries)
				((MemoryTimeSeries) input).write(schedule, from, to);
			else {
				final List<SampledValue> values = getValues(from,to);
				schedule.replaceValues(from, to, values);
				schedule.setInterpolationMode(this.getInterpolationMode());
			}
		}

		@Override
		public MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries, long start, long end) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public MemoryTimeSeries readWithBoundaries(ReadOnlyTimeSeries timeSeries, long start, long end) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public void addValue(SampledValue value) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public void shiftTimestamps(long dt) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public MemoryTimeSeries clone() {
			return new UnmodifiableMemoryTimeSeries(input);
		}
		
	}
	
	private static class UnmodifiableFloatTimeSeries extends UnmodifiableMemoryTimeSeries implements FloatTimeSeries {
		
		private final boolean isFloatTimeSeries;

		public UnmodifiableFloatTimeSeries(ReadOnlyTimeSeries input) {
			super(input);
			this.isFloatTimeSeries = input instanceof FloatTimeSeries;
		}

		@Override
		public void multiplyBy(float factor) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public void multiplyBy(ReadOnlyTimeSeries factor) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public FloatTimeSeries times(float factor) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).times(factor);
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			result.read(input);
			result.multiplyBy(factor);
			return result;
		}

		@Override
		public FloatTimeSeries times(ReadOnlyTimeSeries other) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).times(other);
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			result.read(input);
			result.times(other);
			return result;
		}

		@Override
		public void add(float addend) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public void add(ReadOnlyTimeSeries other) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public FloatTimeSeries plus(float addend) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).plus(addend);
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			result.read(input);
			result.plus(addend);
			return result;
		}

		@Override
		public FloatTimeSeries plus(FloatTimeSeries other) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).plus(other);
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			result.read(input);
			result.plus(other);
			return result;
		}

		@Override
		public float integrate(long t0, long t1) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).integrate(t0, t1);
			return ValueResourceUtils.integrate(input, t0, t1);
		}

		@Override
		public float integrate(TimeInterval interval) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).integrate(interval);
			return ValueResourceUtils.integrate(input, interval.getStart(), interval.getEnd());
		}

		@Override
		public float integrateAbsolute(long t0, long t1) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).integrateAbsolute(t0, t1);
			final FloatTimeSeries absCopy = getAbsolute(); // TODO inefficient... better provide an iterator that maps the value to its absolute part
			return absCopy.integrate(t0, t1);
		}

		@Override
		public float integrateAbsolute(TimeInterval interval) {
			return integrateAbsolute(interval.getStart(), interval.getEnd());
		}

		@Override
		public float integratePositive(TimeInterval interval) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).integratePositive(interval);
			final List<TimeInterval> positiveDomains = getPositiveDomain(interval);
			float result = 0.f;
			for (TimeInterval subDomain : positiveDomains) {
				result += integrate(subDomain);
			}
			return result;
		}

		@Override
		public float integratePositive(long t0, long t1) {
			return (t0 < t1) ? integratePositive(new TimeInterval(t0, t1)) : -integratePositive(new TimeInterval(t1, t0));
		}

		@Override
		public float getAverage(long t0, long t1) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).getAverage(t0, t1);
			return ValueResourceUtils.getAverage(input, t0, t1);
		}

		@Override
		public List<SampledValue> downsample(long t0, long t1, long minimumInterval) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).downsample(t0, t1, minimumInterval);
			FloatTimeSeries ftt = new FloatTreeTimeSeries();
			ftt.read(input, t0, t1);
			return ftt.downsample(t0, t1, minimumInterval);
		}

		@Override
		public SampledValue getMax(long t0, long t1) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).getMax(t0, t1);
			return ValueResourceUtils.getMax(input, t0, t1);
		}

		@Override
		public SampledValue getMin(long t0, long t1) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).getMin(t0, t1);
			return ValueResourceUtils.getMin(input, t0, t1);
		}

		@Override
		public FloatTimeSeries getAbsolute() {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).getAbsolute();
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			if (input.getInterpolationMode() == InterpolationMode.LINEAR) {
				result.read(input);
				return result.getAbsolute();
			}
			result.setInterpolationMode(getInterpolationMode());
			if (input.isEmpty()) {
				return result;
			}

			final Iterator<SampledValue> it = input.iterator();
			SampledValue sv;
			while (it.hasNext()) {
				sv = it.next();
				final float x = sv.getValue().getFloatValue();
				if (x >= 0) {
					result.addValue(sv);
				}
				else {
					result.addValue(new SampledValue(new FloatValue(-x), sv.getTimestamp(), sv.getQuality()));
				}
			}
			return result;
		}

		@Override
		public void setConstant(float value) {
			throw new UnsupportedOperationException("Unmodifiable time series");
		}

		@Override
		public List<TimeInterval> getPositiveDomain(TimeInterval searchInterval) {
			if (isFloatTimeSeries)
				return ((FloatTimeSeries) input).getPositiveDomain(searchInterval);
			final FloatTimeSeries result = new FloatTreeTimeSeries();
			result.read(input);
			return result.getPositiveDomain(searchInterval);
		}

		@Override
		public void optimizeRepresentation() {}
		
	}
	
}
