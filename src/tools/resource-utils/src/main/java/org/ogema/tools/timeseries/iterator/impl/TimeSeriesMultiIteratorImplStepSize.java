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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.iterator.api.MultiTimeSeriesIteratorBuilder;

/**
 * To instantiate this, use a {@link MultiTimeSeriesIteratorBuilder}, and set the 
 * {@link MultiTimeSeriesIteratorBuilder#setStepSize(long, long) stepSize}. 
 * Since this class is not exported, it cannot be instantiated directly. 
 */
// TODO 
//  - gaps handling (missing points and bad quality points)
public class TimeSeriesMultiIteratorImplStepSize extends TimeSeriesMultiIteratorImpl {
	
	// fixed step size
	final Long stepSize;
	// only evaluated in conjunction with stepSize
	final Long startTime;
	// state variablea
	// partial integral values
	final Map<Integer,Float> integrals;
	// complete integrals over the last period
	final Map<Integer,Float> currentIntegrals;

	
	// states for stepSize
	final Map<Integer,SampledValue> nextPrevious = new HashMap<>();
	final Map<Integer,SampledValue> nextNext = new HashMap<>();
	Long nextTargetTime; // only != null if stepSize != null 
	
	public TimeSeriesMultiIteratorImplStepSize(List<Iterator<SampledValue>> iterators, int maxNrHistoricalValues,
			final Map<Integer,SampledValue> lowerBoundaryValues, final Map<Integer,SampledValue> upperBoundaryValues,
			InterpolationMode globalMode, List<InterpolationMode> modes, boolean doAverage, boolean doIntegrate, Long stepSize, Long startTime) {
//			boolean doAverage, boolean doIntegrate) {
		super(iterators, maxNrHistoricalValues, lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate);
		this.stepSize = stepSize;
		this.startTime = startTime;
		this.integrals = (doAverage || doIntegrate) ? new HashMap<Integer,Float>(iterators.size()) : null;
		this.currentIntegrals = (doAverage || doIntegrate) ? new HashMap<Integer,Float>(iterators.size()) : null;
//		this.doAverage = doAverage;
//		this.doIntegrate = doIntegrate;
		init2();
	}
	
