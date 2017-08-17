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
package org.ogema.recordeddata.slotsdb;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReadWriteLock;

import org.ogema.core.channelmanager.measurements.SampledValue;

/**
 * A fail-safe iterator that reads data file by file (which means day by day) 
 */
class SlotsDbIterator implements Iterator<SampledValue> {
	
	private final SlotsDb recorder;
	private final String id; 
	private final long start;
	private final long end;
	private final ReadWriteLock lock;
	final String label;
	
	private volatile FileObjectList folder = null;
	private volatile List<SampledValue> folderValues = null;
	private int currentIdx = 0;
	private SampledValue current = null;
	private SampledValue next = null;
	
	SlotsDbIterator(String id, SlotsDb recorder, ReadWriteLock lock) {
		this(id, recorder, lock, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	SlotsDbIterator(String id, SlotsDb recorder, ReadWriteLock lock, long start, long end) {
		this.recorder = recorder;
		this.id = id;
		this.lock = lock;
		this.start = start;
		this.end = end;
		String label = id;
		try {
			label = recorder.getProxy().encodeLabel(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.label = label;
	}

	@Override
	public SampledValue next() {
		if (!hasNext())
			throw new NoSuchElementException();
		current = next;
		return next;
	}
	
	@Override
	public boolean hasNext() {
		if (nextIsNewer(current, next)) {
			return true;
		}
		else if (next == null && current != null) // reached the end 
			return false;
		if (folderValues != null && currentIdx < folderValues.size()) {
			next = folderValues.get(currentIdx++);
			if (next.getTimestamp() > end) { // reached the end
				next = null;
				return false;
			}
			while (next.getTimestamp() < start) {
				current = next;
				if (currentIdx < folderValues.size()) {
					next = folderValues.get(currentIdx++);
				} else {
					return hasNext();
				}
//			else if (next.getTimestamp() < start) {
//				current = next;
//				return hasNext();
			}
			return true;
		}
		parseNextFile();
		if (folderValues == null) {
			next = null;
			return false;
		}
		return hasNext(); // parse next folder's list
	}
	
	
	private void parseNextFile() {
		final FileObjectList folder = this.folder;
		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

				@Override
				public Void run() throws Exception {
					final FileObjectList newfolder;
					final List<SampledValue> values;
					lock.readLock().lock();
					try {
						if (folder == null) 
							newfolder = recorder.getProxy().getNextFolder(label, start);
						else
							newfolder = recorder.getProxy().getNextFolder(label, folder); 
						if (newfolder != null) 
							values = FileObjectProxy.readFolder(newfolder);
						else
							values = null;
					} finally {
						lock.readLock().unlock();
					}
					SlotsDbIterator.this.folder = newfolder;
					SlotsDbIterator.this.currentIdx = 0;
					SlotsDbIterator.this.folderValues = values;
					return null;
				}

			});
		} catch (PrivilegedActionException e) {
			throw new RuntimeException(e);
		}
	}

	private static final boolean nextIsNewer(SampledValue current, SampledValue next) {
		if (current == null || next == null) 
			return next != null;
		if (next == current)
			return false;
		return current.getTimestamp() < next.getTimestamp();
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Logdata iterator does not support removal");
	};
	
}
