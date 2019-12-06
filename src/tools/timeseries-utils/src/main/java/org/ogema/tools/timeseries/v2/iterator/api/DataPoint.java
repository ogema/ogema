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
package org.ogema.tools.timeseries.v2.iterator.api;

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
	 * The size of the time series collection.
	 * @return
	 */
	int inputSize();
	
}
