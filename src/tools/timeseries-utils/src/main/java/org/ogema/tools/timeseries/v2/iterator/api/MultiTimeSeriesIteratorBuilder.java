/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.v2.iterator.impl.TimeSeriesMultiIteratorImpl;
import org.ogema.tools.timeseries.v2.iterator.impl.TimeSeriesMultiIteratorImplDiff;
import org.ogema.tools.timeseries.v2.iterator.impl.TimeSeriesMultiIteratorImplIntegrating;
import org.ogema.tools.timeseries.v2.iterator.impl.TimeSeriesMultiIteratorImplStepRuler;
import org.ogema.tools.timeseries.v2.iterator.impl.TimeSeriesMultiIteratorImplStepRulerIntegrating;
import org.ogema.tools.timeseries.v2.iterator.impl.TimeSeriesMultiIteratorImplStepSize;

/**
 * Create new {@link MultiTimeSeriesIterator}s. 
 */
public class MultiTimeSeriesIteratorBuilder {
	
	private final List<Iterator<SampledValue>> iterators;
	// may be null
	private Map<Integer, SampledValue> lowerBoundaryValues;
	// may be null
	private Map<Integer, SampledValue> upperBoundaryValues;
	private int maxNrHistoricalValues;
	// a value between 0 and 1. 0 means that values are associated to the earlier timestamp,
	// 1 to the later timestamp, otherwise it is interpolated according to the value. 
	private double timeInterpolation = 0.5F;
	private boolean doAverage = false;
	private boolean doIntegrate = false;
	private boolean doDiff = false;
	
	// fixed step size
	private Long stepSize = null;
	// only evaluated in conjunction with stepSize
	private Long startTime = null; 
	private int[] stepRulers = null;
	private InterpolationMode globalMode = null;
	private List<InterpolationMode> modes = null;

	private MultiTimeSeriesIteratorBuilder(List<Iterator<SampledValue>> iterators) {
		Objects.requireNonNull(iterators);
		this.iterators = new ArrayList<>(iterators);
	}

	/**
	 * @param iterators
	 * 		An ordered collection
	 * @return
	 */
	public static MultiTimeSeriesIteratorBuilder newBuilder(List<Iterator<SampledValue>> iterators) {
		return new MultiTimeSeriesIteratorBuilder(iterators);
	}
	
	public MultiTimeSeriesIterator build() {
		final MultiTimeSeriesIterator it = getIterator();
		if (doDiff)
			return new TimeSeriesMultiIteratorImplDiff(it, timeInterpolation);
		else
			return it;
	}
	
	private final MultiTimeSeriesIterator getIterator() {
		if (startTime != null)
			return new TimeSeriesMultiIteratorImplStepSize(iterators, maxNrHistoricalValues, 
					lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate, stepSize, startTime);
		if (stepRulers != null) {
			if (doAverage || doIntegrate)
				return new TimeSeriesMultiIteratorImplStepRulerIntegrating(iterators, maxNrHistoricalValues, 
						lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate, stepRulers);
			return new TimeSeriesMultiIteratorImplStepRuler(iterators, maxNrHistoricalValues, 
					lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate, stepRulers);
		}
		if (doAverage || doIntegrate)
			return new TimeSeriesMultiIteratorImplIntegrating(iterators, maxNrHistoricalValues, 
					lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate);
		return new TimeSeriesMultiIteratorImpl(iterators, maxNrHistoricalValues, 
					lowerBoundaryValues, upperBoundaryValues, globalMode, modes, doAverage, doIntegrate);
	}
	
//	private final Long getActualStartTime() {
//		if (!doDiff || startTime == null || stepSize == null)
//			return startTime;
//		final long half = stepSize / 2;
//		if (Long.MIN_VALUE + half <= startTime)
//			return startTime - half;
//		return startTime + half;
//	}
	
	/**
	 * Default value: null
	 * @param lowerBoundaryValues
	 */
	public MultiTimeSeriesIteratorBuilder setLowerBoundaryValues(Map<Integer, SampledValue> lowerBoundaryValues) {
		this.lowerBoundaryValues = (lowerBoundaryValues != null ? new HashMap<>(lowerBoundaryValues) : null);
		return this;
	}

	/**
	 * Default value: null
	 * @param upperBoundaryValues
	 */
	public MultiTimeSeriesIteratorBuilder setUpperBoundaryValues(Map<Integer, SampledValue> upperBoundaryValues) {
		this.upperBoundaryValues = (upperBoundaryValues != null ? new HashMap<>(upperBoundaryValues) : null);
		return this;
	}

