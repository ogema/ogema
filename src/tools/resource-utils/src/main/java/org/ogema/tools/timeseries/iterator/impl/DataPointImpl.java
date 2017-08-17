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
package org.ogema.tools.timeseries.iterator.impl;

import java.util.HashMap;
import java.util.Map;

import org.ogema.tools.timeseries.iterator.api.DataPoint;

public class DataPointImpl<T extends Comparable<T>> implements DataPoint<T> {
	
	// equal time stamp, not necessarily defined for all iterators
	protected final Map<Integer,T> values;
	// different time stamps, defined for all iterators except those from which no points have been retrieved yet 
	protected final Map<Integer,T> previousValues; 
	// different time stamps, defined for all iterators which have another entry
	protected final Map<Integer,T> nextValues;
	protected final MultiIteratorImpl<T> iterator;
	private Object context = null;
	
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
	public <S> void setContext(S object) {
		this.context = object;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S> S getContext() {
		return (S) context;
	}
	
}
