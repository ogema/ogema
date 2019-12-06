package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Skips elements of a base iterator that do not match the specified condition
 * 
 * @param <T>
 */
public class PredicateIterator<T> implements Iterator<T>  {

	private final Predicate<T> condition;
	private final Iterator<T> base;
	private T next;

	public PredicateIterator(Iterator<T> base, Predicate<T> condition) {
		this.base = Objects.requireNonNull(base);
		this.condition = Objects.requireNonNull(condition);
	}

	@Override
	public boolean hasNext() {
		if (next != null)
			return true;
		while (base.hasNext()) {
			final T next = base.next();
			if (condition.test(next)) {
				this.next = next;
				return true;
			}
		}
		return false;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException("No further element");
		final T next = this.next;
		this.next = null;
		return next;
	}
	
	@Override
	public void remove() {
		base.remove();
	}
	
}
