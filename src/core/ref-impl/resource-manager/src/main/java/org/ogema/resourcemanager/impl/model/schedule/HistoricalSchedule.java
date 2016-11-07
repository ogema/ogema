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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.persistence.impl.faketree.ScheduleTreeElement;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;
import org.ogema.resourcemanager.virtual.VirtualTreeElement;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.TreeTimeSeries;

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
	private final Class<? extends Value> type;

	public HistoricalSchedule(VirtualTreeElement el, ScheduleTreeElement scheduleElement, String path,
			ApplicationResourceManager resMan, ApplicationManager appMan) {
		super(el, scheduleElement, path, resMan, appMan);
		this.parentResource = resMan.getResource(scheduleElement.getParent().getParent().getPath()); // this is the parent by location, not by path
		if (parentResource == null)
			throw new RuntimeException("Internal error: schedule has no parent");
		this.type = scheduleElement.getValueType();
	}

	@Override
	public SampledValue getValue(long time) {
		MemoryTimeSeries mts = new TreeTimeSeries(this, type);
		return mts.getValue(time);
	}

	@Override
	public SampledValue getNextValue(long time) {
		MemoryTimeSeries mts = new TreeTimeSeries(this, type);
		return mts.getNextValue(time);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		RecordedData rd = getHistoricalData();
		List<SampledValue> loggedData = rd.getValues(startTime, endTime); // FIXME this sometimes returns an empty list although log data is available
		List<SampledValue> otherData = super.getValues(startTime, endTime);
		return merge(loggedData, otherData);
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		RecordedData rd = getHistoricalData();
		List<SampledValue> loggedData = rd.getValues(startTime);
		List<SampledValue> otherData = super.getValues(startTime);
		List<SampledValue> result = merge(loggedData, otherData);
		return result;
	}

	/*
	 * Merges two lists of SampledValues. In case of duplicate timestamps elements from list1 are removed.
	 */
	private static List<SampledValue> merge(List<SampledValue> list1, List<SampledValue> list2) {
		if (list1.isEmpty()) {
			return list2;
		}
		else if (list2.isEmpty()) {
			return list1;
		}
		// merge the two time series
		long l1Start = list1.get(0).getTimestamp();
		long l1End = list1.get(list1.size() - 1).getTimestamp();
		long l2Start = list2.get(0).getTimestamp();
		long l2End = list2.get(list2.size() - 1).getTimestamp();
		if (l1End < l2Start) {
			list1.addAll(list2);
			return list1;
		}
		else if (l2End < l1Start) {
			list2.addAll(list1);
			return list2;
		}
		// remove duplicate timestamps from list1
		removeDuplicates(list1, list2);
		list1.addAll(list2);
		Collections.sort(list1);
		return list1;
	}

	private static void removeDuplicates(List<SampledValue> list1, List<SampledValue> list2) {
		Iterator<SampledValue> it = list1.iterator();
		long last1 = 0;
		long last2 = 0;
		int lastIdx2 = 0;
		int sz2 = list2.size();
		while (it.hasNext()) {
			SampledValue sv1 = it.next();
			last1 = sv1.getTimestamp();
			for (int i = lastIdx2; i < sz2; i++) {
				SampledValue sv2 = list2.get(i);
				last2 = sv2.getTimestamp();
				lastIdx2 = i;
				if (last2 > last1)
					break;
				if (last2 == last1) {
					it.remove();
					break;
				}
			}
		}
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

}
