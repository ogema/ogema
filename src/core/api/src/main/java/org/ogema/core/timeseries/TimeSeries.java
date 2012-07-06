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
package org.ogema.core.timeseries;

import java.util.Collection;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;

/**
 * Function over time (which is stored as ms since 1970 in OGEMA). Extends the
 * {@link ReadOnlyTimeSeries} by methods to manipulate the function. <br>
 *
 * @see ReadOnlyTimeSeries
 */
public interface TimeSeries extends ReadOnlyTimeSeries {

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
	boolean addValue(long timestamp, Value value);

	/**
	 * Adds a set of new entries.
	 *
	 * @param values new entries to add.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	boolean addValues(Collection<SampledValue> values);

	/**
	 * @deprecated Use {@link #replaceValuesFixedStep(long, java.util.List, long)}, instead.
	 */
	@Deprecated
	boolean addValueSchedule(long startTime, long stepSize, List<Value> values);

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
	boolean addValue(long timestamp, Value value, long timeOfCalculation);

	/**
	 * Adds a set of new entries
	 *
	 * @param values new entries to add.
	 * @param timeOfCalculation time the entries were calculated. Updates the
	 * schedule's time of calculation.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	boolean addValues(Collection<SampledValue> values, long timeOfCalculation);

	/**
	 * @deprecated User {@link #replaceValuesFixedStep(long, java.util.List, long, long)}, instead
	 */
	@Deprecated
	boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation);

	/**
	 * get time of calculation provided with last write operation into time
	 * series
	 *
	 * @return ms since epoch if the time of calculation was provided with last
	 * write operation. Otherwise null is returned.
	 */
	Long getLastCalculationTime();

	/**
	 * Deletes all support points defining the time series.
	 *
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	boolean deleteValues();

	/**
	 * Deletes the support points in the time interval [0;endTime-1] (i.e. the
	 * time at endTime is not included).
	 *
	 * @param endTime time up to which the values are deleted.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	boolean deleteValues(long endTime);

	/**
	 * Delete the values in the time interval [startTime; endTime-1] (i.e. with
	 * startTime being included in the interval but endTime not).
	 *
	 * @param startTime first time stamp of the interval to delete
	 * @param endTime time up to which the values are deleted.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	boolean deleteValues(long startTime, long endTime);

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
	boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values);

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
	boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize);

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
	boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize, long timeOfCalculation);

	/**
	 * (Re-) defines how the schedule entries are to be interpreted.
	 *
	 * @param mode New interpolation mode to set.
	 * @return returns true if the operation was performed, false if it was
	 * rejected (this can occur in TimeSeries on which a write restriction is
	 * enforced or that can be virtual).
	 */
	boolean setInterpolationMode(InterpolationMode mode);
}
