/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.timeseries.v2.memory;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.v2.base.ReadOnlyTimeSeriesBase;

/**
 * Memory timeseries based on a NavigableSet
 */
public class ReadOnlyTreeTimeSeries implements ReadOnlyTimeSeriesBase {

	final NavigableSet<SampledValue> values;
	volatile InterpolationMode mode;
	
	/**
	 * Note: if the passed set of values will be changed afterwards, it must
	 * be concurrent.
	 * @param values
	 * @param mode
	 */
	public ReadOnlyTreeTimeSeries(NavigableSet<SampledValue> values, InterpolationMode mode) {
		this.values = Objects.requireNonNull(values);
		this.mode = mode == null ? InterpolationMode.NONE : mode;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		return subSet(startTime, endTime, false).stream()
			.collect(Collectors.toList());
	}
	
	@Override
	public SampledValue getPreviousValue(long time) {
		return values.floor(new SampledValue(FloatValue.ZERO, time, Quality.BAD));
	}
	
	@Override
	public SampledValue getNextValue(long time) {
		return values.ceiling(new SampledValue(FloatValue.ZERO, time, Quality.BAD));
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return mode;
	}
	
	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return subSet(startTime, endTime, true).iterator();
	}
	
	@Override
	public boolean isEmpty(long startTime, long endTime) {
		return subSet(startTime, endTime, true).isEmpty();
	}
	
	@Override
	public int size() {
		return values.size();
	}
	
	@Override
	public int size(long startTime, long endTime) {
		return subSet(startTime, endTime, true).size();
	}
	
	NavigableSet<SampledValue> subSet(long startTime, long endTime, boolean endIncluded) {
		return startTime == Long.MIN_VALUE && endTime == Long.MAX_VALUE ? values : 
			values.subSet(new SampledValue(FloatValue.ZERO, startTime, Quality.BAD), true,
							new SampledValue(FloatValue.ZERO, endTime, Quality.BAD), endIncluded);

	}
	
}
