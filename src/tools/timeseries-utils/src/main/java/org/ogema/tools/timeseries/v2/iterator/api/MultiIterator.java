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
package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.Iterator;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Wraps a set of iterators over elements of T. Typically, T = {@link SampledValue}, and
 * the iterators are obtained from a set of {@link ReadOnlyTimeSeries}. The MultiIterator
 * loops through the time series in a chronological manner, i.e. it checks which time series 
 * has the next timestamp. The time series from which a point has been retrieved can be queried
 * via {@link DataPoint#getElements()} (the map keys are the indices of the time series in the collection
 * that defines the MultiIterator).
 */
public interface MultiIterator<T extends Comparable<T>> extends Iterator<DataPoint<T>> {

	/**
	 * Check whether at least one of the underlying iterators has a next value. 
	 * @see Iterator#hasNext()
	 * @return
	 */
	@Override
	boolean hasNext();
	
	/**
	 * The MultiIterator can keep track of historical values; this returns the maximum number of values stored
	 * @return
	 * 		a non-negative number that was passed to the constructor of the MultiIterator.
	 */
	int maxNrHistoricalValues();
	
	/**
	 * @return
	 * 		The number of iterators wrapped by this multi iterator. Not the number of data points provided by the iterators.
	 */
	int size();
	
}
