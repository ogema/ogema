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
package org.ogema.recordeddata.slotsdb;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
public class EmptyStorageTest extends SlotsDbTest {

	@BeforeClass
	public static void setUp() {
		deleteTestFiles();
	}

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	/**
	 * Creates a new storage which holds no data. Tests all ReductionModes. Result of the getValues(...) should be a
	 * empty list.
	 */
	@Test
	public void testAllReductionModesWithEmptyDataStorage() throws DataRecorderException {

		// create an empty storage
		SlotsDb sdb = new SlotsDb();
		sdb.activate(null, null);
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
	}

}
