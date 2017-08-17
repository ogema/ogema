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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.iterator.api.DataPoint;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;

/**
 * Provides either the sum or averages of the input time series;
 * does not copy the data points on construction, but evaluates the 
 * components on demand.
 */
public class GenericSumTimeSeries extends MultiTimeSeries {
	
	private final InterpolationMode imode;
	private final List<InterpolationMode> modes;
	private final boolean sumOrAverage;
	
	public GenericSumTimeSeries(
			Collection<ReadOnlyTimeSeries> constituents,
			boolean sumOrAverage,
			boolean ignoreGaps,
			InterpolationMode forcedModeConstituents,
			InterpolationMode forcedModeResult) {
		super(constituents, ignoreGaps,forcedModeConstituents, forcedModeResult);
		this.imode = forcedModeResult != null ? forcedModeResult : getMode(constituents);
		this.sumOrAverage = sumOrAverage;
		final List<InterpolationMode> modes = new ArrayList<>();
		for (ReadOnlyTimeSeries ts : constituents)
			modes.add(ts.getInterpolationMode());
		this.modes = Collections.unmodifiableList(modes);
	}
	
	@Override	
	public SampledValue getValue(long time) {
		float f = 0;
		int cnt = 0;
		Quality q = Quality.GOOD;
		for (ReadOnlyTimeSeries ts : constituents) {
			final SampledValue sv = ts.getValue(time);
			if (sv != null)
				cnt++;
			if (sv == null || sv.getQuality() == Quality.BAD) {
				if (!ignoreGaps)
					q = Quality.BAD;
			} else {
				f += sv.getValue().getFloatValue();
			}
		}
		if (cnt == 0)
			return null;
		if (!sumOrAverage)
			f = f/cnt;
		return new SampledValue(new FloatValue(f), time, q);
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
		return new ConversionIterator.GenericSumIterator(iterator, ignoreGaps, forcedModeConstituents, modes, sumOrAverage);
	}

}
