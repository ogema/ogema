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
package org.ogema.tools.timeseries.v2.iterator.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.v2.iterator.api.DataPoint;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIteratorBuilder;
import org.ogema.core.channelmanager.measurements.StringValue;

/**
 * Provides either the sum or averages of the input time series;
 * does not copy the data points on construction, but evaluates the 
 * components on demand.
 * @param <N> 
 * 		either Float, Integer or Long
 */
public class FunctionMultiTimeSeries<N> extends MultiTimeSeries {
	
	private final InterpolationMode imode;
	private final List<InterpolationMode> modes;
	final Function<Collection<N>, N> function;
	final Class<N> type;
	
	public FunctionMultiTimeSeries(
			Collection<ReadOnlyTimeSeries> constituents,
			boolean ignoreGaps,
			Function<Collection<N>, N> function,
			Class<N> type,
			InterpolationMode forcedModeConstituents,
			InterpolationMode forcedModeResult) {
		super(constituents, ignoreGaps, forcedModeConstituents, forcedModeResult);
		this.imode = forcedModeResult != null ? forcedModeResult : getMode(constituents);
		this.function = function;
		final List<InterpolationMode> modes = new ArrayList<>();
		for (ReadOnlyTimeSeries ts : constituents)
			modes.add(ts.getInterpolationMode());
		this.modes = Collections.unmodifiableList(modes);
		this.type = type;
		if (type != Float.class && type != Integer.class && type != Long.class && type != String.class)
			throw new IllegalArgumentException("Illegal type, must be either Float.class, Integer.class or Long.class, got " + type);
	}
	
	@SuppressWarnings("unchecked")
	N getValue(final SampledValue sv) {
		if (type == Double.class)
			return (N) (Double) sv.getValue().getDoubleValue();
		if (type == Float.class)
			return (N) (Float) sv.getValue().getFloatValue();
		if (type == Integer.class)
			return (N) (Integer) sv.getValue().getIntegerValue();
		if (type == Long.class)
			return (N) (Long) sv.getValue().getLongValue();
        if (type == String.class) {
            return (N) sv.getValue().getStringValue();
        }
        return (N) (Double) sv.getValue().getDoubleValue();
	}
	
	@SuppressWarnings("unchecked")
	N getNullReplacement() {
		if (type == Double.class)
			return (N) (Double) Double.NaN;
		if (type == Float.class)
			return (N) (Float) Float.NaN;
		if (type == Integer.class || type == Long.class)
			return (N) (Long) 0L;
        if (type == String.class) {
            return (N) "";
        }
        return (N) (Double) Double.NaN;
	}
	
	Value getValue(N value) {
		if (value == null)
			value = getNullReplacement();
		if (type == Double.class)
			return new DoubleValue((Double) value);
		if (type == Float.class)
			return new FloatValue((Float) value);
		if (type == Integer.class)
			return new IntegerValue((Integer) value); 
		if (type == Long.class)
			return new LongValue((Long) value);
        if (type == String.class) {
            return new StringValue((String) value);
        }
        return new DoubleValue((Double) value);
	}
	
	
	@Override	
	public SampledValue getValue(long time) {
		int cnt = 0;
		int inRangeCount = 0;
		Quality q = Quality.GOOD;
		@SuppressWarnings("unchecked")
		final N[] values = (N[]) Array.newInstance(type, size);
		for (ReadOnlyTimeSeries ts : constituents) {
			final SampledValue sv = ts.getValue(time);
			if (sv != null)
				inRangeCount++;
			if (sv == null || sv.getQuality() == Quality.BAD) {
				if (!ignoreGaps)
					q = Quality.BAD;
			} else {
				values[cnt] = getValue(sv);
			}
			cnt++;
		}
		if (inRangeCount == 0)
			return null;
		N result = function.apply(Arrays.asList(values));
		if (result == null)
			result = getNullReplacement();
		if (type == Float.class && Float.isNaN((Float) result) || type == Double.class && Double.isNaN((Double) result))
			q = Quality.BAD;
		return new SampledValue(getValue(result), time, q);
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return imode;
	}

	@Override
	public Iterator<SampledValue> iterator() {
		return iterator(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		final List<Iterator<SampledValue>> iterators = new ArrayList<>();
		for (ReadOnlyTimeSeries ts : constituents) {
			iterators.add(ts.iterator(startTime, endTime));
		}
		final MultiTimeSeriesIteratorBuilder itBuilder = MultiTimeSeriesIteratorBuilder.newBuilder(iterators);
		if (forcedModeConstituents != null)
			itBuilder.setGlobalInterpolationMode(forcedModeConstituents);
		else
			itBuilder.setIndividualInterpolationModes(modes);
		final Iterator<DataPoint<SampledValue>> iterator = itBuilder.build();
		return new ConversionIterator.FunctionIterator<N>(iterator, ignoreGaps, forcedModeConstituents, modes, this);
	}

}
