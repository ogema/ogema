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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import org.ogema.core.channelmanager.measurements.FloatValue;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.api.InterpolationFunction;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.interpolation.LinearInterpolation;
import org.ogema.tools.timeseries.interpolation.NearestInterpolation;
import org.ogema.tools.timeseries.interpolation.NoInterpolation;
import org.ogema.tools.timeseries.interpolation.StepInterpolation;

/**
 * TimeSeries implementation internally based on a sorted tree structure. The implementation is not thread-safe. Wrap it
 * with a {@link SynchronizedTimeSeries} if you need thread safety.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class TreeTimeSeries implements MemoryTimeSeries {

	private final Class<? extends Value> m_type;
	private final NavigableSet<SampledValue> m_values = new TreeSet<>();
	private InterpolationFunction m_interpolationFunction = new NoInterpolation();
	private InterpolationMode m_interpolationMode = InterpolationMode.NONE;
	private Long m_lastCalculationTime = null;

	public TreeTimeSeries(Class<? extends Value> valueType) {
		this.m_type = valueType;
	}

	/**
	 * Copy-constructor from another time series. Note that the TimeSeries interface provides no means of telling the
	 * user what the actual data type is, so it must be provided explicitly.
	 */
	public TreeTimeSeries(ReadOnlyTimeSeries other, Class<? extends Value> type) {
		this.m_type = type;
		addValues(other.getValues(Long.MIN_VALUE));
		setInterpolationMode(other.getInterpolationMode());
	}

	/**
	 * Gets the element corresponding to time t.
	 * 
	 * @param t
	 *            Time for which the value is requested for.
	 * @return Element associated with time; returns null if no element is associated to the particular timestamp.
	 */
	protected SampledValue getElement(long t) {
		final SortedSet<SampledValue> values = getSubset(t, t + 1);
		return values.isEmpty() ? null : values.first();
	}

	protected SortedSet<SampledValue> getValues() {
		return Collections.unmodifiableSortedSet(m_values);
	}

	/**
	 * Gets the interpolation operator
	 */
	protected final InterpolationFunction getInterpolationFunction() {
		return m_interpolationFunction;
	}

	/**
	 * Gets the subset of entries in the time interval [t0;t1)
	 * 
	 * @param t0
	 *            left border of the interval
	 * @param t1
	 *            right border of the interval - non-inclusive
	 * @return Set to all elements in the interval.
	 */
	protected SortedSet<SampledValue> getSubset(long t0, long t1) {
		final SampledValue from = new SampledValue(null, t0, Quality.BAD);
		final SampledValue to = new SampledValue(null, t1, Quality.BAD);
		final SortedSet<SampledValue> result = m_values.subSet(from, to);
		return result;
	}

	@Override
	final public SampledValue getValue(long time) {
        if (!isInsideTimeSeriesRange(time)){
            return null;
        }
		final SampledValue pivot = new SampledValue(null, time, Quality.GOOD);
		final SampledValue left = m_values.floor(pivot);
		final SampledValue right = m_values.ceiling(pivot);
		return m_interpolationFunction.interpolate(left, right, time, m_type);
	}
    
        /**
         * Checks if a given timestamp is in the range generally covered by the 
         * schedule (irrespective of the value qualities).
         */
    private boolean isInsideTimeSeriesRange(long timestamp){
        if (m_values.isEmpty()) return false;
        final long tmin = m_values.first().getTimestamp();
        final long tmax = m_values.last().getTimestamp();
        switch (m_interpolationMode) {
            case NEAREST:
                return true; // since there is at least one point there is alwayst a nearest one.
            case STEPS:
                return (timestamp>=tmin);
            case NONE:
            case LINEAR:                
                return ((timestamp>=tmin) && (timestamp<=tmax));
            default:
                throw new UnsupportedOperationException("Unsupported interpolation mode encountered: "+m_interpolationMode.toString());
        }
    }

	@Override
	final public SampledValue getNextValue(long time) {
		final SampledValue pivot = new SampledValue(null, time, Quality.BAD);
		final SampledValue element = m_values.ceiling(pivot);
		return (element != null) ? element.copyDefensively() : null;
	}

	@Override
	final public Long getLastCalculationTime() {
		return m_lastCalculationTime;
	}

	@Override
	public void addValue(SampledValue value) {
		final SampledValue oldElement = getElement(value.getTimestamp());
		if (oldElement != null)
			m_values.remove(oldElement);
		m_values.add(value);
	}

	@Override
	public boolean addValue(long timestamp, Value value) {
		addValue(new SampledValue(value, timestamp, Quality.GOOD));
		return true;
	}

	@Override
	public boolean addValue(long timestamp, Value value, long timeOfCalculation) {
		addValue(timestamp, value);
		m_lastCalculationTime = timeOfCalculation;
		return true;
	}

	// used in a constructor -> final
	@Override
	public final boolean addValues(Collection<SampledValue> values) {
		if (values.isEmpty())
			return true;
		if (m_values.isEmpty()) { // no need to delete old values in this case
			m_values.addAll(values);
			return true;
		}
		final SortedSet<SampledValue> copy;
		if (values instanceof SortedSet) {
			copy = (SortedSet<SampledValue>) values;
		} else {
			copy = new TreeSet<>(values);
		}
		
		SampledValue first = copy.first();
		SampledValue last = copy.last();
		if (!m_values.subSet(first, true, last, true).isEmpty()) {
			deleteByTimestamps(copy);
		}
		m_values.addAll(values);
		return true;
	}

	@Override
	public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
		addValues(values);
		m_lastCalculationTime = timeOfCalculation;
		return true;
	}

	/**
	 * Delete the subset of values in m_vlaue given by elements.
	 * 
	 * @param elements
	 *            subset of m_values that will be removed from the sorted set.
	 */
