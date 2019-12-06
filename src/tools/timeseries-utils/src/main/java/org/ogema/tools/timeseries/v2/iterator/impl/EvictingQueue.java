package org.ogema.tools.timeseries.v2.iterator.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * A queue with finite capacity, which removes elements automatically once it is
 * full. Essentially copied from Guava's EvictingQueue.
 */
class EvictingQueue<T> implements Queue<T> {
	
	private final Queue<T> delegate;
	private final int maxSize;
	
	/**
	 * @param maxSize non-negative
	 * @throws IllegalArgumentException if maxSize is negative
	 */
	public EvictingQueue(int maxSize) {
		if (maxSize < 0)
			throw new IllegalArgumentException("maxSize must be non-negative, got " + maxSize);
		this.delegate = new ArrayDeque<T>(maxSize);
	    this.maxSize = maxSize;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <S> S[] toArray(S[] a) {
		return delegate.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T t: c) {
			add(t);
		}
		return !c.isEmpty();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean add(T e) {
		if (maxSize == 0)
			return true;
		if (size() == maxSize)
			delegate.remove();
		delegate.add(e);
		return true;
	}

	@Override
	public boolean offer(T e) {
		return add(e);
	}

	@Override
	public T remove() {
		return delegate.remove();
	}

	@Override
	public T poll() {
		return delegate.poll();
	}

	@Override
	public T element() {
		return delegate.element();
	}

	@Override
	public T peek() {
		return delegate.peek();
	}
	
}
