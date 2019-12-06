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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * Tools for time series creation
 */
public class SampleTimeSeries {
	
	private SampleTimeSeries() {
		// TODO Auto-generated constructor stub
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

}