//	private void deleteValues(SortedSet<SampledValue> elements) {
//		while (!elements.isEmpty()) {
//			m_values.remove(elements.first());
//		}
//	}

	/**
	 * 
	 */
	@Override
	public boolean deleteValues() {
		m_values.clear();
		return true;
	}

	@Override
	final public boolean deleteValues(long endTime) {
		final SampledValue max = new SampledValue(null, endTime, Quality.BAD);
//		deleteValues(m_values.headSet(max));
		m_values.headSet(max).clear();
		return true;
	}

	@Override
	final public boolean deleteValues(long startTime, long endTime) {
		final SampledValue min = new SampledValue(null, startTime, Quality.BAD);
		final SampledValue max = new SampledValue(null, endTime, Quality.BAD);
//		deleteValues(m_values.subSet(min, max));
		m_values.subSet(min, max).clear();
		return true;
	}
	
	protected boolean deleteByTimestamps(Collection<SampledValue> points) {
		long t;
		for (SampledValue sv : points) {
			t = sv.getTimestamp();
			getSubset(t, t+1).clear();
		}
		return true;
	}

	@Override
	public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		deleteValues(startTime, endTime);
		if (values == null) values = Collections.emptyList();
		addValues(values);
		return true;
	}

	@Override
	final public boolean setInterpolationMode(InterpolationMode mode) {
		m_interpolationMode = mode;
		switch (mode) {
		case NONE:
			m_interpolationFunction = new NoInterpolation();
			break;
		case LINEAR:
			m_interpolationFunction = new LinearInterpolation();
			break;
		case NEAREST:
			m_interpolationFunction = new NearestInterpolation();
			break;
		case STEPS:
			m_interpolationFunction = new StepInterpolation();
			break;
		default:
			throw new UnsupportedOperationException("Interpolation mode " + mode + " not supported.");
		}
		return true;
	}

	@Override
	final public InterpolationMode getInterpolationMode() {
		return m_interpolationMode;
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		SortedSet<SampledValue> elements = m_values.tailSet(new SampledValue(null, startTime, Quality.BAD));
		return getValuesInternal(elements);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		SortedSet<SampledValue> elements = getSubset(startTime, endTime);
		return getValuesInternal(elements);
	}

	protected List<SampledValue> getValuesInternal(SortedSet<SampledValue> elements) {
		final List<SampledValue> result;
		if (elements.isEmpty())
			return new ArrayList<>();
		SampledValue first = elements.first();
		if (first.copyDefensively() == first) { // check if cloning of values is required
			result = new ArrayList<>(elements);  // should be faster than below method
		}
		else {
			result = new ArrayList<>(elements.size());
			for (SampledValue value : elements)
				result.add(value.copyDefensively());
		}
		return result;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		return m_type;
	}

	@Override
	public void write(TimeSeries schedule) {
		final List<SampledValue> values = new ArrayList<>(m_values);
		schedule.replaceValues(0, Long.MAX_VALUE, values);
		schedule.setInterpolationMode(this.getInterpolationMode());
	}
	
	@Override
	public void write(TimeSeries schedule, long from, long to) {
		final List<SampledValue> values = new ArrayList<>(m_values);
		schedule.replaceValues(from, to, values);
		schedule.setInterpolationMode(this.getInterpolationMode());
	}

	@Override
	public TreeTimeSeries read(ReadOnlyTimeSeries schedule) {
		m_values.clear();
		final List<SampledValue> newValues = schedule.getValues(Long.MIN_VALUE);
		for (SampledValue value : newValues) {
			m_values.add(value.copyDefensively());
		}
		setInterpolationMode(schedule.getInterpolationMode());
		return this;
	}

	@Override
	public TreeTimeSeries read(ReadOnlyTimeSeries schedule, long start, long end) {
		m_values.clear();
		final List<SampledValue> newValues = schedule.getValues(start, end);
		for (SampledValue value : newValues) {
			m_values.add(value.copyDefensively());
		}
		setInterpolationMode(schedule.getInterpolationMode());

		return this;
	}
	
	@Override
	public TreeTimeSeries readWithBoundaries(ReadOnlyTimeSeries schedule, long start, long end) {
		m_values.clear();
		setInterpolationMode(schedule.getInterpolationMode());
		if (end < start)
			return this;
		if (end == start) {
			SampledValue sv = schedule.getValue(start);
			if (sv != null)
				m_values.add(new SampledValue(sv));
			return this;
		}
		final List<SampledValue> newValues = schedule.getValues(start, end);
		if (newValues.isEmpty() || start < newValues.get(0).getTimestamp()) {
			SampledValue sv = schedule.getValue(start);
			if (sv != null)
				m_values.add(sv.copyDefensively());
		}
		for (SampledValue value : newValues) {
			m_values.add(value.copyDefensively());
		}
		SampledValue sv = schedule.getValue(end);
		if (sv != null)
			m_values.add(sv.copyDefensively());
		return this;
	}

	@Override
	public void shiftTimestamps(long dt) {

		final NavigableSet<SampledValue> shiftedValues = new TreeSet<>();
		for (SampledValue value : m_values) {
			final long t = value.getTimestamp() + dt;
			// if (t<0) continue;
			final SampledValue newValue = new SampledValue(value.getValue(), t, value.getQuality());
			shiftedValues.add(newValue);
		}
		m_values.clear();
		m_values.addAll(shiftedValues);
	}

	// this involves copying all sampled values
	@Override
	public MemoryTimeSeries clone() {
		return new TreeTimeSeries(this, m_type);
	}

	@Override
    @Deprecated
	public Long getTimeOfLatestEntry() {
		return null;
	}

    @Override
    public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		final long endTime = startTime + stepSize * values.size();
		deleteValues(startTime, endTime);
		long t = startTime;
		for (Value value : values) {
			addValue(t, value);
			t += stepSize;
		}
		return true;       
    }

    @Override
    public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize, long timeOfCalculation) {
            replaceValuesFixedStep(startTime, values, stepSize);
		m_lastCalculationTime = timeOfCalculation;
		return true;
    }


	@Override
    @Deprecated
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
            return replaceValuesFixedStep(startTime, values, stepSize);
	}

	@Override
    @Deprecated
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
            return replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
	}

    @Override
    public SampledValue getValueSecure(long t) {
        final SampledValue result = getValue(t);
        return (result!=null) ? result : new SampledValue(new FloatValue(0.f), t, Quality.BAD);
    }
    
    @Override
    public SampledValue getPreviousValue(long time) {
    	final SampledValue pivot = new SampledValue(null, time, Quality.BAD);
		final SampledValue element = m_values.floor(pivot);
		return (element != null) ? element.copyDefensively() : null;
    }

	@Override
	public boolean isEmpty() {
		return m_values.isEmpty();
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		SampledValue next = getNextValue(startTime);
		return (next == null || next.getTimestamp() > endTime);
	}

	@Override
	public int size() {
		return m_values.size();
	}

	@Override
	public int size(long startTime, long endTime) {
		return m_values.subSet(new SampledValue(null, startTime, Quality.BAD), true, new SampledValue(null, endTime, Quality.BAD), true).size();
	}

	// TODO check: any problems with this proxy approach?
	@Override
	public Iterator<SampledValue> iterator() {
		return m_values.iterator(); 
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return m_values.subSet(new SampledValue(null, startTime, Quality.BAD), true, new SampledValue(null, endTime, Quality.BAD), true).iterator();
	}
    
}
