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
import org.ogema.tools.memoryschedules.tools.SampledValueSortedList;

/**
 * TimeSeries implementation internally based on an array of sampled values. In a logging-like scenario where data are
 * always appended behind the existing ones this should have a better performance than the {@link TreeTimeSeries}.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ArrayTimeSeries implements MemoryTimeSeries {

	private final Class<? extends Value> m_type;
	private final SampledValueSortedList m_values = new SampledValueSortedList();
	private InterpolationFunction m_interpolationFunction = new NoInterpolation();
	private InterpolationMode m_interpolationMode = InterpolationMode.NONE;
	private long m_lastCalculationTime = 0;

	public ArrayTimeSeries(Class<? extends Value> type) {
		this.m_type = type;
	}

	/**
	 * Copy-constructor from another time series. Note that the TimeSeries interface provides no means of telling the
	 * user what the actual data type is, so it must be provided explicitly.
	 */
	public ArrayTimeSeries(ReadOnlyTimeSeries other, Class<? extends Value> type) {
		this.m_type = type;
		final List<SampledValue> values = other.getValues(Long.MIN_VALUE);
		addValues(values);
		setInterpolationMode(other.getInterpolationMode());
	}

	@Override
	public void write(TimeSeries schedule) {
		final List<SampledValue> values = m_values.getValues();
		schedule.replaceValues(0, Long.MAX_VALUE, values);
		schedule.setInterpolationMode(getInterpolationMode());
	}

	@Override
	public void write(TimeSeries schedule, long from, long to) {
		final List<SampledValue> values = m_values.getValues();
		schedule.replaceValues(from, to, values);
		schedule.setInterpolationMode(getInterpolationMode());
	}

	@Override
	public ArrayTimeSeries read(ReadOnlyTimeSeries schedule) {
		m_values.clear();
		final List<SampledValue> newValues = schedule.getValues(0);
		m_values.addValuesCopies(newValues);
		setInterpolationMode(schedule.getInterpolationMode());

		return this;
	}

	@Override
	public ArrayTimeSeries read(ReadOnlyTimeSeries schedule, long start, long end) {
		m_values.clear();
		final List<SampledValue> newValues = schedule.getValues(start, end);
		m_values.addValuesCopies(newValues);
		setInterpolationMode(schedule.getInterpolationMode());

		return this;
	}

	@Override
	final public SampledValue getValue(long time) {
		final int idx = m_values.getIndexBelow(time);
		final SampledValue left = (idx != SampledValueSortedList.NO_SUCH_INDEX) ? m_values.get(idx) : null;
		final SampledValue right = (idx < m_values.size() - 1) ? m_values.get(idx + 1) : null;
		return m_interpolationFunction.interpolate(left, right, time, m_type);
	}

	@Override
	public SampledValue getNextValue(long time) {
		return m_values.getNextValue(time);
	}

	@Override
	final public Long getTimeOfLatestEntry() {
		return new Long(-1);
	}

	@Override
	final public Long getLastCalculationTime() {
		return m_lastCalculationTime;
	}

	@Override
	public void addValue(SampledValue value) {
		m_values.addValue(value);
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
		// TODO make more performant by sorting only once, not after every new value insterted.
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

	@Override
	public boolean deleteValues() {
		m_values.clear();
		return true;
	}

	@Override
	final public boolean deleteValues(long endTime) {
		deleteValues(0, endTime);
		return true;
	}

	@Override
	public boolean deleteValues(long startTime, long endTime) {
		m_values.deleteValues(startTime, endTime);
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
	public Class<? extends Value> getValueType() {
		return m_type;
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return m_values.getValues(startTime);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		return m_values.getValues(startTime, endTime);
	}

	@Override
	public void shiftTimestamps(long dt) {
		final List<SampledValue> shiftedValues = new ArrayList<>(m_values.getValues().size());
		for (SampledValue value : m_values.getValues()) {
			final long t = value.getTimestamp() + dt;
			// if (t<0) continue;
			final SampledValue newValue = new SampledValue(value.getValue(), t, value.getQuality());
			shiftedValues.add(newValue);
		}
		m_values.clear();
		m_values.addValuesCopies(shiftedValues);
	}

	@Override
	public MemoryTimeSeries clone() {
		return new ArrayTimeSeries(this, m_type);
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
