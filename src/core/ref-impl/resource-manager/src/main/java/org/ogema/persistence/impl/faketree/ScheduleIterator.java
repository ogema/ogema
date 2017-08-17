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
package org.ogema.persistence.impl.faketree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReadWriteLock;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;

public class ScheduleIterator implements Iterator<SampledValue> {
	
	private final MemoryTimeSeries timeSeries;
	private final ReadWriteLock lock;
	private final long minIncl;
	private final long maxIncl;
	private SampledValue current = null;
	private SampledValue next = null;
	
	public ScheduleIterator(MemoryTimeSeries timeSeries,ReadWriteLock lock) {
		this(timeSeries, lock, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	public ScheduleIterator(MemoryTimeSeries timeSeries,ReadWriteLock lock, long minIcluded, long maxIncluded) {
		this.timeSeries = timeSeries;
		this.lock = lock;
		this.minIncl = minIcluded;
		this.maxIncl = maxIncluded;
	}

	@Override
	public boolean hasNext() {
		final SampledValue n = getNext();
		// we need to remember the next value, since if we return true here, we must be able
		// to return a non-null value in the following next() call, even if the schedule has been 
		// cleared in the meantime.
		next = n;
		return (n != null);
	}

	@Override
	public SampledValue next() {
		final SampledValue current = this.current;
		// if this is non-null and newer than current, we must return a non-null value (this means we have returned true to hasNext(), 
		// since the last call to next() )
		final SampledValue storedNext = this.next;
		final SampledValue next;
		if (nextIsNewer(current, storedNext)) {
			next = storedNext;
		}
		else {
			if (!hasNext())
				throw new NoSuchElementException();
			next = this.next;
		}
		this.current = next;
		return next;
		
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Schedule iterator does not support removal");
	}
	
	private SampledValue getNext() {
		final SampledValue current = this.current;
		final SampledValue n;
		lock.readLock().lock();
		try {
			if (current == null) 
				n = timeSeries.getNextValue(minIncl);
			else {
				long t = current.getTimestamp();
				if (t >= maxIncl)
					return null;
				n = timeSeries.getNextValue(t+1);
			}
		} finally {
			lock.readLock().unlock();
		}
		if (n != null && n.getTimestamp() > maxIncl)
			return null;
		return n;
	}
	
	private static final boolean nextIsNewer(SampledValue current, SampledValue next) {
		if (current == null || next == null) 
			return next != null;
		if (next == current)
			return false;
		return current.getTimestamp() < next.getTimestamp();
	}

}
