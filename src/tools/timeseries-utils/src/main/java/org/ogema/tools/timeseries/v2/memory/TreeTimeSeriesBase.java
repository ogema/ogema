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

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.timeseries.v2.base.TimeSeriesBase;

/**
 * Base class for NavigableSet-based TimeSeries implementations
 */
abstract class TreeTimeSeriesBase extends ReadOnlyTreeTimeSeries implements TimeSeriesBase {

	private final Clock clock;
	private volatile long timeOfCalculation;
	
	protected TreeTimeSeriesBase(NavigableSet<SampledValue> set, InterpolationMode mode, Clock clock) {
		super(set, mode);
		this.clock = clock;
		this.timeOfCalculation = clock.millis();
	}
	
	@Override
	public Long getLastCalculationTime() {
		return timeOfCalculation;
	}

	@Override
	public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
		this.timeOfCalculation = timeOfCalculation;
		return this.values.addAll(values);
	}
	
	@Override
	public boolean addValues(Collection<SampledValue> values) {
		return addValues(values, clock.millis());
	}
	
	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		return replaceValuesFixedStep(startTime, values, stepSize, clock.millis());
	}

	@Override
	public boolean deleteValues(long startTime, long endTime) {
		subSet(startTime, endTime, endTime == Long.MAX_VALUE).clear();
		return true;
	}

	@Override
	public boolean setInterpolationMode(InterpolationMode mode) {
		this.mode = mode == null ? InterpolationMode.NONE : mode;
		return true;
	}
	
}
