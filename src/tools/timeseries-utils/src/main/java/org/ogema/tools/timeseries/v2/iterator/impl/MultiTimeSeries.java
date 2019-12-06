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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * A time series that is composed of several other time series.
 * Data points of the underlying time series are not copied on construction, but evaluated  
 * on demand.
 * @author cnoelle
 */
abstract class MultiTimeSeries implements ReadOnlyTimeSeries {

	protected final List<ReadOnlyTimeSeries> constituents;
	protected final boolean ignoreGaps;
	protected final int size;
	protected final InterpolationMode forcedModeConstituents;
	protected final InterpolationMode forcedModeResult;
	
	MultiTimeSeries(
			Collection<ReadOnlyTimeSeries> constituents,
			boolean ignoreGaps,
			InterpolationMode forcedModeConstituents,
			InterpolationMode forcedModeResult) {
		this.constituents = new ArrayList<>(Objects.requireNonNull(constituents));
		this.ignoreGaps = ignoreGaps;
		this.size = constituents.size();
		this.forcedModeConstituents = forcedModeConstituents;
		this.forcedModeResult = forcedModeResult;
	}
	
	// best guess
        @SuppressWarnings("fallthrough")
	protected static InterpolationMode getMode(final Collection<ReadOnlyTimeSeries> timeseries) {
		InterpolationMode targetMode = null;
//		boolean allModesEqual = true;
		for (ReadOnlyTimeSeries schedule : timeseries) {
			InterpolationMode mode = schedule.getInterpolationMode();
//			if (targetMode != null && mode != targetMode)
//				allModesEqual = false;
			switch (mode) {
			case NONE:
				return InterpolationMode.NONE;
			case NEAREST:
				if (targetMode == null || targetMode == InterpolationMode.NEAREST)
					targetMode = InterpolationMode.NEAREST;
				else
					return InterpolationMode.NONE;
			case STEPS:
			case LINEAR:
				if (targetMode != InterpolationMode.LINEAR)
					targetMode = mode;
			}
		}
		return targetMode != null ? targetMode : InterpolationMode.NONE;
	}
	
	@Override
	public SampledValue getNextValue(long time) {
		SampledValue sv = null;
		for (ReadOnlyTimeSeries ts : constituents) {
			SampledValue next = ts.getNextValue(time);
			if (next == null)
				continue;
			if (sv != null) {
				if (next.getTimestamp() >= sv.getTimestamp())
					continue;
			}
			sv = next;
		}
		if (sv == null)
			return null;
		return getValue(sv.getTimestamp());
	}

	@Override
	public SampledValue getPreviousValue(long time) {
		SampledValue sv = null;
		for (ReadOnlyTimeSeries ts : constituents) {
			SampledValue next = ts.getPreviousValue(time);
			if (next == null)
				continue;
			if (sv != null) {
				if (next.getTimestamp() <= sv.getTimestamp())
					continue;
			}
			sv = next;
		}
		if (sv == null)
			return null;
		return getValue(sv.getTimestamp());
	}
	

	@Override
	public List<SampledValue> getValues(long startTime) {
		return getValues(startTime, Long.MAX_VALUE);
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		final Iterator<SampledValue> iterator = iterator(startTime, endTime);
		List<SampledValue> values = new ArrayList<>(30);
		while (iterator.hasNext())
			values.add(iterator.next());
		return values;
	}
	
	@Override
	public boolean isEmpty() {
		for (ReadOnlyTimeSeries ts: constituents) {
			if (!ts.isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		for (ReadOnlyTimeSeries ts: constituents) {
			if (!ts.isEmpty(startTime, endTime))
				return false;
		}
		return true;
	}

	/**
	 * Actually returns an upper limit on the size
	 */
	@Override
	public int size() {
		int sz = 0;
		for (ReadOnlyTimeSeries ts: constituents) {
			sz+=ts.size();
		}
		return sz;
	}

	/**
	 * Actually returns an upper limit on the size
	 */
	@Override
	public int size(long startTime, long endTime) {
		int sz = 0;
		for (ReadOnlyTimeSeries ts: constituents) {
			sz+=ts.size(startTime, endTime);
		}
		return sz;
	}
	

	@Override
	public Long getTimeOfLatestEntry() {
		long last = Long.MIN_VALUE;
		for (ReadOnlyTimeSeries ts: constituents) {
			@SuppressWarnings("deprecation")
			long t = ts.getTimeOfLatestEntry();
			if (t > last)
				last = t;
		}
		return last;
	}
	
}
