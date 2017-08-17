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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.iterator.api.DataPoint;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;

/**
 * We assume interpolation mode {@link InterpolationMode#STEPS} for 
 * all time series.
 */
public abstract class BooleanLogicTimeSeries extends MultiTimeSeries {

	BooleanLogicTimeSeries(
			Collection<ReadOnlyTimeSeries> constituents,
			boolean ignoreGaps) {
		super(constituents, ignoreGaps, InterpolationMode.STEPS, InterpolationMode.STEPS);
	}
	
	/**
	 * the concatenation operation 
	 */
	protected abstract boolean getValue(boolean... input);
	
	@Override	
	public SampledValue getValue(long time) {
		int cnt = 0;
		int cntInRange = 0;
		Quality q = Quality.GOOD;
		final boolean[] values = new boolean[size]; 
		for (ReadOnlyTimeSeries ts : constituents) {
			final SampledValue sv = ts.getValue(time);
			if (sv != null)
				cntInRange++;
			if (sv == null || sv.getQuality() == Quality.BAD) {
				if (!ignoreGaps)
					q = Quality.BAD;
			} else {
				values[cnt] = sv.getValue().getBooleanValue();
			}
			cnt++;
		}
		if (cntInRange == 0)
			return null;
		return new SampledValue(getValue(values) ? BooleanValue.TRUE : BooleanValue.FALSE, time, q);
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return InterpolationMode.STEPS;
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
		final Iterator<DataPoint<SampledValue>> iterator = MultiTimeSeriesIteratorBuilder.newBuilder(iterators)
				.build();
		return new ConversionIterator.BooleanLogicIterator(iterator, ignoreGaps, this);
	}
	
	
	public static class BooleanAndTimeSeries extends BooleanLogicTimeSeries {

		public BooleanAndTimeSeries(Collection<ReadOnlyTimeSeries> constituents, boolean ignoreGaps) {
			super(constituents, ignoreGaps);
		}
		
		@Override
		protected boolean getValue(boolean... input) {
			for (boolean i : input) {
				if (!i)
					return false;
			}
			return true;
		}
		
	}
	
	public static class BooleanOrTimeSeries extends BooleanLogicTimeSeries {

		public BooleanOrTimeSeries(Collection<ReadOnlyTimeSeries> constituents, boolean ignoreGaps) {
			super(constituents, ignoreGaps);
		}

		@Override
		protected boolean getValue(boolean... input) {
			for (boolean i : input) {
				if (i)
					return true;
			}
			return false;
		}
		
	}
	
}
