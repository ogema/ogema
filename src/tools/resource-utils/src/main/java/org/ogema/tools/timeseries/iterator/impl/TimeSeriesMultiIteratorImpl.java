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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIterator;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;

/**
 * To instantiate this, use a {@link MultiTimeSeriesIteratorBuilder}. Since this class is not exported, it cannot be
 * instantiated directly. 
 */
public class TimeSeriesMultiIteratorImpl extends MultiIteratorImpl<SampledValue> implements MultiTimeSeriesIterator {
	
	final InterpolationMode globalMode;
	final List<InterpolationMode> modes;
	// only one of the following two flags can be true; they are only used in subclasses
	protected final boolean doAverage;
	protected final boolean doIntegrate;
	// state variable

	@Deprecated
	public TimeSeriesMultiIteratorImpl(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues) {
		this(iterators, maxNrHistoricalValues, null, null);
	}

	@Deprecated
	public TimeSeriesMultiIteratorImpl(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues,
			final Map<Integer,SampledValue> lowerBoundaryValues, final Map<Integer,SampledValue> upperBoundaryValues) {
		this(iterators, maxNrHistoricalValues, lowerBoundaryValues, upperBoundaryValues, null, null, false, false);
	}
	
	public TimeSeriesMultiIteratorImpl(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues,
			final Map<Integer,SampledValue> lowerBoundaryValues, final Map<Integer,SampledValue> upperBoundaryValues,
			InterpolationMode globalMode, List<InterpolationMode> modes, boolean doAverage, boolean doIntegrate) {
//			boolean doAverage, boolean doIntegrate) {
		super(iterators, maxNrHistoricalValues, lowerBoundaryValues, upperBoundaryValues);
		this.globalMode = globalMode;
		this.modes = modes;
		this.doAverage = doAverage;
		this.doIntegrate = doIntegrate;
	}
	

	@Override
	public SampledValueDataPointImpl next() {
		return (SampledValueDataPointImpl) super.next();
	}
	
	@Override
	protected SampledValueDataPointImpl getDataPoint(Map<Integer, SampledValue> currentValue,
			Map<Integer, SampledValue> previousValues, Map<Integer, SampledValue> nextValues) {
		return new SampledValueDataPointImpl(currentValue, previousValues, nextValues, this);
	}
	
	
	static final SampledValue interpolate(final long t, final SampledValue previous, final SampledValue next, final InterpolationMode mode) {
		switch (mode) {
		case STEPS:
			if (previous == null)
				return null;
			return new SampledValue(previous.getValue(), t, previous.getQuality());
		case NONE:
			return null;
		case LINEAR:
			if (previous == null || next == null)
				return null;
			final float p = previous.getValue().getFloatValue();
			final float n = next.getValue().getFloatValue();
			Long tp = previous.getTimestamp();
			Long tn = next.getTimestamp();
			if (tn.longValue() == tp.longValue())
				throw new IllegalArgumentException("Received same timestamp values : " + tp);
			float newV = p + (n-p)*(t-tp)/(tn-tp);
			return new SampledValue(new FloatValue(newV), t, 
				previous.getQuality() == Quality.GOOD && next.getQuality() == Quality.GOOD ? Quality.GOOD : Quality.BAD);
		case NEAREST:
			if (previous == null && next == null)
				return null;
			tp = (previous != null ? previous.getTimestamp() : null);
			tn = (next != null ? next.getTimestamp() : null);
			final SampledValue sv = (tp == null ? next : tn == null ? previous : (t-tp)<=(tn-t) ? previous : next);
			return new SampledValue(sv.getValue(), t, sv.getQuality());
		default:
			return null;
		}
	}
	
	static final float integrate(final SampledValue previous, final SampledValue start, final SampledValue end, final SampledValue next, final InterpolationMode mode) {
		if (start == null || end == null)
			return Float.NaN;
		final long startT = start.getTimestamp();
		final long endT = end.getTimestamp();
		if (startT == endT)
			return 0;
		if (endT < startT)
			throw new IllegalArgumentException("Interval boundaries interchanged");
		final float p;
		final float n;
		if (mode == null)
			throw new NullPointerException("Interpolation mode is null, integration not possible");
		switch (mode) {
		case STEPS:
			return previous.getValue().getFloatValue() * (endT-startT);
		case LINEAR:
			p = previous.getValue().getFloatValue();
			n = end.getValue().getFloatValue();
			return (endT-startT)*(p+(n-p)*(startT+endT-2*previous.getTimestamp())/2/(next.getTimestamp()-previous.getTimestamp()));
		case NEAREST:
			p = previous.getValue().getFloatValue();
			n = next.getValue().getFloatValue();
			Objects.requireNonNull(previous);
			Objects.requireNonNull(next);
			final long boundary = (next.getTimestamp() + previous.getTimestamp())/2;
			if (boundary <= startT)
				return n*(endT-startT);
			if (boundary >= endT)
				return p*(endT-startT);
			return p*(boundary-startT) + n*(endT-boundary);
		default:
			return Float.NaN;
		}
	}
	
	static final boolean applicableIndexContained(Collection<Integer> currentIndices, int[] stepRulers) {
		for (int i : stepRulers) {
			if (currentIndices.contains(i))
				return true;
		}
		return false;
	}	
	
	InterpolationMode getInterpolationMode(int idx) {
		return globalMode != null ? globalMode : modes != null && modes.size() > idx ? modes.get(idx) : null;
	}
	
}
