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

	RecordedDataStorage getRecordedDataStorage(String id);

	/**
	 * Delete a time series storage
	 * 
	 * This will permanently remove the stored time series data and the associated configuration.
	 * 
	 * @param recordedDataID
	 *            unique ID of the time series
	 * @throws DataRecorderException
	 *             if the time series does not exist
	 */
	boolean deleteRecordedDataStorage(String id);

	List<String> getAllRecordedDataStorageIDs();
}
