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
package org.ogema.resourcemanager.impl.model.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.persistence.impl.faketree.ScheduleTreeElement;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.impl.ResourceDBManager;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.tools.resource.util.ValueResourceUtils;

/*
 * A schedule that allows to access both its own schedule values and the log data of its parent resource
 */
public class HistoricalSchedule extends DefaultSchedule {

	/*
	 * all schedule resources with this name will be realized as HistoricalSchedule
	 * (where the name is determined by location not path)
	 */
	public static final String PATH_IDENTIFIER = "historicalData";
	private final SingleValueResource parentResource;
//	private final Class<? extends Value> type;

	public HistoricalSchedule(VirtualTreeElement el, ScheduleTreeElement scheduleElement, String path,
			ApplicationResourceManager resMan, ApplicationManager appMan, ResourceDBManager dbMan) {
		super(el, scheduleElement, path, resMan, appMan, dbMan);
		this.parentResource = resMan.getResource(scheduleElement.getParent().getParent().getPath()); // this is the parent by location, not by path
		if (parentResource == null)
			throw new RuntimeException("Internal error: schedule has no parent");
//		this.type = scheduleElement.getValueType();
	}

	@Override
	public SampledValue getValue(long time) {
		SampledValue prev = getPreviousValue(time);
		SampledValue next = getNextValue(time);
		return ValueResourceUtils.interpolate(prev, next, time, getInterpolationMode());
	}

	@Override
	public SampledValue getNextValue(long time) {
		final SampledValue next = super.getNextValue(time);
		final RecordedData rd = getHistoricalData();
		if (rd != null) {
			final SampledValue rdNext = rd.getNextValue(time);
			if (rdNext != null && (next == null || next.getTimestamp() > rdNext.getTimestamp()))
				return rdNext;
		}
		return next;
	}
	
	@Override
	public SampledValue getPreviousValue(long time) {
		final SampledValue prev = super.getPreviousValue(time);
		final RecordedData rd = getHistoricalData();
		if (rd != null) {
			final SampledValue rdPrev = rd.getPreviousValue(time);
			if (rdPrev != null && (prev == null || prev.getTimestamp() < rdPrev.getTimestamp()))
				return rdPrev;
		}
		return prev;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		final RecordedData rd = getHistoricalData();
		if (rd == null)
			return super.getValues(startTime, endTime);
		return toList(new HistoricalIterator(super.iterator(startTime, endTime), rd.iterator(startTime, endTime)));
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return getValues(startTime, Long.MAX_VALUE); 
	}
	
	@Override
	public boolean isEmpty() {
		if (!super.isEmpty())
			return false;
		final RecordedData rd = getHistoricalData();
		return rd == null || rd.isEmpty();
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		if (!super.isEmpty(startTime, endTime))
			return false;
		final RecordedData rd = getHistoricalData();
		return rd == null || rd.isEmpty(startTime, endTime);
	}

	// note: this is only a guess... we do not know exactly the size
	// here we assume there are no overlaps
	@Override
	public int size(long startTime, long endTime) {
		int sz = super.size(startTime, endTime);
		final RecordedData rd = getHistoricalData();
		if (rd != null)
			sz += rd.size(startTime, endTime);
		return sz;
	}
	
	@Override
	public int size() {
		return size(Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	@Override
	public Iterator<SampledValue> iterator() {
		final RecordedData rd = getHistoricalData();
		if (rd == null)
			return super.iterator();
		return new HistoricalIterator(super.iterator(), rd.iterator());
	}
	
	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		final RecordedData rd = getHistoricalData();
		if (rd == null)
			return super.iterator(startTime, endTime);
		return new HistoricalIterator(super.iterator(startTime, endTime), rd.iterator(startTime, endTime));
	}
	
	/*
	 * Merges two lists of SampledValues. In case of duplicate timestamps log data is skipped.
	 */
	private static List<SampledValue> toList(Iterator<SampledValue> it) {
		final List<SampledValue> list = new ArrayList<>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	private RecordedData getHistoricalData() {
		if (parentResource instanceof FloatResource) {
			return ((FloatResource) parentResource).getHistoricalData();
		}
		else if (parentResource instanceof BooleanResource) {
			return ((BooleanResource) parentResource).getHistoricalData();
		}
		else if (parentResource instanceof IntegerResource) {
			return ((IntegerResource) parentResource).getHistoricalData();
		}
		else if (parentResource instanceof TimeResource) {
			return ((TimeResource) parentResource).getHistoricalData();
		}
		else
			return null;
	}
	
	// a joint iterator that skips log data points in case of timestamp collisions
	private static final class HistoricalIterator implements Iterator<SampledValue> {
	
		private final Iterator<SampledValue> scheduleIt;
		private final Iterator<SampledValue> logIt;
		// state variables
		private SampledValue logBuffer;
		private SampledValue scheduleBuffer;
		
		public HistoricalIterator(Iterator<SampledValue> scheduleIt, Iterator<SampledValue> logIt) {
			this.scheduleIt = scheduleIt;
			this.logIt = logIt;
		}

		@Override
		public boolean hasNext() {
			return scheduleIt.hasNext() || logIt.hasNext() || scheduleBuffer != null || logBuffer != null;
		}

		@Override
		public SampledValue next() {
			final SampledValue lnext = (logBuffer != null ? logBuffer : logIt.hasNext() ? logIt.next() : null);
			final SampledValue snext = (scheduleBuffer != null ? scheduleBuffer : scheduleIt.hasNext() ? scheduleIt.next() : null);
			if (lnext == null && snext == null)
				throw new NoSuchElementException();
			if (lnext == null) {
				scheduleBuffer = null;
				return snext;
			} 
			if (snext == null) {
				logBuffer = null;
				return lnext;
			}
			final long ts = snext.getTimestamp();
			final long tl = lnext.getTimestamp();
			if (ts <= tl) {
				scheduleBuffer = null;
				if (ts == tl)
					logBuffer = null;
				else
					logBuffer = lnext;
				return snext;
			}
			logBuffer = null;
			scheduleBuffer = snext;
			return lnext;
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Historical schedule iterator does not support removal");
		}
		
	}

}
