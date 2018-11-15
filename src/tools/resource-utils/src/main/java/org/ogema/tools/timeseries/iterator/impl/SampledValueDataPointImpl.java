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
