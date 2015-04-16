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
package org.ogema.core.recordeddata;

/**
 * Defines the configuration for one RecordedData database
 * 
 */
public class RecordedDataConfiguration {
	/**
	 * FIXED_INTERVAL - all values to be written are matched to the fixed time interval. In one interval there is only
	 * one value possible. Missing values will be filled with ??? data. Read-out values have fixed timestamp interval
	 * given by the user.<br>
	 * FLEXIBLE - There is no fixed time interval. Each written value will be stored with its original timestamp.
	 */
	public enum StorageType {
		/** write the value to the database every time the value is updated */
		ON_VALUE_UPDATE,

		/** write new values into database only if the value differs from the last stored value */
		ON_VALUE_CHANGED,

		/** write values with a fixed update rate */
		FIXED_INTERVAL
	}

	private StorageType storageType;

	/**
	 * Time in ms! The Interval between the TimeStamps of the Values. If an App will log every 100ms a Value but the
	 * updateRate is set on 200ms, than will only record every second value!
	 */
	private long fixedInterval;

	/**
	 * Getter method for the storage type to be used
	 * 
	 * @return returns the storage type for this RecordedData set: FLEXIBLE or FIXED_INTERVAL
	 */
	public StorageType getStorageType() {
		return storageType;
	}

	/**
	 * Setter method for the storage type to be used
	 * 
	 * @param storageType
	 *            set the storage type for this RecordedData set
	 */
	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
	}

	/**
	 * Getter for the fixed time interval for storage type FIXED_INTERVAL
	 * 
	 * @return fixed time interval for storage type FIXED_INTERVAL
	 */
	public long getFixedInterval() {
		return fixedInterval;
	}

	/**
	 * Getter for the fixed time interval for storage type FIXED_INTERVAL
	 * 
	 * @param fixedInterval
	 *            fixed time interval for storage type FIXED_INTERVAL
	 */
	public void setFixedInterval(long fixedInterval) {
		this.fixedInterval = fixedInterval;
	}
}