	/**
	 * Default value: 0
	 * @param maxNrHistoricalValues
	 */
	public MultiTimeSeriesIteratorBuilder  setMaxNrHistoricalValues(int maxNrHistoricalValues) {
		if (maxNrHistoricalValues < 0)
			throw new IllegalArgumentException("maxNrHistoricalValues must not be negative. Got " + maxNrHistoricalValues);
		this.maxNrHistoricalValues = maxNrHistoricalValues;
		return this;
	}
	
	/**
	 * If stepRulers is set to a non-null value, the step size of the iteration will be determined by the points of the iterators
	 * corresponding to the indices in stepRulers. Other schedule values will be interpolated based on the configured interpolation mode. 
	 * @param stepRulers
	 */
	public MultiTimeSeriesIteratorBuilder  stepSizeAsInSchedules(int[] stepRulers) {
		this.stepRulers = stepRulers;
		this.stepSize = null;
		this.startTime = null;
		return this;
	}
	
	/**
	 * Set a constant time interval for the iteration. Data points will be generated from all schedules based on a common 
	 * interpolation mode
	 * @param startTime
	 * 		Used for alignment of time stamps only; even time stamps before this value will be processed.
	 * @param stepSize
	 */
	public MultiTimeSeriesIteratorBuilder setStepSize(long startTime, long stepSize) {
		if (stepSize <= 0)
			throw new IllegalArgumentException("Step size must be positive, got " + stepSize);
		this.stepSize = stepSize;
		this.startTime = startTime;
		this.stepRulers = null;
		return this;
	}

	/**
	 * If this is set to true, then the values returned by the iterator will be obtained by averaging over the past interval 
	 * (the interval between two data points). 
	 * @param average
	 */
	public MultiTimeSeriesIteratorBuilder doAverage(boolean average) {
		this.doAverage = average; 
		if (average) {
			doIntegrate = false;
			doDiff = false;
		}
		return this;
	}
	
	/**
	 * If this is set to true, then the values returned by the iterator will be obtained by integrating over the past interval 
	 * (the interval between two data points). Note that the integral is taken w.r.t. milliseconds, i.e. to obtain 
	 * a result in the unit 'seconds', divide the resulting values by 1000.
	 * @param integrate
	 */
	public MultiTimeSeriesIteratorBuilder doIntegrate(boolean integrate) {
		this.doIntegrate = integrate;
		if (integrate) {
			doAverage = false;
			doDiff = false;
		}
		return this;
	}
	
	/**
	 * If this is set to true, then the values returned by the iterator will be obtained by subtracting the 
	 * value at the start of the past interval from the value at the end of the interval.
	 * 
	 * See also {@link #doDiff(boolean, double)}, which allows to control the timestamp to which a difference value
	 * will be associated.
	 * @param diff
	 */
	public MultiTimeSeriesIteratorBuilder doDiff(boolean diff) {
		return doDiff(diff, timeInterpolation);
	}
	
	/**
	 * If this is set to true, then the values returned by the iterator will be obtained by subtracting the 
	 * value at the start of the past interval from the value at the end of the interval.
	 * @param diff
	 * @param timeInterpolation typically a value between 0 and 1. 0 means that values are associated to the earlier timestamp,
	 * 		 1 to the later timestamp, an intermediate value leads to an interpolated timestamp. Default is 0.5.
	 */
	public MultiTimeSeriesIteratorBuilder doDiff(boolean diff, double timeInterpolation) {
		this.doDiff = diff;
		this.timeInterpolation = timeInterpolation;
		if (diff) {
			doAverage = false;
			doIntegrate = false;
		}
		return this;
	}
	
	/**
	 * Set a common interpolation mode for all time series in iterations that need interpolation 
	 * (e.g. if the values are averaged, or fixed step size is configured).
	 * @param mode
	 */
	public MultiTimeSeriesIteratorBuilder setGlobalInterpolationMode(InterpolationMode mode) {
		this.globalMode = mode;
		this.modes = null;
		return this;
	}
	
	/**
	 * Set an interpolation mode per time series in iterations that need interpolation 
	 * (e.g. if the values are averaged, or fixed step size is configured).
	 * @param modes
	 */
	public MultiTimeSeriesIteratorBuilder setIndividualInterpolationModes(List<InterpolationMode> modes) {
		this.modes = new ArrayList<>(modes);
		this.globalMode = null;
		return this;
	}
	
}
