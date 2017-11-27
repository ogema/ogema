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

import java.util.List;
import java.util.Map;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.iterator.api.SampledValueDataPoint;

public class SampledValueDataPointImpl extends DataPointImpl<SampledValue> implements SampledValueDataPoint {

	public SampledValueDataPointImpl(Map<Integer, SampledValue> values, Map<Integer, SampledValue> previousValues,
			Map<Integer, SampledValue> nextValues, MultiIteratorImpl<SampledValue> iterator) {
		super(values, previousValues, nextValues, iterator);
	}

	@Override
	public long getTimestamp() {
		return values.values().iterator().next().getTimestamp();
	}

	@Override
	public long getPreviousTimestamp() {
		if (iterator instanceof TimeSeriesMultiIteratorImplStepSize) {
			final Long step  =((TimeSeriesMultiIteratorImplStepSize) iterator).stepSize;
			if (step != null ) 
				return getTimestamp()-step;
		}
		long t = Long.MIN_VALUE;
		long current;
		for (SampledValue sv : previousValues.values()) {
			current = sv.getTimestamp();
			if (current > t)
				t = current;
		}
		return t;
	}

	@Override
	public long getNextTimestamp() {
		if (iterator instanceof TimeSeriesMultiIteratorImplStepSize) {
			final Long step  =((TimeSeriesMultiIteratorImplStepSize) iterator).stepSize;
			if (step != null ) 
				return getTimestamp()+step;
		}
		long t = Long.MAX_VALUE;
		long current;
		for (SampledValue sv : nextValues.values()) {
			current = sv.getTimestamp();
			if (current < t)
				t = current;
		}
		return t;
	}
	
	// TODO take into account averaging, etc.
	@Override
	public SampledValue getElement(int idx) {
		SampledValue sv = values.get(idx);
		if (sv != null)
			return sv;
		InterpolationMode mode = ((TimeSeriesMultiIteratorImpl) iterator).globalMode;
		if (mode != null)
			return getElement(idx, mode);
		final List<InterpolationMode> modes = ((TimeSeriesMultiIteratorImpl) iterator).modes;
		if (modes != null) {
			mode = modes.get(idx);
			if (mode != null)
				return getElement(idx, mode);
		}
		return null;
	}
	
	@Override
	public SampledValueDataPoint getPrevious(int stepsBack) throws IllegalArgumentException, IllegalStateException {
		return (SampledValueDataPoint) super.getPrevious(stepsBack);
	}

	@Override
	public SampledValue getElement(int idx, InterpolationMode interpolationMode) {
		SampledValue current = values.get(idx);
		if (current != null)
			return current;
		if (interpolationMode == InterpolationMode.NONE || interpolationMode == null)
			return null;
		final SampledValue previous = previousValues.get(idx);
		if (previous == null)
			return null;
		final long now = getTimestamp();
		if (interpolationMode == InterpolationMode.STEPS)
			return new SampledValue(previous.getValue(), now, previous.getQuality());
		final SampledValue next = nextValues.get(idx);
		if (next == null)
			return null;
		final long diff1 = now - previous.getTimestamp();
		final long diff2 = next.getTimestamp() - now;
		if (interpolationMode == InterpolationMode.NEAREST) {
			final SampledValue model = (diff1 <= diff2 ? previous : next);
			return new SampledValue(model.getValue(), now, model.getQuality());
		}
		final Quality qual = (previous.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD) ? Quality.GOOD : Quality.BAD;
		final float f1 = previous.getValue().getFloatValue();
		final float f2 = next.getValue().getFloatValue();
		final float result = (f1 + (f2-f1) * diff1/(diff1+diff2));
		// case interpolationMode linear
		return new SampledValue(new SampledValue(new FloatValue(result), now, qual));
	}

	@Override
	public SampledValue getNextElement(int idx) {
		return nextValues.get(idx);
	}
	@Override
	public SampledValue getPreviousElement(int idx) {
		return previousValues.get(idx);
	}

	@Override
	public float getSum(boolean ignoreMissingPoints, InterpolationMode mode) {
		float value = 0;
		for (int i=0;i<iterator.size();i++) {
			SampledValue sv = getElement(i, mode);
			if (sv == null || sv.getQuality() == Quality.BAD) {
				if (!ignoreMissingPoints)
					return Float.NaN;
				continue;
			}
			value += sv.getValue().getFloatValue();
		}
		return value;
	}
	
}
