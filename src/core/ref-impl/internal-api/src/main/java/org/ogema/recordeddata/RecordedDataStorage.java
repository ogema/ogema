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
