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
package org.ogema.tools.timeseries.iterator.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.iterator.api.DataPoint;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;

import com.google.common.base.Function;

/**
 * Provides either the sum or averages of the input time series;
 * does not copy the data points on construction, but evaluates the 
 * components on demand.
 * @param <N> 
 * 		either Float, Integer or Long
 */
public class FunctionMultiTimeSeries<N extends Number> extends MultiTimeSeries {
	
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
		if (type != Float.class && type != Integer.class && type != Long.class)
			throw new IllegalArgumentException("Illegal type, must be either Float.class, Integer.class or Long.class, got " + type);
	}
	
	@SuppressWarnings("unchecked")
	N getValue(final SampledValue sv) {
		if (type == Float.class)
			return (N) (Float) sv.getValue().getFloatValue();
		if (type == Integer.class)
			return (N) (Integer) sv.getValue().getIntegerValue();
		if (type == Long.class)
			return (N) (Long) sv.getValue().getLongValue();
		return (N) (Float) sv.getValue().getFloatValue();
	}
	
	@SuppressWarnings("unchecked")
	N getNullReplacement() {
		if (type == Float.class)
			return (N) (Float) Float.NaN;
		if (type == Integer.class || type == Long.class)
			return (N) (Long) 0L;
		return (N) (Float) Float.NaN;
	}
	
	Value getValue(N value) {
		if (value == null)
			value = getNullReplacement();
		if (type == Float.class)
			return new FloatValue((Float) value);
		if (type == Integer.class)
			return new IntegerValue((Integer) value); 
		if (type == Long.class)
			return new LongValue((Long) value);
		return new FloatValue((Float) value);
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
		if (type == Float.class && Float.isNaN((Float) result))
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
