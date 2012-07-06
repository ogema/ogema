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
package org.ogema.tools.timeseries.api;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.core.timeseries.TimeSeries;

/**
 * An object behaving like OGEMA schedules but that is not an OGEMA resource. Can
 * be copied from and to real schedules. Intended use is for being used in complicated
 * calculations by OGEMA application that do not want to expose their intermediate
 * schedule calculation steps to the framework.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface MemoryTimeSeries extends TimeSeries, Cloneable {

	/**
	 * Returns the type of Value that this schedule takes. Note that there is
	 * no setter for this method. The type must be defined on construction.
	 * @return 
	 */
	Class<? extends Value> getValueType();

	/**
	 * Write this to another time series. Overwrites all previous content in the
	 * schedule.
	 */
	void write(TimeSeries timeSeries);

	/**
	 * Write this to another time series. Overwrites all previous content in the
	 * schedule.
	 * @param start - time of the first value in the time series in ms since epoche (inclusive).
	 * @param end - time of the last value in the time series in ms since epoche (exclusive).
	 */
	void write(TimeSeries timeSeries, long from, long to);

	/**
	 * Copies another TimeSeries into this (schedules, recorded data, time series). 
	 * Overwrites all previous content of this.
	 * @return returns a reference to itself after operation.
	 */
	MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries);

	/**
	 * Copies part of another TimeSeries into this. Overwrites all previous content of this.
	 * @param start - time of the first value in the time series in ms since epoch (inclusive).
	 * @param end - time of the last value in the time series in ms since epoch (exclusive).
	 * @return returns a reference to itself after operation.
	 */
	MemoryTimeSeries read(ReadOnlyTimeSeries timeSeries, long start, long end);

	/**
	 * Adds a copy of a SampledValue to schedule. The value type of the sampled
	 * value must fit to the type of values represented by this.
	 * @param value new value to add to schedule.
	 */
	void addValue(SampledValue value);

	/**
	 * Memory schedules do not live in an OGEMA resource framework and hence may
	 * not have a notion of a framework time. Therefore, this returns -1 for memory
	 * schedules.
	 * @return Always returns -1 for memory schedules.
	 */
	@Override
	Long getTimeOfLatestEntry();

	/**
	 * Shifts the timestamps of all entries by dT.
	 */
	void shiftTimestamps(long dt);

	/**
	 * Create a copy of this.
	 */
	MemoryTimeSeries clone();
}
