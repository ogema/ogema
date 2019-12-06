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

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.v2.iterator.api.MultiTimeSeriesIteratorBuilder;

/**
 * To instantiate this, use a {@link MultiTimeSeriesIteratorBuilder}. Since this class is not exported, it cannot be
 * instantiated directly. 
 */
public class TimeSeriesMultiIteratorImplStepRuler extends TimeSeriesMultiIteratorImpl {
	
	final int[] stepRulers;

	// state variables
	// duplicate some variables from MultiIteratorImpl to keep track of matching points only
	// ordered chronologically; may be null (if maxNrHistoricalValues == 0)
	final Queue<SampledValueDataPointImpl> historicalValuesInternal;
	// equal time stamps
	final Map<Integer, SampledValue> currentValuesInternal;
	// equal time stamps, only defined for a subset of iterators
//	final Map<Integer, SampledValue> comingValuesInternal;  // TODO needed?
	SampledValueDataPointImpl currentInternal;
	// different time stamps
	final Map<Integer,SampledValue> nextValuesInternal;
	// different time stamps
	final Map<Integer,SampledValue> previousValuesInternal;
	
	public TimeSeriesMultiIteratorImplStepRuler(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues,
			final Map<Integer,SampledValue> lowerBoundaryValues, final Map<Integer,SampledValue> upperBoundaryValues,
			InterpolationMode globalMode, List<InterpolationMode> modes, boolean doAverage, boolean doIntegrate, int[] stepRulers) {
//			boolean doAverage, boolean doIntegrate) {
		super(iterators, maxNrHistoricalValues, lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate);
		if (doIntegrate || doAverage)
			throw new UnsupportedOperationException("integration and average not implemented yet");
		this.stepRulers = stepRulers;
		this.nextValuesInternal = new HashMap<>(iterators.size());
		this.previousValuesInternal = new HashMap<>(iterators.size());
		this.currentValuesInternal = new HashMap<>(iterators.size());
//		this.comingValuesInternal = new HashMap<>(iterators.size());
		this.historicalValuesInternal = (maxNrHistoricalValues != 0 ? new EvictingQueue<SampledValueDataPointImpl>(maxNrHistoricalValues) : null);
//		this.doAverage = doAverage;
//		this.doIntegrate = doIntegrate;
		init2();
	}
	
	@Override
	public SampledValueDataPointImpl next() {
		advance();
		currentInternal = getDataPoint(currentValuesInternal, previousValuesInternal, nextValuesInternal);
		return currentInternal;
	}
	
	@Override
	protected void advance() {
		if (comingValues.isEmpty())
			throw new NoSuchElementException("No further element");
		if (historicalValuesInternal != null && currentInternal != null) 
			historicalValuesInternal.add(currentInternal);
		super.advance();
		currentValuesInternal.clear();
		currentValuesInternal.putAll(currentValues);
		previousValuesInternal.clear();
		previousValuesInternal.putAll(previousValues);
		nextValuesInternal.clear();
		nextValuesInternal.putAll(nextValues);
		while (!applicableIndexContained(comingValues.keySet(), stepRulers)) {
			if (!super.hasNext()) {
				comingValues.clear();
				nextValues.clear();
				return;
			}
			super.advance();
		}
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
	
	protected void init2() {
		if (comingValues.isEmpty())
			return;
		while (!applicableIndexContained(comingValues.keySet(), stepRulers)) {
			if (!super.hasNext()) {
				comingValues.clear();
				nextValues.clear();
				return;
			}
			super.advance();
		}
	}
	
}
