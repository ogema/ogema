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
package org.ogema.tools.timeseries.v2.iterator.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.tools.timeseries.v2.tools.TimeSeriesUtils;

/**
 * A timeseries based on data provided through some iterator. It does not
 * cache any values.  
 */
public class IteratorTimeSeries implements ReadOnlyTimeSeries {
	
	public static interface IteratorSupplier {
		
		/**
		 * Return an iterator over the specified 
		 * @param startTime inclusive
		 * @param endTime inclusive
		 * @return
		 */
		Iterator<SampledValue> get(long startTime, long endTime);
		
	}
	
	private final IteratorSupplier supplier;
	private final InterpolationMode mode;
	
	public IteratorTimeSeries(IteratorSupplier supplier, InterpolationMode mode) {
		this.supplier = Objects.requireNonNull(supplier);
		this.mode = mode; 
	}

	@Override
	public SampledValue getValue(long time) {
		if (mode == null || mode == InterpolationMode.NONE) {
			final Iterator<SampledValue> it = iterator(time, time);
			return it.hasNext() ? it.next() : null;
		}
		final Iterator<SampledValue> it = iterator(Long.MIN_VALUE, time);
		if (!it.hasNext())
			return null;
		SampledValue last = it.next();
		if (last.getTimestamp() == time)
			return last;
		if (last.getTimestamp() > time)
			return null;
		while (it.hasNext()) {
			SampledValue sv = it.next();
			if (sv.getTimestamp() == time)
				return sv;
			if (sv.getTimestamp() > time)
				return TimeSeriesUtils.interpolate(last, sv, time, mode);
			last = sv;
		}
		if (mode == InterpolationMode.STEPS || mode == InterpolationMode.NEAREST)
			return new SampledValue(last.getValue(), time, last.getQuality());
		return null;
	}

	@Override
	public SampledValue getNextValue(long time) {
		final Iterator<SampledValue> it = iterator(time, Long.MAX_VALUE);
		return it.hasNext() ? it.next(): null;
	}

	@Override
	public SampledValue getPreviousValue(long time) {
		final Iterator<SampledValue> it = iterator(Long.MIN_VALUE, time);
		if (!it.hasNext())
			return null;
		SampledValue last = it.next();
		if (last.getTimestamp() == time)
			return last;
		if (last.getTimestamp() > time)
			return null;
		while (it.hasNext()) {
			final SampledValue next = it.next();
			if (next.getTimestamp() == time)
				return next;
			if (next.getTimestamp() > time)
				return last;
			last = next;
		}
		return last;
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return getValues(startTime, Long.MAX_VALUE);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		final Iterator<SampledValue> it = iterator(startTime, endTime);
		final List<SampledValue> list=  new ArrayList<>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return mode;
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		return !iterator(startTime, endTime).hasNext();
	}

	// expensive... buffer value?
	@Override
	public int size() {
		return getValues(Long.MIN_VALUE).size();
	}

	@Override
	public int size(long startTime, long endTime) {
		return getValues(startTime, endTime).size();
	}

	@Override
	public Iterator<SampledValue> iterator() {
		return iterator(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		final Iterator<SampledValue> it = supplier.get(startTime, endTime);
		if (startTime == Long.MIN_VALUE && endTime == Long.MAX_VALUE)
			return it;
		return new PredicateIterator<>(it, sv -> sv.getTimestamp() >= startTime && sv.getTimestamp() <= endTime);
	}

	@Override
	public Long getTimeOfLatestEntry() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
