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

import java.util.Map;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * A DataPoint wraps the elements returned by the iterators for a fixed time stamp.
 * It also keeps a reference to the previous and the next values of all time series.
 * 
 * @param <T>
 */
public interface DataPoint<T extends Comparable<T>> {

	/**
	 * Returns the elements at a given point. Here, a "point" is defined 
	 * abstractly by the set of elements for which t1.compare(t2) == 0.
	 * For the reference case T = {@link SampledValue} this means 
	 * data points at equal time stamps. Hence, think of a set of time series,
	 * then a DataPoint is associated to one fixed time stamp, and 
	 * getElements() returns an entry for each of the time series that have a
	 * data point at this time stamp.
	 * 
	 * @return
	 * 		an unmodifiable map, whose keys correspond to the indices of the iterators
	 * 		passed to the underlying {@link MultiIterator}.
	 */
	Map<Integer,T> getElements();
	
//	/**
//	 * Retrieve a value also for time series that do not have a data point at 
//	 * the given timestamp, by means of interpolation.
//	 * @param idx
//	 * @param interpolationMode
//	 * @return
//	 * 		null, if no value can be retrieved
//	 */
	// TODO only for SampledValue
//	T getElement(int idx, InterpolationMode interpolationMode);
	
	/**
	 * Access previous points. Note that this will fail as soon as this DataPoint is no longer the
	 * last one retrieved from the iterator. In particular, calling <tt>getPrevious</tt> recursively
	 * (<code>dataPoint.getPrevious(m).getPrevious(n)</code>) will always lead to an exception.
	 * @param stepsBack
	 * 		a positive integer
	 * @throws IllegalArgumentException
	 * 		if stepsBack is non-positive, or greater than the maximum number of stored 
	 * 		historical values (see {@link MultiIterator#maxNrHistoricalValues()}).
	 * @throws IllegalStateException
	 * 		if this DataPoint is not the last element retrieved from the iterator
	 * @return
	 * 		null if stepsBack is greater than the number of values retrieved so far, the historical values
	 * 		otherwise
	 */
	DataPoint<T> getPrevious(int stepsBack) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * Check whether the time series at index <tt>idx</tt> has another value.
	 * @param idx
	 * @return
	 */
	boolean hasNext(int idx);
	
	/**
	 * Retrieve the following value for the time series at index <tt>idx</tt>. This does not advance the underlying MultiIterator.
	 * @param idx
	 * @return
	 * 		the next value of the idx-th time series, or null, if there isn't any.
	 */
	T next(int idx);
	
	/**
	 * See {@link #next(int)}.
	 * @param idx
	 * @return
	 */
	T previous(int idx);
	
	/**
	 * This is a convenience method that allows users of the interface to store context data for a DataPoint.
	 * 
	 * @param object
	 */
	<S> void setContext(S object);
	
	/**
	 * Retrieve the context element set previously in {@link #setContext(Object)}.
	 * 
	 * @return
	 */
	<S> S getContext();
	
	/**
	 * The size of the time series collection.
	 * @return
	 */
	int inputSize();
	
}
