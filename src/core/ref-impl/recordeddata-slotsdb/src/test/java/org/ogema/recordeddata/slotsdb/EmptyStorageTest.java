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
package org.ogema.recordeddata.slotsdb;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

/**
 * Performs tests on an empty storage 
 */
public class EmptyStorageTest extends DbTest {
	/**
	 * Creates a new storage which holds no data. Tests all ReductionModes. Result of the getValues(...) should be a
	 * empty list.
	 * @throws IOException 
	 */
	@Test
	public void testAllReductionModesWithEmptyDataStorage() throws DataRecorderException, IOException {

		// create an empty storage
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage emptyStorage = sdb.createRecordedDataStorage("emptyReduction", conf);

		// doesn't matter which values are used below since the storage is empty
		long start = 0;
		long end = 1;
		long interval = 1;

		for (ReductionMode mode : ReductionMode.values()) {
			List<SampledValue> recordedData = emptyStorage.getValues(start, end, interval, mode);
			Assert.assertTrue(recordedData.isEmpty());
		}
		Assert.assertTrue(emptyStorage.isEmpty());
	}

}
