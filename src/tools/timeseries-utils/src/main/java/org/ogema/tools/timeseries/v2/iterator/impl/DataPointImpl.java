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
package org.ogema.tools.timeseries.v2.iterator.impl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.tools.timeseries.v2.iterator.api.DataPoint;

public class DataPointImpl<T extends Comparable<T>> implements DataPoint<T> {
	
	// equal time stamp, not necessarily defined for all iterators
	protected final Map<Integer,T> values;
	// different time stamps, defined for all iterators except those from which no points have been retrieved yet 
	protected final Map<Integer,T> previousValues; 
	// different time stamps, defined for all iterators which have another entry
	protected final Map<Integer,T> nextValues;
	protected final MultiIteratorImpl<T> iterator;

	public DataPointImpl(final Map<Integer,T> values, final Map<Integer,T> previousValues, final Map<Integer, T> nextValues, final MultiIteratorImpl<T> iterator) {
		if (values.isEmpty())
			throw new IllegalArgumentException("No entries found");
		this.values = new HashMap<>(values);
		this.iterator = iterator;
		this.previousValues = (previousValues != null ? new HashMap<>(previousValues) : null);
		this.nextValues = (nextValues != null ? new HashMap<>(nextValues) : null);
	}
	
	@Override
	public int inputSize() {
		return iterator.size;
	}
	
	@Override
	public Map<Integer, T> getElements() {
		return values;
	}
	
	@Override
	public DataPoint<T> getPrevious(int stepsBack) throws IllegalArgumentException, IllegalStateException {
		if (iterator.getCurrent() != this)
			throw new IllegalStateException("Trying to retrieve historical data from a historical DataPoint");
		return iterator.getHistorical(stepsBack);
	}
		
	@Override
	public T next(int idx) {
		if (nextValues == null)
			throw new UnsupportedOperationException("next() not supported");
		return nextValues.get(idx);
	}
	
	@Override
	public T previous(int idx) {
		if (previousValues == null)
			throw new UnsupportedOperationException("previous() not supported");
		return previousValues.get(idx);
	}

	@Override
	public boolean hasNext(int idx) {
		if (nextValues == null)
			throw new UnsupportedOperationException("hasNext() not supported");
		return nextValues.containsKey(idx);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + values + "]";
	}
	
}
