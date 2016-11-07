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

/**
 * Interval: All data of a interval are aggregated to a new value
 * Tests of this class only perform aggregation on a single interval e.g. from, to matches with the interval size of an interval
 */
public class SingleIntervalTest extends SlotsDbTest {

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
		sdb.activate(null, null);
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rdsTemp;

		try {
			rdsTemp = sdb.createRecordedDataStorage("testSingleInterval", conf);
			rdsTemp.insertValue(new SampledValue(new DoubleValue(1.0), 1000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(new DoubleValue(2.0), 2000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(new DoubleValue(3.0), 3000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(new DoubleValue(4.0), 4000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(new DoubleValue(5.0), 5000, Quality.GOOD));

			//Read back
			rds = sdb.getRecordedDataStorage("testSingleInterval");

		} catch (DataRecorderException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReductionModeAverageOnSingleInterval() {
		List<SampledValue> recordedData = rds.getValues(1000, 5001, 5000, ReductionMode.AVERAGE);

		Assert.assertTrue("Expected size = 1 but got " + recordedData.size(), recordedData.size() == 1);

		long expectedTimestamp = 1000;
		double expectedValue = 3.0;

		long timestamp = recordedData.get(0).getTimestamp();
		double value = recordedData.get(0).getValue().getDoubleValue();

		Assert.assertTrue("Expeced timestamp " + expectedTimestamp + " but got " + timestamp,
				timestamp == expectedTimestamp);
		Assert.assertTrue("Expeced value " + expectedValue + " but got " + value, value == expectedValue);
	}

	@Test
	public void testReductionModeMinOnSingleInterval() {
		List<SampledValue> recordedData = rds.getValues(1000, 5001, 5000, ReductionMode.MINIMUM_VALUE);

		Assert.assertTrue("Expected size = 1 but got " + recordedData.size(), recordedData.size() == 1);

		long expectedTimestamp = 1000;
		double expectedValue = 1.0;

		long timestamp = recordedData.get(0).getTimestamp();
		double value = recordedData.get(0).getValue().getDoubleValue();

		Assert.assertTrue("Expeced timestamp " + expectedTimestamp + " but got " + timestamp,
				timestamp == expectedTimestamp);
		Assert.assertTrue("Expeced value " + expectedValue + " but got " + value, value == expectedValue);
	}

	@Test
	public void testReductionModeMaxOnSingleInterval() {
		List<SampledValue> recordedData = rds.getValues(1000, 5001, 5000, ReductionMode.MAXIMUM_VALUE);

		Assert.assertTrue("Expected size = 1 but got " + recordedData.size(), recordedData.size() == 1);

		long expectedTimestamp = 1000;
		double expectedValue = 5.0;

		long timestamp = recordedData.get(0).getTimestamp();
		double value = recordedData.get(0).getValue().getDoubleValue();

		Assert.assertTrue("Expeced timestamp " + expectedTimestamp + " but got " + timestamp,
				timestamp == expectedTimestamp);
		Assert.assertTrue("Expeced value " + expectedValue + " but got " + value, value == expectedValue);
	}

	@Test
	public void testReductionModeMinMaxOnSingleInterval() {
		List<SampledValue> recordedData = rds.getValues(1000, 5001, 5000, ReductionMode.MIN_MAX_VALUE);

		Assert.assertTrue("Expected size = 2 but got " + recordedData.size(), recordedData.size() == 2);

		long expectedTimestamp = 1000;
		double expectedMinValue = 1.0;
		double expectedMaxValue = 5.0;

		long timestamp = recordedData.get(0).getTimestamp();
		double minValue = recordedData.get(0).getValue().getDoubleValue();
		double maxValue = recordedData.get(1).getValue().getDoubleValue();

		Assert.assertTrue("Expeced timestamp " + expectedTimestamp + " but got " + timestamp,
				timestamp == expectedTimestamp);
		Assert.assertTrue("Expeced value " + expectedMinValue + " but got " + minValue, minValue == expectedMinValue);
		Assert.assertTrue("Expeced value " + expectedMaxValue + " but got " + maxValue, maxValue == expectedMaxValue);
	}

	@Test
	public void testReductionModeNoneOnSingleInterval() {

		List<SampledValue> recordedData = rds.getValues(1000, 5001);
		Assert.assertTrue("Expected size = 5 but got " + recordedData.size(), recordedData.size() == 5);

		// read back the values
		for (int i = 0; i < 5; i++) {
			Assert.assertTrue(recordedData.get(i).getTimestamp() == ((i + 1) * 1000));
			Assert.assertTrue(recordedData.get(i).getValue().getDoubleValue() == i + 1);
		}
	}

}
