/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.resourcemanager.impl.timeseries;

import java.util.Collections;
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SampledValue getNextValue(long time) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public InterpolationMode getInterpolationMode() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Long getTimeOfLatestEntry() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void update(RecordedDataConfiguration configuration) throws DataRecorderException {
	}

}
