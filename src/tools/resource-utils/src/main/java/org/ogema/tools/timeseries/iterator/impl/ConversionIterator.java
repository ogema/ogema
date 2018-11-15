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
package org.ogema.tools.timeseries.iterator.impl;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.iterator.api.DataPoint;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;

abstract class ConversionIterator implements Iterator<SampledValue> {
	
	final Iterator<DataPoint<SampledValue>> input;
	final InterpolationMode forcedMode;
	final List<InterpolationMode> modes;
	final boolean ignoreGaps;
	
	ConversionIterator(
			Iterator<DataPoint<SampledValue>> input,
			boolean ignoreGaps,
			InterpolationMode forcedMode,
			List<InterpolationMode> modes) {
		this.input = input;
		this.ignoreGaps = ignoreGaps;
		this.forcedMode = forcedMode;
		this.modes = modes;
	}

	@Override
	public boolean hasNext() {
		return input.hasNext();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("ConversionIterator does not support removal.");
	}
	
	static class GenericSumIterator extends ConversionIterator {

		private final boolean sumOrAverage;
		
		GenericSumIterator(			
				Iterator<DataPoint<SampledValue>> input,
				boolean ignoreGaps,
				InterpolationMode forcedMode,
				List<InterpolationMode> modes,
				boolean sumOrAverage) {
			super(input, ignoreGaps, forcedMode, modes);
			this.sumOrAverage = sumOrAverage;
		}

		@Override
		public SampledValue next() {
			final SampledValueDataPoint point = (SampledValueDataPoint) input.next();
			SampledValue sv; 
			int cnt;
			float result;
			cnt = 0;
			result = 0;
			for (int n=0;n<point.inputSize();n++) {
				sv = point.getElement(n, forcedMode != null ? forcedMode : modes.get(n));
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
			if (cnt == 0)
				result = Float.NaN;
			else if (!sumOrAverage)
				result = result/cnt;
			if (Float.isNaN(result))
				return new SampledValue(FloatValue.NAN, point.getTimestamp(), Quality.BAD);
			return new SampledValue((result == 0F ? FloatValue.ZERO : new FloatValue(result)), point.getTimestamp(), Quality.GOOD);
		}
		
	}
	
	static class BooleanLogicIterator extends ConversionIterator {

		private final BooleanLogicTimeSeries timeSeries;
		
		BooleanLogicIterator(Iterator<DataPoint<SampledValue>> input, boolean ignoreGaps, BooleanLogicTimeSeries timeSeries) {
			super(input, ignoreGaps, InterpolationMode.STEPS, null);
			this.timeSeries = timeSeries; 
		}

		@Override
		public SampledValue next() {
			final SampledValueDataPoint point = (SampledValueDataPoint) input.next();
			SampledValue sv; 
			int cnt = 0;
			Quality q = Quality.GOOD;
			final boolean[] values = new boolean[timeSeries.size]; 
			for (int n=0;n<point.inputSize();n++) {
				sv = point.getElement(n, InterpolationMode.STEPS);
				if (sv == null || sv.getQuality() == Quality.BAD) {
					if (!ignoreGaps)
						q = Quality.BAD;
				} else {
					values[cnt] = sv.getValue().getBooleanValue();
				}
				cnt++;
			}
			return new SampledValue(timeSeries.getValue(values) ? BooleanValue.TRUE : BooleanValue.FALSE, point.getTimestamp(), q);
		}
		
	}
	
	static class FunctionIterator<N> extends ConversionIterator {
		
		private final FunctionMultiTimeSeries<N> timeSeries;

		FunctionIterator(			
				Iterator<DataPoint<SampledValue>> input,
				boolean ignoreGaps,
				InterpolationMode forcedMode,
				List<InterpolationMode> modes,
				FunctionMultiTimeSeries<N> timeSeries) {
			super(input, ignoreGaps, forcedMode, modes);
			this.timeSeries = timeSeries;
		}

		@Override
		public SampledValue next() {
			final SampledValueDataPoint point = (SampledValueDataPoint) input.next();
			SampledValue sv; 
			@SuppressWarnings("unchecked")
			final N[] values = (N[]) Array.newInstance(timeSeries.type, timeSeries.size);
			Quality q = Quality.GOOD;
			for (int n=0;n<point.inputSize();n++) {
				sv = point.getElement(n, forcedMode != null ? forcedMode : modes.get(n));
				if (sv == null || sv.getQuality() == Quality.BAD) {
					if (!ignoreGaps)
						q = Quality.BAD;
					values[n] = sv == null ? timeSeries.getNullReplacement() : timeSeries.getValue(sv);
				}
				else
					values[n] = timeSeries.getValue(sv);
			}
			N result = timeSeries.function.apply(Arrays.asList(values));
			if (result == null)
				result = timeSeries.getNullReplacement();
			if (timeSeries.type == Float.class && Float.isNaN((Float) result))
				q = Quality.BAD;
			return new SampledValue(timeSeries.getValue(result), point.getTimestamp(), q);
		}
		
	}

}
