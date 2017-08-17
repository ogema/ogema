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
