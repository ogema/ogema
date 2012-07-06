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

import java.util.Collection;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;

/**
 * Synchronized wrapper for a TimeSeries.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public class SynchronizedTimeSeries implements MemoryTimeSeries {

	/**
	 * Schedule that is wrapped. All calls in this are synchronized and delegated to the schedule.
	 */
	private final MemoryTimeSeries m_schedule;

	/**
	 * Wrapper constructor. Creates this synchronization wrapper around an existing MemoryTimeSeries
	 * 
	 * @param realSeries
	 *            Actual memory schedule to create a wrapper for. This object will keep a reference to the real
	 *            schedule, so other applications do not need to remember the real schedule as long as they keep the
	 *            reference to this wrapper.
	 */
	public SynchronizedTimeSeries(MemoryTimeSeries realSeries) {
		m_schedule = realSeries;
	}

	@Override
	public Class<? extends Value> getValueType() {
		return m_schedule.getValueType();
	}

	@Override
	public synchronized void write(TimeSeries schedule) {
		m_schedule.write(schedule);
	}

	@Override
	public synchronized void write(TimeSeries schedule, long from, long to) {
		m_schedule.write(schedule, from, to);
	}

	@Override
	public synchronized SynchronizedTimeSeries read(ReadOnlyTimeSeries schedule) {
		m_schedule.read(schedule);
		return this;
	}

	@Override
	public synchronized SynchronizedTimeSeries read(ReadOnlyTimeSeries schedule, long from, long to) {
		m_schedule.read(schedule, from, to);
		return this;
	}

	@Override
	public synchronized void addValue(SampledValue value) {
		m_schedule.addValue(value);
	}

	@Override
	public Long getTimeOfLatestEntry() {
		return m_schedule.getTimeOfLatestEntry();
	}

	@Override
	public synchronized void shiftTimestamps(long dt) {
		m_schedule.shiftTimestamps(dt);
	}

	@Override
	public synchronized boolean addValue(long timestamp, Value value) {
		return m_schedule.addValue(timestamp, value);
	}

	@Override
	public synchronized boolean addValues(Collection<SampledValue> values) {
		return m_schedule.addValues(values);
	}

	@Override
	public synchronized boolean addValue(long timestamp, Value value, long timeOfCalculation) {
		return m_schedule.addValue(timestamp, value, timeOfCalculation);
	}

	@Override
	public synchronized boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
		return m_schedule.addValues(values, timeOfCalculation);
	}

	@Override
	public Long getLastCalculationTime() {
		return m_schedule.getLastCalculationTime();
	}

	@Override
	public synchronized boolean deleteValues() {
		return m_schedule.deleteValues();
	}

	@Override
	public synchronized boolean deleteValues(long endTime) {
		return m_schedule.deleteValues(endTime);
	}

	@Override
	public synchronized boolean deleteValues(long startTime, long endTime) {
		return m_schedule.deleteValues(startTime, endTime);
	}

	@Override
	public synchronized boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		return m_schedule.replaceValues(startTime, endTime, values);
	}

	@Override
	public synchronized boolean setInterpolationMode(InterpolationMode mode) {
		return m_schedule.setInterpolationMode(mode);
	}

	@Override
	public synchronized SampledValue getValue(long time) {
		return m_schedule.getValue(time);
	}

	@Override
	public synchronized SampledValue getNextValue(long time) {
		return m_schedule.getNextValue(time);
	}

	@Override
	public synchronized List<SampledValue> getValues(long startTime) {
		return m_schedule.getValues(startTime);
	}

	@Override
	public synchronized List<SampledValue> getValues(long startTime, long endTime) {
		return m_schedule.getValues(startTime, endTime);
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return m_schedule.getInterpolationMode();
	}

	@Override
	public synchronized MemoryTimeSeries clone() {
		return m_schedule.clone();
	}

	@Override
	public synchronized boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		return m_schedule.replaceValuesFixedStep(startTime, values, stepSize);
	}

	@Override
	public synchronized boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize,
			long timeOfCalculation) {
		return m_schedule.replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
	}

	@Override
	public synchronized boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
		return m_schedule.addValueSchedule(startTime, stepSize, values);
	}

	@Override
	public synchronized boolean addValueSchedule(long startTime, long stepSize, List<Value> values,
			long timeOfCalculation) {
		return m_schedule.addValueSchedule(startTime, stepSize, values, timeOfCalculation);
	}

}
