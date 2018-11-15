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
package org.ogema.resourcemanager.impl.timeseries;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

/**
 *
 * @author jlapp
 */
public class EmptyRecordedData implements RecordedDataStorage {

	@Override
	public RecordedDataConfiguration getConfiguration() {
		return null;
	}

	@Override
	public void insertValue(SampledValue value) throws DataRecorderException {
		throw new UnsupportedOperationException("Read-only empty timeseries.");
	}

	@Override
	public void insertValues(List<SampledValue> values) throws DataRecorderException {
		throw new UnsupportedOperationException("Read-only empty timeseries.");
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
	public SampledValue getValue(long timestamp) {
		return null;
	}

	@Override
	public List<SampledValue> getValues(long startTime, long endTime, long interval, ReductionMode mode) {
		return Collections.emptyList();
	}

	@Override
	public void setConfiguration(RecordedDataConfiguration configuration) {
		throw new UnsupportedOperationException("Not supported.");
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
	public InterpolationMode getInterpolationMode() {
		return InterpolationMode.NONE;
	}

	@Override
	@Deprecated
	public Long getTimeOfLatestEntry() {
		return -1l;
	}

	@Override
	public void update(RecordedDataConfiguration configuration) throws DataRecorderException {
	}

	@Override
	public String getPath() {
		return null;
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

}
