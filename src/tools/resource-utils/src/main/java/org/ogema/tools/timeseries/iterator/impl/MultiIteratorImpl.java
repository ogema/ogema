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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;

import org.ogema.tools.timeseries.iterator.api.DataPoint;
import org.ogema.tools.timeseries.iterator.api.MultiIterator;

import com.google.common.collect.EvictingQueue;

/**
 * An iterator that keeps track of a configurable number of historical points, plus allows access to the following point.
 *
 * @param <T>
 */
public class MultiIteratorImpl<T extends Comparable<T>> implements MultiIterator<T> {
	
	// do not rename: accessed by reflections
	final List<Iterator<T>> iterators;
	final int maxNrHistoricalValues;
	// may be null
	final Map<Integer,T> lowerBoundaryValues;
	// may be null
	final Map<Integer,T> upperBoundaryValues;
	// state variables
	// ordered chronologically; may be null (if maxNrHistoricalValues == 0)
	final Queue<DataPointImpl<T>> historicalValues;
	// equal time stamps
	final Map<Integer, T> currentValues;
	// equal time stamps, only defined for a subset of iterators
	final Map<Integer, T> comingValues;
	// Map<iterator index, next value>; includes the comingValues
	// different time stamps
	final Map<Integer,T> nextValues;
	// different time stamps
	final Map<Integer,T> previousValues;
	DataPointImpl<T> current;
	final int size;
	
	public MultiIteratorImpl(final List<Iterator<T>> iterators, final int maxNrHistoricalValues) {
		this(iterators, maxNrHistoricalValues, null, null);
	}
	
	/**
	 * It is assumed that the iterators are compatible with the ordering on T elements.
	 * @param iterators
	 * @param maxNrHistoricalValues
	 */
	public MultiIteratorImpl(final List<Iterator<T>> iterators, final int maxNrHistoricalValues,
			final Map<Integer,T> lowerBoundaryValues, final Map<Integer,T> upperBoundaryValues) {
		Objects.requireNonNull(iterators);
		if (maxNrHistoricalValues < 0)
			throw new IllegalArgumentException("maxNrHistoricalValues must not be negative. Got " + maxNrHistoricalValues);
		this.iterators = iterators;
		this.size = iterators.size();
		this.currentValues = new HashMap<>(iterators.size());
		this.comingValues = new HashMap<>(iterators.size());
		this.nextValues = new HashMap<>(iterators.size());
		this.previousValues = new HashMap<>(iterators.size());
		this.lowerBoundaryValues = (lowerBoundaryValues != null ? new HashMap<>(lowerBoundaryValues) : null);
		this.upperBoundaryValues = (upperBoundaryValues != null ? new HashMap<>(upperBoundaryValues) : null);
		this.maxNrHistoricalValues = maxNrHistoricalValues;
		this.historicalValues = (maxNrHistoricalValues != 0 ? EvictingQueue.<DataPointImpl<T>> create(maxNrHistoricalValues) : null);
		init();
	}

	@Override
	public boolean hasNext() {
		return !comingValues.isEmpty();
	}
	
	@Override
	public DataPoint<T> next() {
		advance();
		current = getDataPoint(currentValues, previousValues, nextValues);
		return current;
	}
	
	protected DataPointImpl<T> getDataPoint(Map<Integer, T> currentValue, Map<Integer,T> previousValues, Map<Integer,T> nextValues) {
		return new DataPointImpl<>(currentValue, previousValues, nextValues, this);
	}
	
	protected final DataPointImpl<T> getCurrent() {
		return current;
	}
	
	protected DataPointImpl<T> getHistorical(int stepsBack) {
		if (historicalValues == null)
			throw new UnsupportedOperationException("This iterator does not store historical values");
		if (stepsBack <= 0 || stepsBack > maxNrHistoricalValues)
			throw new IllegalArgumentException("stepsBack must be a positive number between 1 and " + maxNrHistoricalValues + ". Got " + stepsBack);
		final int sz = historicalValues.size();
		if (sz < stepsBack)
			return null;
		int idx = sz - stepsBack;
		final Iterator<DataPointImpl<T>> it = historicalValues.iterator();
		for (int i=0;i<idx;i++)
			it.next();
		return it.next();
	}

	protected void advance() {
		if (comingValues.isEmpty())
			throw new NoSuchElementException("No further element");
		if (historicalValues != null && current != null) {
			historicalValues.add(current);
		}
		final Iterator<Map.Entry<Integer, T>> currentValsIt = currentValues.entrySet().iterator();
		while (currentValsIt.hasNext()) {
			final Map.Entry<Integer, T> entry = currentValsIt.next();
			currentValsIt.remove();
			previousValues.put(entry.getKey(), entry.getValue());
		}
		// TODO more efficient algorithm?
		currentValues.putAll(comingValues);
		comingValues.clear();
		for (int key : currentValues.keySet()) {
			final Iterator<T> it = iterators.get(key);
			boolean done = false;
			if (it.hasNext()) { 
				done = true;
				nextValues.put(key, it.next());
			} else if (upperBoundaryValues  != null) {
				final T next = upperBoundaryValues.remove(key);
				if (next != null) {
					done = true;
					nextValues.put(key, next);
				}
			}
			if (!done)
				nextValues.remove(key);
		}
		
		if (nextValues.isEmpty())
			return;
		T sampleNext = null;
		// determine next time stamp
		for (T t : nextValues.values()) {
			if (sampleNext == null || t.compareTo(sampleNext) < 0)
				sampleNext = t;
		}
		T t;
		Map.Entry<Integer, T> entry;
		// set nextValues and comingValues
		final Iterator<Map.Entry<Integer, T>> entriesIt = nextValues.entrySet().iterator();
		while (entriesIt.hasNext()) {
			entry = entriesIt.next();
			t = entry.getValue();
			if (t.compareTo(sampleNext) == 0) {
				final int n = entry.getKey();
				comingValues.put(n, t);
			}
			
		}
	}
	
	protected void init() {
		Iterator<T> it;
		T next;
		for (int n=0;n<iterators.size();n++) {
			if (lowerBoundaryValues == null || !lowerBoundaryValues.containsKey(n)) {
				it = iterators.get(n);
				if (!it.hasNext())
					continue;
				next = it.next();
			} else {
				next = lowerBoundaryValues.get(n);
			}
			if (comingValues.isEmpty()) 
				comingValues.put(n, next);
			else {
				final T other = comingValues.values().iterator().next();
				final int diff = next.compareTo(other);
				if (diff < 0) 
					comingValues.clear();
				if (diff <= 0)
					comingValues.put(n, next);
			}
			nextValues.put(n, next);
		}
		
	}

	@Override
	public int maxNrHistoricalValues() {
		return maxNrHistoricalValues;
	}
	
	@Override
	public int size() {
		return iterators.size();
	}

	@Override
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("MultiIterator does not support remove()");
	}
	

}