	@Override
	protected void advance() {
		if (comingValues.isEmpty())
			throw new NoSuchElementException("No further element");
		if (historicalValues != null && current != null) {
			historicalValues.add(current);
		}
		previousValues.clear();
		previousValues.putAll(nextPrevious);
		if (integrals != null) { // finish integrals computation
			currentIntegrals.clear();
			for (Map.Entry<Integer, SampledValue> entry: previousValues.entrySet()) {
				final int idx = entry.getKey();
				if (!currentValues.containsKey(idx)) // no partial intervals evaluated
					continue;
				SampledValue upperBoundayPoint = comingValues.get(idx); 
				if (upperBoundayPoint == null || upperBoundayPoint.getQuality() == Quality.BAD) {// TODO if gaps are ignored?
					integrals.remove(idx);
					continue;
				}
				final SampledValue previous = entry.getValue();
				final InterpolationMode mode = getInterpolationMode(idx);
				final float val;
//				if (integrals.containsKey(idx)) // tp > lastPoint
//					val = integrate(previous, previous, upperBoundayPoint, upperBoundayPoint, mode) + integrals.get(idx); // FIXME mode NEAREST not considered! 
//				else {
				if (!integrals.containsKey(idx)) {// tp < lastPoint
					SampledValue lowerBoundaryPoint = interpolate(nextTargetTime - stepSize, previous, upperBoundayPoint, mode);
					if (lowerBoundaryPoint == null || lowerBoundaryPoint.getQuality() == Quality.BAD)
						continue;
					val = integrate(previous, lowerBoundaryPoint, upperBoundayPoint, upperBoundayPoint, mode);
				} else
					val = integrals.get(idx);
				currentIntegrals.put(idx, val);
				// init new integrals computation XXX
//				SampledValue sv= comingValues.get(idx);
//				final SampledValue boundaryVal = sv != null? sv : interpolate(nextTargetTime, previous, next, mode);
//				if (boundaryVal != null) {
//					float itg = integrate(sv != null ? sv : previous, boundaryVal, next, next, mode);
//					if (!Float.isNaN(itg))
//						integrals.put(idx, itg);
//					else
//						integrals.remove(idx);
//				}
//				else
//					integrals.remove(idx);
			}
			// init new integrals computation -> moved to advance loop below
			for (Map.Entry<Integer, SampledValue> entry: comingValues.entrySet()) {
				final int idx = entry.getKey();
//				final SampledValue next = nextNext.get(idx);
//				if (next == null) {
//					integrals.remove(idx);
//					continue;
//				}
//				// FIXME
//				System.out.println("    coming: " + entry.getValue().getTimestamp() + " : next " + next.getTimestamp());
//				// other case will be dealt with in finish integrals computation above
//				if (next.getTimestamp() < nextTargetTime+startTime) {
//					final InterpolationMode mode = getInterpolationMode(idx);
//					float val = integrate(entry.getValue(), entry.getValue(), next, next, mode);
//					integrals.put(idx, val);
//				} else
//					integrals.remove(idx);
				if (!currentIntegrals.containsKey(idx)) // relevant for initialisation, if first point matches first target time stamp
					currentIntegrals.put(idx, 0F); 
				integrals.put(idx, 0F);
			}
			integrals.keySet().retainAll(comingValues.keySet());
		}
		
		nextValues.clear();
		nextValues.putAll(nextNext);
		
		currentValues.clear();
		if (doIntegrate || doAverage) {
			float val;
			for (Map.Entry<Integer, Float> entry: currentIntegrals.entrySet()) {
				val = entry.getValue();
				if (doAverage)
					val = val/stepSize;
				currentValues.put(entry.getKey(), new SampledValue(new FloatValue(val), nextTargetTime, Quality.GOOD)); // FIXME quality
			}
		} 
		else
			currentValues.putAll(comingValues);
		nextTargetTime += stepSize;
		
		for (int idx = 0; idx < iterators.size(); idx++) {
			SampledValue n = nextNext.get(idx);
			final boolean doIntegrate = integrals != null && integrals.containsKey(idx);
			if (n == null) {
				if (doIntegrate)
					integrals.remove(idx);
				comingValues.remove(idx);
				continue;
			}
			if (!previousValues.containsKey(idx) && comingValues.containsKey(idx))
				nextPrevious.put(idx, comingValues.get(idx));
			final Iterator<SampledValue> it = iterators.get(idx);
			long t = n.getTimestamp();
			final InterpolationMode mode = getInterpolationMode(idx);
			if (doIntegrate) {
				final SampledValue last = comingValues.get(idx);
				if (last != null) {
					final SampledValue end = (t < nextTargetTime ? n : interpolate(nextTargetTime, last, n, mode));
					
					// FIXME the order of n and end must be interchanged... but then it doesn't work any more
					float itgr = integrate(last, last, n, end, mode);
					integrals.put(idx, integrals.get(idx) + itgr);
				}
			}
			while (t < nextTargetTime) {
				if (!it.hasNext()) {
					n=null;
					break;
				}
				nextPrevious.put(idx, n);
				n = it.next();
				t = n.getTimestamp();
				if (doIntegrate) {
					final SampledValue last = nextPrevious.get(idx);
					final SampledValue end = (t <= nextTargetTime ? n : interpolate(nextTargetTime, last, n, mode));
					float itgr = integrate(last, last, end, n, mode);
					integrals.put(idx, integrals.get(idx) + itgr);
				}
			}
			if (n==null) {
				comingValues.remove(idx);
				nextNext.remove(idx);
			} else {
				if (t==nextTargetTime) {
					// XXX ?
//						if (n==null) 
//							comingValues.put(idx, n);
//						else
//							comingValues.remove(idx);
					comingValues.put(idx, n); // TODO check
					if (it.hasNext()) 
						nextNext.put(idx, it.next());
					else
						nextNext.remove(idx);
				} else {
					nextNext.put(idx, n);
					SampledValue prev = nextPrevious.get(idx);
					if (prev == null) 
						continue;
//						SampledValue next = nextValues.get(idx);
//					final InterpolationMode m = (globalMode != null ? globalMode : modes != null ? modes.get(idx) : InterpolationMode.LINEAR);
					final SampledValue v = interpolate(nextTargetTime,prev,n,mode);
					if (v != null)
						comingValues.put(idx, v);
				}
			}
		}
		// for debuggign
//		System.out.println("  advance done" );
//		System.out.println("    nextPrevious: " + nextPrevious);
//		System.out.println("    current:  " + currentValues);
//		System.out.println("    coming:   " + comingValues);
//		System.out.println("    nextNext:   " + nextNext);
//		System.out.println("    next time stamp:     " + nextTargetTime);
	}
	
	
	
	protected void init2() {
		if (comingValues.isEmpty())
			return;
		long firstT = comingValues.values().iterator().next().getTimestamp();
		long start = startTime;
		final int offset = (start < firstT ? 1 : 0);
		long diff = (firstT - start - offset) / stepSize + offset;
		start += diff * stepSize; // now start is in the range [firstT, firstT + step) 
		while (firstT < start) {
			for (Map.Entry<Integer, SampledValue> n : nextValues.entrySet()) {
				SampledValue sv = n.getValue();
				if (sv == null)
					continue;
				if (sv.getTimestamp() < start) 
					nextPrevious.put(n.getKey(), sv);
			}
			super.advance();
			if (comingValues.isEmpty())
				return;
			firstT = comingValues.values().iterator().next().getTimestamp();
		}
		nextTargetTime = start;
		if (firstT > start)  { // might be equal, too
// 				final Map<Integer, SampledValue> syntheticPoint = new HashMap<Integer, SampledValue>(iterators.size());
			for (int i=0;i<iterators.size();i++) {
 				SampledValue n = comingValues.remove(i);
 				if (n != null) {
 					nextValues.put(i, n);
 				} else {
 					n = nextValues.get(i);
 				}
				SampledValue p = nextPrevious.get(i);
				final SampledValue c = interpolate(start, p, n, 
						(globalMode != null ? globalMode : modes != null ? modes.get(i) : InterpolationMode.LINEAR));
				if (c != null)
					comingValues.put(i, c);
			}
		} else {
			for (int i=0;i<iterators.size();i++) {
				if (comingValues.containsKey(i)) {
					Iterator<SampledValue> it = iterators.get(i);
					if (it.hasNext()) {
						SampledValue n = it.next();
						nextValues.put(i, n);
					}
					continue;
				}
				SampledValue n = nextValues.get(i);
				SampledValue p = previousValues.get(i);
				final SampledValue c = interpolate(start, p, n, (globalMode != null ? globalMode : modes.get(i)));
				if (c != null)
					comingValues.put(i, c);
			}
		}
		nextNext.putAll(nextValues);
	}
	
}
