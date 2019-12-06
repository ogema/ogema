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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.TimeSeries;

class SynchronizedTimeseries implements TimeSeries {
	
	private final TimeSeries base;

	SynchronizedTimeseries(TimeSeries base) {
		this.base = Objects.requireNonNull(base);
	}
	
	@Override
	public SampledValue getValue(long time) {
		synchronized (this) {
			return base.getValue(time);
		}
	}

	@Override
	public SampledValue getNextValue(long time) {
		synchronized (this) {
			return base.getNextValue(time);
		}
	}

	@Override
	public SampledValue getPreviousValue(long time) {
		synchronized (this) {
			return base.getPreviousValue(time);
		}
	}

	@Override
	public List<SampledValue> getValues(long startTime) {
		synchronized (this) {
			return base.getValues(startTime);
		}
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime) {
		synchronized (this) {
			return base.getValues(startTime, endTime);
		}
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		synchronized (this) {
			return base.getInterpolationMode();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (this) {
			return base.isEmpty();
		}
	}

	@Override
	public boolean isEmpty(long startTime, long endTime) {
		synchronized (this) {
			return base.isEmpty(startTime, endTime);
		}
	}

	@Override
	public int size() {
		synchronized (this) {
			return base.size();
		}
	}

	@Override
	public int size(long startTime, long endTime) {
		synchronized (this) {
			return base.size(startTime, endTime);
		}
	}

	// must be manually synced
	@Override
	public Iterator<SampledValue> iterator() {
		return base.iterator();
	}
	
	// must be manually synced
	@Override
	public Iterator<SampledValue> iterator(long startTime, long endTime) {
		return base.iterator(startTime, endTime);
	}

	@Override
	public Long getTimeOfLatestEntry() {
		synchronized (this) {
			return base.getTimeOfLatestEntry();
		}
	}

	@Override
	public boolean addValue(long timestamp, Value value) {
		synchronized (this) {
			return base.addValue(timestamp, value);
		}
	}

	@Override
	public boolean addValues(Collection<SampledValue> values) {
		synchronized (this) {
			return base.addValues(values);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean addValueSchedule(long startTime, long stepSize, List<Value> values) {
		synchronized (this) {
			return base.addValueSchedule(startTime, stepSize, values);
		}
	}

	@Override
	public boolean addValue(long timestamp, Value value, long timeOfCalculation) {
		synchronized (this) {
			return base.addValue(timestamp, value, timeOfCalculation);
		}
	}

	@Override
	public boolean addValues(Collection<SampledValue> values, long timeOfCalculation) {
		synchronized (this) {
			return base.addValues(values, timeOfCalculation);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean addValueSchedule(long startTime, long stepSize, List<Value> values, long timeOfCalculation) {
		synchronized (this) {
			return base.addValueSchedule(startTime, stepSize, values, timeOfCalculation);
		}
	}

	@Override
	public Long getLastCalculationTime() {
		synchronized (this) {
			return base.getLastCalculationTime();
		}
	}

	@Override
	public boolean deleteValues() {
		synchronized (this) {
			return base.deleteValues();
		}
	}

	@Override
	public boolean deleteValues(long endTime) {
		synchronized (this) {
			return base.deleteValues(endTime);
		}
	}

	@Override
	public boolean deleteValues(long startTime, long endTime) {
		synchronized (this) {
			return base.deleteValues(startTime, endTime);
		}
	}

	@Override
	public boolean replaceValues(long startTime, long endTime, Collection<SampledValue> values) {
		synchronized (this) {
			return base.replaceValues(startTime, endTime, values);
		}
	}

	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize) {
		synchronized (this) {
			return base.replaceValuesFixedStep(startTime, values, stepSize);
		}
	}

	@Override
	public boolean replaceValuesFixedStep(long startTime, List<Value> values, long stepSize, long timeOfCalculation) {
		synchronized (this) {
			return base.replaceValuesFixedStep(startTime, values, stepSize, timeOfCalculation);
		}
	}

	@Override
	public boolean setInterpolationMode(InterpolationMode mode) {
		synchronized (this) {
			return base.setInterpolationMode(mode);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		synchronized (this) {
			return base.equals(obj);
		}
	}
	
	@Override
	public int hashCode() {
		synchronized (this) {
			return base.hashCode();
		}
	}

	@Override
	public String toString() {
		return "SynchronizedTimeSeries[" + base + "]";
	}
	
}
