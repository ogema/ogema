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
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class GetValuesArgumentTest extends SlotsDbTest {

	private static RecordedDataStorage rds;

	@BeforeClass
	public static void setUp() {
		deleteTestFiles();
		generateTestData();
	}

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	/**
	 * Generates demo data
	 */
	private static void generateTestData() {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		try {
			rds = sdb.createRecordedDataStorage("testArguments", conf);
			rds.insertValue(new SampledValue(new DoubleValue(1.0), 0, Quality.GOOD));
			rds.insertValue(new SampledValue(new DoubleValue(2.0), 1, Quality.GOOD));
			rds.insertValue(new SampledValue(new DoubleValue(3.0), 2, Quality.GOOD));
			rds.insertValue(new SampledValue(new DoubleValue(4.0), 3, Quality.GOOD));
			rds.insertValue(new SampledValue(new DoubleValue(5.0), 4, Quality.GOOD));
		} catch (DataRecorderException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Negative interval is not allowed. getValues(...) should return an empty list in this case
	 */
	@Test
	public void testArgumentsNegativeInterval() {
		for (ReductionMode mode : ReductionMode.values()) {
			List<SampledValue> recordedData = rds.getValues(0, 1, -1, mode);
			Assert.assertTrue(recordedData.isEmpty());
		}
	}

	/**
	 * End timestamp must be bigger than the start timestamp. If it is not then getValues(...) should return an empty
	 * list.
	 */
	@Test
	public void testArgumentsEndBeforeStart() {
		for (ReductionMode mode : ReductionMode.values()) {
			List<SampledValue> recordedData = rds.getValues(2, 1, 1, mode);
			Assert.assertTrue(recordedData.isEmpty());
		}
	}

}
