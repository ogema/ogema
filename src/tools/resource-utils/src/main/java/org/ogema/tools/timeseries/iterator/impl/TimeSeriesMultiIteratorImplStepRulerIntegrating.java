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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;

import com.google.common.collect.EvictingQueue;

public class TimeSeriesMultiIteratorImplStepRulerIntegrating extends TimeSeriesMultiIteratorImpl {
	
	final int[] stepRulers;

	// state variables
	// duplicate some variables from MultiIteratorImpl to keep track of matching points only
	// ordered chronologically; may be null (if maxNrHistoricalValues == 0)
	final Queue<SampledValueDataPointImpl> historicalValuesInternal;
	// equal time stamps, only defined for a subset of iterators
//	final Map<Integer, SampledValue> comingValuesInternal;  // TODO needed?
	SampledValueDataPointImpl currentInternal;
	
	// equal time stamps, but contains all keys for defined time series
	final Map<Integer, SampledValue> currentValuesIntegrated;	
	// different time stamps
	final Map<Integer,SampledValue> nextValuesIntegrated;
	// different time stamps
	final Map<Integer,SampledValue> previousValuesIntegrated;
	// contains partial integrals only between two points defined by step rulesr
	final Map<Integer, Float> integrationBuffer;
	final Map<Integer, Long> intervalBuffer;
	// only != null if doIntegrate is set
	final Map<Integer, Float> lastValidValue;
	private final Set<Integer> nanBuffer;
	
	
	public TimeSeriesMultiIteratorImplStepRulerIntegrating(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues,
			final Map<Integer,SampledValue> lowerBoundaryValues, final Map<Integer,SampledValue> upperBoundaryValues,
			InterpolationMode globalMode, List<InterpolationMode> modes, boolean doAverage, boolean doIntegrate, int[] stepRulers) {
//			boolean doAverage, boolean doIntegrate) {
		super(iterators, maxNrHistoricalValues, lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate);
		if (!doIntegrate && !doAverage)
			throw new UnsupportedOperationException("This version of MultiTimeSeriesIterator only deals with integration/averaging");
		this.stepRulers = stepRulers;
//		this.comingValuesInternal = new HashMap<>(size);
		this.historicalValuesInternal = (maxNrHistoricalValues != 0 ? EvictingQueue.<SampledValueDataPointImpl> create(maxNrHistoricalValues) : null);
		this.nextValuesIntegrated = new HashMap<>(size);
		this.previousValuesIntegrated = new HashMap<>(size);
		this.currentValuesIntegrated = new HashMap<>(size);
		this.integrationBuffer = new HashMap<>(size);
		this.intervalBuffer = new HashMap<>(size);
		this.lastValidValue = doIntegrate ? new HashMap<Integer,Float>(size): null;
		this.nanBuffer = new HashSet<>(size);
//		this.doAverage = doAverage;
//		this.doIntegrate = doIntegrate;
		// TODO adapt
		init2();
	}
	
	@Override
	public SampledValueDataPointImpl next() {
		advance();
		// FIXME or provide the actual (nonintegrated) values?
		currentInternal = getDataPoint(currentValuesIntegrated, previousValuesIntegrated, nextValuesIntegrated);
		return currentInternal;
	}
	
	
	protected void init2() {
		if (comingValues.isEmpty())
			return;
		while (!applicableIndexContained(comingValues.keySet(), stepRulers)) {
			super.advance();
			if (!super.hasNext()) {
				comingValues.clear();
				nextValues.clear();
				break;
			}
			calculateIntegrals();
		} 
		for (Map.Entry<Integer, SampledValue> entry : comingValues.entrySet()) {
			final int idx = entry.getKey();
			if (!integrationBuffer.containsKey(idx) && !nanBuffer.contains(idx)) {
				final SampledValue sv = entry.getValue();
				final float val = sv.getValue().getFloatValue();
				if (doAverage) {
					if (sv.getQuality() == Quality.GOOD)
						integrationBuffer.put(idx, val);
					else 
						nanBuffer.add(idx);
				} else {
					if (sv.getQuality() == Quality.GOOD)
						integrationBuffer.put(idx, 0F);
					else 
						nanBuffer.add(idx);
				}
			}
		}
		getNextIntegralValues();
	}
	
