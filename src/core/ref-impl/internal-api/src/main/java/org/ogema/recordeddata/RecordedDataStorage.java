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
package org.ogema.recordeddata;

import java.util.List;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;

public interface RecordedDataStorage extends RecordedData {

	/**
	 * Insert a value into the database.
	 * 
	 * @param value
	 *            new value to be inserted
	 */
	public void insertValue(SampledValue value) throws DataRecorderException;

	/**
	 * Insert multiple values into the database.
	 * 
	 * @param values
	 *            list of new values to be inserted
	 */
	public void insertValues(List<SampledValue> values) throws DataRecorderException;

	//public Map<String, RecordedDataConfiguration> getPersistenConfigurationMap();

	/**
	 * Update the configuration of the time series storage.
	 * 
	 * The new configuration will come into effect immediately.
	 * 
	 * @param configuration
	 *            a new configuration object.
	 * @throws DataRecorderException
	 *             if the time series does not exist or the provided configuration is invalid
	 */
	void update(RecordedDataConfiguration configuration) throws DataRecorderException;

}
