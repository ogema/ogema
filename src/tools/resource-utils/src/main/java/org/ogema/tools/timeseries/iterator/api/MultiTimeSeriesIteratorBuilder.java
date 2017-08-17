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
package org.ogema.tools.timeseries.iterator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImpl;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImplIntegrating;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImplStepRuler;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImplStepRulerIntegrating;
import org.ogema.tools.timeseries.iterator.impl.TimeSeriesMultiIteratorImplStepSize;

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
	private boolean doAverage = false;
	private boolean doIntegrate = false;
	
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
		if (average)
			doIntegrate = false;
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
		if (integrate)
			doAverage = false;
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