	@Override
	protected void advance() {
		if (comingValues.isEmpty())
			throw new NoSuchElementException("No further element");
		if (historicalValuesInternal != null && currentInternal != null) 
			historicalValuesInternal.add(currentInternal);
		do {
			super.advance();
			if (!super.hasNext()) {
				comingValues.clear();
				nextValues.clear();
				break;
			}
			calculateIntegrals();
		} while (!applicableIndexContained(comingValues.keySet(), stepRulers));
		advanceIntegralValues();
		getNextIntegralValues();
	}
	
	private void calculateIntegrals() {
		final SampledValue end = comingValues.values().iterator().next();
		final SampledValue start = currentValues.values().iterator().next();
		final long diff = end.getTimestamp() - start.getTimestamp();
		for (int idx=0;idx<size;idx++) {
			final SampledValue current = currentValues.get(idx);
			final SampledValue startValue = current != null ? current : previousValues.get(idx);
			final SampledValue next = nextValues.get(idx);
			if (startValue == null || next == null)
				continue;
			final float result = integrate(startValue, start, end, next, getInterpolationMode(idx));
			final boolean ok = !Float.isNaN(result);
			if (ok) {
				if (!intervalBuffer.containsKey(idx))
					intervalBuffer.put(idx, diff);
				else 
					intervalBuffer.put(idx, intervalBuffer.get(idx) + diff);
				if (integrationBuffer.containsKey(idx))
					integrationBuffer.put(idx, integrationBuffer.get(idx) + result);
				else
					integrationBuffer.put(idx, result);
				nanBuffer.remove(idx);
				// remember bad quality only if no subinterval contains a valid value
			} else if (!integrationBuffer.containsKey(idx)) {
				nanBuffer.add(idx); 
			}
		}
	}
	
	private void getNextIntegralValues() {
		if (comingValues.isEmpty()) 
			return;
		final long nextT = comingValues.values().iterator().next().getTimestamp();
		SampledValue sv;
		for (Map.Entry<Integer, Float> entry: integrationBuffer.entrySet()) {
			float result = entry.getValue();
			final boolean ok = !Float.isNaN(result);
			if (ok) {
				if (doAverage) {
					final Long duration = intervalBuffer.get(entry.getKey());
					if (duration != null && duration > 0)
						result = result/duration;
				} else {  // doIntegrate
					final Float previous = lastValidValue.get(entry.getKey());
					if (previous != null)
						result += previous;
					lastValidValue.put(entry.getKey(), result);
				}
			}
			sv = new SampledValue(!ok ? FloatValue.NAN : new FloatValue(result), nextT, ok ? Quality.GOOD : Quality.BAD);
			nextValuesIntegrated.put(entry.getKey(), sv);
		}
		for (int idx : nanBuffer) {
			nextValuesIntegrated.put(idx, new SampledValue(FloatValue.NAN, nextT, Quality.BAD));
		}
		integrationBuffer.clear();
		intervalBuffer.clear();
		nanBuffer.clear();
	}
	
	private void advanceIntegralValues() {
//		if (currentValuesIntegrated.isEmpty() && nextValuesIntegrated.isEmpty()) { // add start values
//			
//			
//		}
		final Iterator<Map.Entry<Integer, SampledValue>> currentValsIt = currentValuesIntegrated.entrySet().iterator();
		while (currentValsIt.hasNext()) {
			final Map.Entry<Integer, SampledValue> entry = currentValsIt.next();
			currentValsIt.remove();
			previousValuesIntegrated.put(entry.getKey(), entry.getValue());
		}
		currentValuesIntegrated.putAll(nextValuesIntegrated);
		nextValuesIntegrated.clear();
	}
	
	protected SampledValueDataPointImpl getHistorical(int stepsBack) {
		if (historicalValuesInternal == null)
			throw new UnsupportedOperationException("This iterator does not store historical values");
		if (stepsBack <= 0 || stepsBack > maxNrHistoricalValues)
			throw new IllegalArgumentException("stepsBack must be a positive number between 1 and " + maxNrHistoricalValues + ". Got " + stepsBack);
		final int sz = historicalValuesInternal.size();
		if (sz < stepsBack)
			return null;
		int idx = sz - stepsBack;
		final Iterator<SampledValueDataPointImpl> it = historicalValuesInternal.iterator();
		for (int i=0;i<idx;i++)
			it.next();
		return it.next();
	}
	
}
