/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.tools.timeseries.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

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
	private long m_lastCalculationTime = 0;

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

	/**
	 * Returns a reference to the values defining this. Would be a const-reference if Java supported it.
	 */
	protected SortedSet<SampledValue> getValues() {
		return m_values;
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
		if (t1 <= t0)
			return null;
		final SampledValue from = new SampledValue(null, t0, Quality.BAD);
		final SampledValue to = new SampledValue(null, t1, Quality.BAD);
		final SortedSet<SampledValue> result = m_values.subSet(from, to);
		return result;
	}

	@Override
	final public SampledValue getValue(long time) {
		final SampledValue pivot = new SampledValue(null, time, Quality.GOOD);
		final SampledValue left = m_values.floor(pivot);
		final SampledValue right = m_values.ceiling(pivot);
		return m_interpolationFunction.interpolate(left, right, time, m_type);
	}

	@Override
	final public SampledValue getNextValue(long time) {
		final SampledValue pivot = new SampledValue(null, time, Quality.BAD);
		final SampledValue element = m_values.ceiling(pivot);
		return (element != null) ? new SampledValue(element) : null;
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
		for (SampledValue value : values) {
			addValue(value);
		}
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
	private void deleteValues(SortedSet<SampledValue> elements) {
		while (!elements.isEmpty()) {
			m_values.remove(elements.first());
		}
	}

	@Override
	public boolean deleteValues() {
		m_values.clear();
		return true;
	}

	@Override
	final public boolean deleteValues(long endTime) {
		final SampledValue max = new SampledValue(null, endTime, Quality.BAD);
		deleteValues(m_values.headSet(max));
		return true;
	}

	@Override
	final public boolean deleteValues(long startTime, long endTime) {
		final SampledValue min = new SampledValue(null, startTime, Quality.BAD);
		final SampledValue max = new SampledValue(null, endTime, Quality.BAD);
		deleteValues(m_values.subSet(min, max));
		return true;
	}

	@Override
	public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		deleteValues(startTime, endTime);
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
		final List<SampledValue> result = new ArrayList<>(elements.size());
		for (SampledValue value : elements)
			result.add(new SampledValue(value));
		return result;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		SortedSet<SampledValue> elements = getSubset(startTime, endTime);
		final List<SampledValue> result = new ArrayList<>(elements.size());
		for (SampledValue value : elements)
			result.add(new SampledValue(value));
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
		final List<SampledValue> newValues = schedule.getValues(0);
		for (SampledValue value : newValues) {
			m_values.add(new SampledValue(value.getValue(), value.getTimestamp(), value.getQuality()));
		}
		setInterpolationMode(schedule.getInterpolationMode());

		return this;
	}

	@Override
	public TreeTimeSeries read(ReadOnlyTimeSeries schedule, long start, long end) {
		m_values.clear();
		final List<SampledValue> newValues = schedule.getValues(start, end);
		for (SampledValue value : newValues) {
			m_values.add(new SampledValue(value.getValue(), value.getTimestamp(), value.getQuality()));
		}
		setInterpolationMode(schedule.getInterpolationMode());

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

	@Override
	public MemoryTimeSeries clone() {
		return new TreeTimeSeries(this, m_type);
	}

	@Override
	public Long getTimeOfLatestEntry() {
		return new Long(-1);
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
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
            return replaceValuesFixedStep(startTime, values, stepSize);
	}

	@Override
	public final boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
            return replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
	}
    
}
