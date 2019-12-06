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
package org.ogema.tools.timeseries.v2.base;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.TimeSeries;

/**
 * Provides default implementations for most methods in {@link TimeSeries}.
 * Note that the default implementations may be quite inefficient.
 */
public interface TimeSeriesBase extends ReadOnlyTimeSeriesBase, TimeSeries {

	/**
	 * add single value to schedule. If a value for the same timestamp already
	 * exists, it is overwritten
	 *
	 * @param timestamp time to which the value applies (ms since epoch)
	 * @param value
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	default boolean addValue(long timestamp, Value value) {
		return addValues(Collections.singleton(new SampledValue(value, timestamp, Quality.GOOD)));
	}

	/**
	 * Adds a set of new entries.
	 *
	 * @param values new entries to add.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	default boolean addValues(Collection<SampledValue> values) {
		return addValues(values, System.currentTimeMillis());
	}

	/**
	 * @deprecated Use {@link #replaceValuesFixedStep(long, java.util.List, long)}, instead.
	 */
	@Deprecated
	default boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
		return false;
	}

	/**
	 * add single value to schedule. If a value for the same timestamp already
	 * exists, it is overwritten
	 *
	 * @param timestamp
	 * @param value
	 * @param timeOfCalculation time when the value was calculated (ms since
	 * epoch)
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	default boolean addValue(long timestamp, Value value, long timeOfCalculation) {
		return addValues(Collections.singleton(new SampledValue(value, timestamp, Quality.GOOD)), timeOfCalculation);
	}

	/**
	 * @deprecated User {@link #replaceValuesFixedStep(long, java.util.List, long, long)}, instead
	 */
	@Deprecated
	default boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
		return false;
	}

	/**
	 * get time of calculation provided with last write operation into time
	 * series
	 *
	 * @return ms since epoch if the time of calculation was provided with last
	 * write operation. Otherwise null is returned.
	 */
	default Long getLastCalculationTime() {
		return null;
	}

	/**
	 * Deletes all support points defining the time series.
	 *
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	default boolean deleteValues() {
		return deleteValues(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * Deletes the support points in the time interval [0;endTime-1] (i.e. the
	 * time at endTime is not included).
	 *
	 * @param endTime time up to which the values are deleted.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	// FIXME this means that Long.MAX_VALUE cannot be deleted... 
	default boolean deleteValues(long endTime) {
		return deleteValues(Long.MIN_VALUE, endTime);
	}

	/**
	 * Delete the values in the interval [startTime;endTime-1] and add the
	 * values passed into this interval. If a value to be inserted has a
	 * timestamp outside the given interval, it is not added. If Null is passed
	 * for the list of values, the argument is interpreted as an empty list.
	 *
	 * @param startTime first time stamp of the interval to delete
	 * @param endTime time up to which the values are deleted.
	 * @param values the values to be inserted into the interval
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	// note: default impl does not support transactions/synchronization
	default boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		return deleteValues(startTime, endTime) && 
		addValues(values.stream().filter(v -> v.getTimestamp() >= startTime && v.getTimestamp() < endTime).collect(Collectors.toList()));
	}

	/**
	 * add schedule based on values with a fixed time step. All existing values
	 * in the range between startTime and the time of the last value are
	 * deleted.
	 *
	 * @param startTime time to which first value applies (ms since epoch)
	 * @param stepSize duration of each step
	 * @param values array containing values for each step size
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	default boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		return replaceValuesFixedStep(startTime, values, stepSize, System.currentTimeMillis());
	}

	/**
	 * Same as
	 * {@link #replaceValuesFixedStep(long, java.util.List, long)}
	 * but additionally stores the calculation time.
	 *
	 * @param startTime timestamp at which the first value is inserted
	 * @param stepSize time between two subsequently-inserted values
	 * @param values the list of values that is inserted in a time order equal
	 * to the list order
	 * @param timeOfCalculation timestamp of the time when the values were
	 * created. This provides extra information about the schedule that may be
	 * relevant for application (e.g. in case of the time of calculation of a
	 * weather forecast).
	 * @see #getLastCalculationTime()
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	// note: default impl does not support transaction
	default boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize, long timeOfCalculation) {
		final long endTime = startTime + values.size() * stepSize;
		final AtomicLong timestamp = new AtomicLong(startTime);
		return deleteValues(startTime, endTime) && 
				addValues(values.stream()
						.map(v -> new SampledValue(v, timestamp.getAndAdd(stepSize), Quality.GOOD))
						.collect(Collectors.toList()), timeOfCalculation);	
	}

	@Override
	default int size() {
		return size(Long.MIN_VALUE, Long.MAX_VALUE);
	}

}
