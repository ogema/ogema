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

import org.ogema.core.recordeddata.RecordedDataConfiguration;

/**
 * This interface provides access to the recorded data storage repository (time series database). It is provided as a
 * service by the RecordedData database implementation. It provides facilities to create, delete, access and update the
 * configuration of recorded data storages.
 */
public interface DataRecorder {

	/**
	 * Create a new time series storage
	 * 
	 * @param id
	 * @param configuration
	 *            a new configuration object containing the configuration data for the new time series
	 * @throws DataRecorderException
	 *             if the creation fails (e.g. when a time series with the provided recordedDataID already exists).
	 */
	RecordedDataStorage createRecordedDataStorage(String id, RecordedDataConfiguration configuration)
			throws DataRecorderException;

	/**
	 * @param id
	 * @return
	 *    null, if not found
	 */
	RecordedDataStorage getRecordedDataStorage(String id);

	/**
	 * Delete a time series storage
	 * 
	 * This will permanently remove the stored time series data and the associated configuration.
	 * 
	 * @param id
	 *            unique ID of the time series
	 */
	boolean deleteRecordedDataStorage(String id);

	List<String> getAllRecordedDataStorageIDs();
}
