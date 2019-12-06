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
package org.ogema.tools.timeseries.v2.tools;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

class EmptyTimeseries implements ReadOnlyTimeSeries {
	
	static final EmptyTimeseries INSTANCE = new EmptyTimeseries();
	
	private EmptyTimeseries() {}

	@Override
	public SampledValue getValue(long time) {
		return null;
	}

	@Override
	public SampledValue getNextValue(long time) {
		return null;
	}

	@Override
	public SampledValue getPreviousValue(long time) {
		return null;
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		return Collections.emptyList();
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		return Collections.emptyList();
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		return InterpolationMode.NONE;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		return true;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public int size(long startTime, long endTime) {
		return 0;
	}

	@Override
	public Iterator<SampledValue> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return Collections.emptyIterator();
	}

	@Override
	public Long getTimeOfLatestEntry() {
		return null;
	}

}
