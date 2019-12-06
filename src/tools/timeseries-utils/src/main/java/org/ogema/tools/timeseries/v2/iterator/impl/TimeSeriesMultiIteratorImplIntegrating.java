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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;

public class TimeSeriesMultiIteratorImplIntegrating extends TimeSeriesMultiIteratorImpl {
	
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
	// only != null if doIntegrate is set
	final Map<Integer, Double> lastValidValue;
	
	
	public TimeSeriesMultiIteratorImplIntegrating(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues,
			final Map<Integer,SampledValue> lowerBoundaryValues, final Map<Integer,SampledValue> upperBoundaryValues,
			InterpolationMode globalMode, List<InterpolationMode> modes, boolean doAverage, boolean doIntegrate) {
//			boolean doAverage, boolean doIntegrate) {
		super(iterators, maxNrHistoricalValues, lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate);
		if (!doIntegrate && !doAverage)
			throw new UnsupportedOperationException("This version of MultiTimeSeriesIterator only deals with integration/averaging");
//		this.comingValuesInternal = new HashMap<>(size);
		this.historicalValuesInternal = (maxNrHistoricalValues != 0 ? new EvictingQueue<SampledValueDataPointImpl>(maxNrHistoricalValues) : null);
		this.nextValuesIntegrated = new HashMap<>(size);
		this.previousValuesIntegrated = new HashMap<>(size);
		this.currentValuesIntegrated = new HashMap<>(size);
		this.lastValidValue = doIntegrate ? new HashMap<>(size): null;
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
		for (Map.Entry<Integer, SampledValue> entry : comingValues.entrySet()) {
			final int key = entry.getKey();
			final SampledValue sv = entry.getValue();
			final double val = sv.getQuality() == Quality.GOOD ? (doAverage ? sv.getValue().getDoubleValue() : 0) : Double.NaN;
			final boolean ok = !Double.isNaN(val);
			nextValuesIntegrated.put(key, new SampledValue(ok ? new DoubleValue(val) : DoubleValue.NAN, sv.getTimestamp(), 
					ok ? Quality.GOOD : Quality.BAD));
		}
	}
	
	@Override
	protected void advance() {
		if (comingValues.isEmpty())
			throw new NoSuchElementException("No further element");
		if (historicalValuesInternal != null && currentInternal != null) 
			historicalValuesInternal.add(currentInternal);
		super.advance();
		advanceIntegralValues();
		getNextIntegralValues();
	}
	
	private void getNextIntegralValues() {
		if (comingValues.isEmpty()) 
			return;
		final SampledValue start = currentValues.values().iterator().next();
		final SampledValue end = comingValues.values().iterator().next();
		SampledValue next;
		SampledValue previous;
		final long t = end.getTimestamp();
		for (Map.Entry<Integer, SampledValue> entry: nextValues.entrySet()) {
			final int key = entry.getKey();
			next = entry.getValue();
			previous = currentValues.containsKey(key) ? currentValues.get(key) : previousValues.get(key);
			if (previous != null) {
				double result = integrate(previous, start, end, next, getInterpolationMode(key));
				final boolean ok = !Double.isNaN(result);
				if (ok) {
					if (doAverage)
						result = result/(end.getTimestamp() - start.getTimestamp());
					else {
						final Double last = lastValidValue.get(key);
						if (last != null)
							result += last;
					}
					nextValuesIntegrated.put(key, new SampledValue(new DoubleValue(result), t, Quality.GOOD));
					if (lastValidValue != null)
						lastValidValue.put(key, result);
					// remember bad quality only if no subinterval contains a valid value
				} else {
					nextValuesIntegrated.put(key, new SampledValue(DoubleValue.NAN, t, Quality.BAD));
				}
			}
			
		}
	}
	
	private void advanceIntegralValues() {
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
