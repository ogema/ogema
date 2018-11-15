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
package org.ogema.tools.timeseries.implementations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.algebra.BilinearFloatAddition;
import org.ogema.tools.timeseries.algebra.BiliniearFloatMultiplication;
import org.ogema.tools.timeseries.algebra.ConstantFloatAddition;
import org.ogema.tools.timeseries.algebra.ConstantFloatMultiplication;
import org.ogema.tools.timeseries.api.BilinearSampledValueOperator;
import org.ogema.tools.timeseries.api.FloatTimeSeries;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.LinearSampledValueOperator;
import org.ogema.tools.memoryschedules.tools.TimeSeriesMerger;
import org.ogema.tools.timeseries.api.TimeInterval;
import org.slf4j.LoggerFactory;

/**
 * Implementation for the FloatTimeSeries internally using a tree structure for
 * storing data.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FloatTreeTimeSeries extends TreeTimeSeries implements FloatTimeSeries {

	public FloatTreeTimeSeries() {
		super(FloatValue.class);
	}

	/**
	 * Creates a series that has the constant value for all times.
	 */
	public FloatTreeTimeSeries(float value) {
		super(FloatValue.class);
		setConstant(value);
	}

	/**
	 * Copy constructor.
	 */
	public FloatTreeTimeSeries(FloatTimeSeries other) {
		super(other.getValueType());
		addValues(other.getValues(Long.MIN_VALUE));
		setInterpolationMode(other.getInterpolationMode());
	}

	/**
	 * Apply the bi-linear operator on all points of this. Second operator input
	 * is taken from values. Store the values in this.
	 */
	public void applyBilinearOperator(BilinearSampledValueOperator operator, ReadOnlyTimeSeries values) {
//        final FloatTimeSeries factors = new FloatTreeTimeSeries(values); 
        final TimeSeriesMerger merger = new TimeSeriesMerger(this, values);
        // calculate the new values for this.
        final List<SampledValue> newValues = new ArrayList<>(merger.getTimestamps().size());
        for (Long t : merger.getTimestamps()) {
            final SampledValue v1 = this.getValueSecure(t);
            final SampledValue v2 = getValueSecure(values, t);
            final SampledValue newValue = operator.apply(v1, v2);
            newValues.add(newValue);
        }

        // replace values in this with new values and set interpolation mode.
        deleteValues();
        addValues(newValues);
        setInterpolationMode(merger.getInterpolationMode());
    }

	/**
	 * Apply an operator V-&gt;V to all points in this time series.
	 *
	 * @param operator
	 */
	public void applyLinearOperator(LinearSampledValueOperator operator) {
        final SortedSet<SampledValue> values = getValues();
        final List<SampledValue> newValues = new ArrayList<>(values.size());
        for (SampledValue value : values) {
            final SampledValue newValue = operator.apply(value);
            newValues.add(newValue);
        }
        deleteValues();
        addValues(newValues);
    }

	@Override
	public synchronized void multiplyBy(float factor) {
		LinearSampledValueOperator operator = new ConstantFloatMultiplication(factor);
		applyLinearOperator(operator);
	}

	@Override
	public synchronized void add(float addend) {
		LinearSampledValueOperator operator = new ConstantFloatAddition(addend);
		applyLinearOperator(operator);
	}

	@Override
	public synchronized void add(ReadOnlyTimeSeries addends) {
		final BilinearSampledValueOperator operator = new BilinearFloatAddition();
		applyBilinearOperator(operator, addends);
	}

	@Override
	public FloatTreeTimeSeries read(ReadOnlyTimeSeries schedule) {
		return (FloatTreeTimeSeries) super.read(schedule);
	}

	@Override
	public FloatTreeTimeSeries read(ReadOnlyTimeSeries schedule, long start, long end) {
		return (FloatTreeTimeSeries) super.read(schedule, start, end);
	}

	@Override
	public synchronized void multiplyBy(ReadOnlyTimeSeries factor) {
		final BilinearSampledValueOperator operator = new BiliniearFloatMultiplication();
		applyBilinearOperator(operator, factor);
	}

	@Override
	public FloatTimeSeries plus(float addend) {
		final FloatTimeSeries result;
		synchronized (this) {
			result = new FloatTreeTimeSeries(this);
		}
		result.add(addend);
		return result;
	}

	@Override
	public FloatTimeSeries plus(FloatTimeSeries other) {
		final FloatTimeSeries result;
		synchronized (this) {
			result = new FloatTreeTimeSeries(this);
		}
		result.add(other);
		return result;
	}

	@Override
	public FloatTimeSeries times(float factor) {
		final FloatTimeSeries result;
		synchronized (this) {
			result = new FloatTreeTimeSeries(this);
		}
		result.multiplyBy(factor);
		return result;
	}

	@Override
	public FloatTimeSeries times(ReadOnlyTimeSeries other) {
		final FloatTimeSeries result;
		synchronized (this) {
			result = new FloatTreeTimeSeries(this);
		}
		result.multiplyBy(other);
		return result;
	}

	@Override
	public synchronized float integrate(long t0, long t1) {

		// react to zero range or t1<t0.
		if (t1 == t0) {
			return 0.f;
		}
		if (t1 < t0) {
			return -integrate(t1, t0);
		}

		final InterpolationFunction function = getInterpolationFunction();

		double result = 0;
		SampledValue left = getValueSecure(t0);
		for (SampledValue right : getSubset(t0, t1)) {
			result += function.integrate(left, right, getValueType()).getDoubleValue();
			left = right;
		}
		SampledValue right = getValueSecure(t1);
		result += function.integrate(left, right, getValueType()).getDoubleValue();

		return (float) result;
	}

	@Override
	public synchronized SampledValue getMax(long t0, long t1) {
		final long dt = t1 - t0;
		if (dt <= 0) {
			return new SampledValue(new FloatValue(0.f), t0, Quality.BAD);
		}

		final List<SampledValue> values = getValues(t0, t1);
		values.add(getValue(t0)); 
		if (dt > 1) {
			values.add(getValue(t1 - 1));
		}

		// result values.
		float max = Float.MIN_VALUE;
		long t = t0;
		Quality q = Quality.BAD;

		for (SampledValue value : values) {
			if (value == null || value.getQuality() == Quality.BAD) {
				continue;
			}
			final float x = value.getValue().getFloatValue();
			if (x > max) {
				max = x;
				t = value.getTimestamp();
				q = value.getQuality();
			}
		}
		SampledValue result = new SampledValue(new FloatValue(max), t, q);

		if (getInterpolationMode() == InterpolationMode.NEAREST) {
			// Problem: would have to set the timestamp between the support points
//			throw new UnsupportedOperationException("Case InterpolationMode.NEAREST is not yet implemented for getMax");
			SampledValue previous = getPreviousValue(t);
			if (previous != null && previous.getQuality() == Quality.GOOD) {
				// might cause an overflow... not relevant for real timestamps, though
				long maxStamp = Math.max(t0, (t + previous.getTimestamp())/2 + 1);  
				result = getValue(maxStamp);
			}
		}
		return result;
	}
	
	@Override
	public synchronized SampledValue getMin(long t0, long t1) {
		final FloatTreeTimeSeries f = new FloatTreeTimeSeries(this);
		f.multiplyBy(-1.f);
		final SampledValue negMax = f.getMax(t0, t1);
		return new SampledValue(new FloatValue(negMax.getValue().getFloatValue()), negMax.getTimestamp(), negMax
				.getQuality());
	}

	/**
	 * Sets the values of the result such that the resulting function is a
	 * point-wise absolute magnitude copy of this.
	 */
	// XXX result is both argument and return value
	private FloatTimeSeries getAbsoluteLinear(final FloatTimeSeries result) {
		result.setInterpolationMode(InterpolationMode.LINEAR);
		SampledValue lastValue = getValues().first();
		for (SampledValue value : getValues()) {

			// shortcuts for last and this value's properties
			final float xLast = lastValue.getValue().getFloatValue();
			final long tLast = lastValue.getTimestamp();
			final Quality qLast = lastValue.getQuality();
			final float x = value.getValue().getFloatValue();
			final long t = value.getTimestamp();
			final Quality q = value.getQuality();

			if (Float.isNaN(x)) {
				throw new RuntimeException("Could not get the absolute of a function: An entry is NaN.");
			}

			// if this and the last value differ in sign, add an intermediate value.
			if (xLast * x < 0.) {
				final Quality qMid = (q == Quality.GOOD && qLast == Quality.GOOD) ? Quality.GOOD : Quality.BAD;

				final float slope = (x - xLast) / (float) (t - tLast);
				// xLast + slope*delta = 0 => delta = -xLast/slope
				final float delta = -xLast / slope;
				final long tMid = tLast + (long) delta;
				if (tMid != tLast && tMid != t) {
					result.addValue(new SampledValue(new FloatValue(0.f), tMid, qMid));
				}
			}

			// add the absolute of the current value
			result.addValue(new SampledValue(new FloatValue(Math.abs(x)), t, q));

			lastValue = value;
		}
		return result;
	}

	@Override
	public FloatTimeSeries getAbsolute() {
		final FloatTimeSeries result = new FloatTreeTimeSeries();
		result.setInterpolationMode(getInterpolationMode());
		if (getValues().isEmpty()) {
			return result;
		}

		if (getInterpolationMode() == InterpolationMode.LINEAR) {
			return getAbsoluteLinear(result);
		}
		for (SampledValue value : getValues()) {
			final float x = value.getValue().getFloatValue();
			if (x >= 0) {
				result.addValue(value);
			}
			else {
				result.addValue(new SampledValue(new FloatValue(-x), value.getTimestamp(), value.getQuality()));
			}
		}
		return result;
	}

	@Override
	public float integrateAbsolute(long t0, long t1) {
		final FloatTimeSeries absCopy = getAbsolute();
		return absCopy.integrate(t0, t1);
	}

	@Override
	public float integratePositive(TimeInterval interval) {
		List<TimeInterval> positiveDomains = getPositiveDomain(interval);
		float result = 0.f;
		for (TimeInterval subDomain : positiveDomains) {
			result += integrate(subDomain);
		}
		return result;
	}

	@Override
	public float integrate(TimeInterval interval) {
		return (interval.exists()) ? integrate(interval.getStart(), interval.getEnd()) : 0.f;
	}

	@Override
	public float integrateAbsolute(TimeInterval interval) {
		return (interval.exists()) ? integrate(interval.getStart(), interval.getEnd()) : 0.f;
	}

	@Override
	public float integratePositive(long t0, long t1) {
		return (t0 < t1) ? integratePositive(new TimeInterval(t0, t1)) : -integratePositive(new TimeInterval(t1, t0));
	}

	@Override
	public final void setConstant(float value) {
		deleteValues();
		setInterpolationMode(InterpolationMode.STEPS);
		addValue(Long.MIN_VALUE, new FloatValue(value));
		addValue(0, new FloatValue(value));
	}

	/**
	 * Constrains a collection of intervals to a given domain and then joins
	 * adjacent intervals. Empty intervals are discarded.
	 *
	 * @return list of constrained and joined intervals
	 */
	private List<TimeInterval> cleanupIntervalList(Collection<TimeInterval> candidates, TimeInterval searchInterval) {
        final List<TimeInterval> result = new ArrayList<>();
        TimeInterval currentInterval = new TimeInterval(Long.MIN_VALUE, Long.MIN_VALUE);
        for (TimeInterval candidate : candidates) {
            if (currentInterval.getEnd() == candidate.getStart()) {
                currentInterval = new TimeInterval(currentInterval.getStart(), candidate.getEnd());
            } else {
                final TimeInterval combinedInterval = currentInterval.intersect(searchInterval);
                if (combinedInterval.exists()) {
                    result.add(combinedInterval);
                }
                currentInterval = candidate;
            }
        }
        final TimeInterval combinedInterval = currentInterval.intersect(searchInterval);
        if (combinedInterval.exists()) {
            result.add(combinedInterval);
        }
        return result;
    }

	@Override
    public List<TimeInterval> getPositiveDomain(TimeInterval searchInterval) {

        if (getInterpolationMode() == InterpolationMode.NEAREST) {
            throw new UnsupportedOperationException("Method not implemented for interpolation mode NEAREST, yet.");
        }
        
        final InterpolationFunction interpolation = getInterpolationFunction();
        // get pairs or co-joint intervals (SampledValue1, Sampledvalue2) and create initial list of intervals
        SortedSet<TimeInterval> candidates = new TreeSet<>();
        SampledValue lastValue = getValueSecure(searchInterval.getStart());
        // TODO interpolation mode NEAREST would require checking an initial dummy against the first entry - and check that values are not empty        
        for (SampledValue value : getValues()) {
            TimeInterval candidate = interpolation.getPositiveInterval(lastValue, value, getValueType());
            if (!candidate.isEmpty()) {
                candidates.add(candidate);
            }
            lastValue = value;
        }
        
//        // need to explicitly check last entry for interpolation modes other than linear
//        if (getInterpolationMode() != InterpolationMode.LINEAR) {
//            // type does not matter: implementation does not use value of 2nd type.
            SampledValue lastDummy = getValueSecure(searchInterval.getEnd());
            TimeInterval candidate = interpolation.getPositiveInterval(lastValue, lastDummy, getValueType());
            if (!candidate.isEmpty()) {
                candidates.add(candidate);
            }            
//        }

        // clean up the result and return it
        return cleanupIntervalList(candidates, searchInterval);
    }

	@Override
	public void optimizeRepresentation() {
	}

	@Override
	public float getAverage(long t0, long t1) {
		if (t1 == t0) {
			SampledValue sv = getValue(t0);
			if (sv == null || sv.getQuality() == Quality.BAD)
				return Float.NaN;
			else 
				return sv.getValue().getFloatValue();
		}
		if (getInterpolationMode() == InterpolationMode.NONE) {
			int count = 0;
			float val = 0;
			List<SampledValue> values;
			if (t0 < t1)
				values = getValues(t0, t1);
			else 
				values = getValues(t1, t0);
			for (SampledValue sv: values) {
				if (sv.getQuality() != Quality.BAD) {
					count++;
					val += sv.getValue().getFloatValue();
				}
			}
			if (count == 0)
				return Float.NaN;
			else 
				return val / count; 
		}
		return integrate(t0,t1) / (t1 - t0);
	}

	@Override
	public List<SampledValue> downsample(long t0, long t1, long minimumInterval) {	
		List<SampledValue> newValues = new ArrayList<SampledValue>();
		if (t1 < t0) 
			return newValues;
		List<SampledValue> values = getValues(t0, t1);
//		List<SampledValue> oldValues = new ArrayList<SampledValue>();
//		if (values.isEmpty() || t0 < values.get(0).getTimestamp()) {
//			SampledValue sv = getValue(t0);
//			if (sv != null)
//				oldValues.add(sv);
//		}
//		oldValues.addAll(values);
//		SampledValue sv = getValue(t1);
//		if (t1 > t0 && sv != null) 
//			oldValues.add(sv);
//		// split into sub intervals
//		long lastT = Long.MIN_VALUE;
//		Quality lastQuality = Quality.GOOD;
//		InterpolationMode mode = getInterpolationMode();
//		List<SampledValue> currentList = new ArrayList<SampledValue>();
//		for (int i = 0;i<oldValues.size(); i++) {
//			SampledValue sv0 = oldValues.get(i);
//			long ta = sv.getTimestamp();
//			Quality qual = sv.getQuality();
//			if (qual != lastQuality || (qual == Quality.GOOD && t0 - lastT >= minimumInterval)) {
		InterpolationMode mode = getInterpolationMode();
		return downsample(t0, t1, minimumInterval, values, getValue(t0), getValue(t1),mode);
	}
	
	static List<SampledValue> downsample(long t0, long t1, long minimumInterval, List<SampledValue> values, SampledValue sv0, SampledValue sv1, InterpolationMode mode) {
		List<SampledValue> newValues = new ArrayList<SampledValue>();
		if (t1 < t0) 
			return newValues;
		List<SampledValue> oldValues = new ArrayList<SampledValue>();
		if (mode != InterpolationMode.NONE && (values.isEmpty() || t0 < values.get(0).getTimestamp())) {  // we need the boundary points for the integration
			if (sv0 != null)
				oldValues.add(sv0);
		}
		oldValues.addAll(values);
		if (t1 > t0 && sv1 != null) 
			oldValues.add(sv1);
		// split into sub intervals
		long lastT = Long.MIN_VALUE;
		Quality lastQuality = Quality.GOOD;
		List<SampledValue> currentList = new ArrayList<SampledValue>();
		for (int i = 0;i<oldValues.size(); i++) {
			SampledValue sv = oldValues.get(i);
			long ta = sv.getTimestamp();
			Quality qual = sv.getQuality();
			if (qual != lastQuality || (qual == Quality.GOOD && ta - lastT >= minimumInterval)) {
				downsample(currentList, newValues, minimumInterval, mode);
				currentList.clear();
			}
			currentList.add(sv);
			lastQuality = qual;
			lastT = ta;
		}
		downsample(currentList, newValues, minimumInterval, mode);
		return newValues;
	}
	
	private static void downsample(List<SampledValue> oldSubset, List<SampledValue> newValues, long minInterval, InterpolationMode mode) {
		if (oldSubset.isEmpty())
			return;
		else if (oldSubset.size() == 1) {
			newValues.add(oldSubset.get(0));
			return;
		}
		if (mode == InterpolationMode.NONE) {
			downsampleNaive(oldSubset, newValues, minInterval);
		} else {
			downsample(oldSubset, newValues, minInterval);
		}
	}

	/**
	 * Downsampling based on the integration function in memory-timeseries... taking into account the interpolation mode
	 * We can assume that there are at least two data points in oldSubset
	 * 
	 * We always take the last data point equal to the last old one... such that in case of NEAREST or STEPS
	 * interpolation mode, the error introduced by downsampling is minimized.
	 * @param oldSubset
	 * @param newValues
	 */
	private static void downsample(List<SampledValue> oldSubset, List<SampledValue> newValues, long minInterval) {
		long t0 = oldSubset.get(0).getTimestamp();
		long t1 = oldSubset.get(oldSubset.size()-2).getTimestamp();
		Quality quality = oldSubset.get(0).getQuality(); // const
		long delta = t1-t0;
		int nr = (int) (delta/minInterval); 
		if (nr == 0)  
			nr = 1;  // in this case, the distance between the first and second (=last) point may be smaller than minInterval
		FloatTimeSeries fts = new FloatTreeTimeSeries();
		fts.addValues(oldSubset);
		for (int i=0;i<nr-1;i++) {
			float value = fts.integrate(t0 + i*minInterval, t0 + (i+1)*minInterval) / minInterval;
			if (Float.isNaN(value) || Float.isInfinite(value))
				LoggerFactory.getLogger(FloatTreeTimeSeries.class).warn("Downsampling led to a non-finite value: " + value + "; this may cause problems");
			newValues.add(new SampledValue(new FloatValue(value), t0 + i*minInterval, quality));
		}
		float value = fts.integrate(t0 + (nr-1)*minInterval, t1) / (t1 - t0 - (nr-1)*minInterval);
		newValues.add(new SampledValue(new FloatValue(value), t0 + (nr-1)*minInterval, quality));
		newValues.add(new SampledValue(oldSubset.get(oldSubset.size()-1)));
	}
	
	/**
	 * Subdivide the interval into parts of equal length, and simply take the average of all points in the
	 * target intervals, to calculate the new values. In this case, there is no interpolation information.
	 * @param oldSubset
	 * @param newValues
	 */
	private static void downsampleNaive(List<SampledValue> oldSubset, List<SampledValue> newValues, long minInterval) {
		float currentValue = 0;
		int nrCurrentElements = 0;
		SampledValue sv = oldSubset.get(0);
		long lastTs = sv.getTimestamp();
		for (int i = 0;i<oldSubset.size();i++) {
			sv = oldSubset.get(i);
			long t = sv.getTimestamp();
			if (t < lastTs + minInterval) { 
				currentValue += sv.getValue().getFloatValue();
				nrCurrentElements++;
			}
			else {
				currentValue = currentValue/nrCurrentElements;
				newValues.add(new SampledValue(new FloatValue(currentValue), lastTs, sv.getQuality()));
				lastTs = t;
				
				currentValue = sv.getValue().getFloatValue();
				nrCurrentElements = 1;
			}
			
		}
		currentValue = currentValue/nrCurrentElements;
		newValues.add(new SampledValue(new FloatValue(currentValue), lastTs, sv.getQuality()));
		
	}
	
	private final static SampledValue getValueSecure(ReadOnlyTimeSeries schedule, long t) {
		final SampledValue v = schedule.getValue(t);
		return (v != null ? v : new SampledValue(new FloatValue(Float.NaN), t, Quality.BAD));
	}
	
}
