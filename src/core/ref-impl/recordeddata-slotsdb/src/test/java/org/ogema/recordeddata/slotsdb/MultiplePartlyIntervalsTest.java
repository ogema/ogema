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
 * Tests of this class only perform aggregation on more than one interval. from, to is covers more than one interval
 */
public class MultiplePartlyIntervalsTest extends SlotsDbTest {

	private static final String STORAGE_NAME = "testMultiplePartlyIntervals";

	private static RecordedDataStorage rds;

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	@BeforeClass
	public static void onlyOnce() throws DataRecorderException {

		deleteTestFiles();

		SlotsDb sdb = new SlotsDb();
		sdb.activate(null, null);
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rds_setup = sdb.createRecordedDataStorage(STORAGE_NAME, conf);

		// NOTE: setFixedInterval has to match witch timestamps
		rds_setup.insertValue(new SampledValue(new DoubleValue(4.0), 4000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(5.0), 5000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(6.0), 6000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(7.0), 7000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(8.0), 8000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(9.0), 9000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(10.0), 10000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(11.0), 11000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(new DoubleValue(12.0), 12000, Quality.GOOD));

		rds = sdb.getRecordedDataStorage(STORAGE_NAME);
	}

	// TODO update comment
	// /**
	// * Creates a test dataset with 5 values. Those values are read back by all available reduction modes in 3
	// intervals
	// * interval. Quality is GOOD, Interval is equidistant, Interval boundaries don't match with recorded data. the
	// first
	// * and last interval doesn't contain all values.
	// * <p>
	// * Recorded Data :. ---xxx xxxxxx xxx--- <br>
	// * Requested Data: |..I1..|..I2..|..I3..| <br>
	// * x = data; - = no data; Ix = Interval
	// */
	@Test
	public void testAllReductionModesOnMultiplePartlyIntervals() throws DataRecorderException {

		// testReductionModeMultiplePartlyIntervalNone(rds2);
		testReductionModeMultiplePartlyIntervalAverage_0(rds);
		testReductionModeMultiplePartlyIntervalAverage_1000(rds);
		testReductionModeMultiplePartlyIntervalAverage_2000(rds);
		testReductionModeMultiplePartlyIntervalAverage_3000(rds);
		// testReductionModeMultiplePartlyIntervalMin(rds2);
		// testReductionModeMultiplePartlyIntervalMax(rds2);
		// testReductionModeMultiplePartlyIntervalMinMax(rds2);
	}

	private void testReductionModeMultiplePartlyIntervalAverage_0(RecordedDataStorage rds) {
		List<SampledValue> recordedData = rds.getValues(0, 15001, 5000, ReductionMode.AVERAGE);

		Assert.assertTrue("Expected size = 4 but got " + recordedData.size(), recordedData.size() == 4);

		long expectedTime1 = 0;
		double expectedValue1 = 4;
		long expectedTime2 = 5000;
		double expectedValue2 = 7;
		long expectedTime3 = 10000;
		double expectedValue3 = 11;
		long expectedTime4 = 15000;
		Quality expectedQuality4 = Quality.BAD;

		long time1 = recordedData.get(0).getTimestamp();
		double value1 = recordedData.get(0).getValue().getDoubleValue();
		long time2 = recordedData.get(1).getTimestamp();
		double value2 = recordedData.get(1).getValue().getDoubleValue();
		long time3 = recordedData.get(2).getTimestamp();
		double value3 = recordedData.get(2).getValue().getDoubleValue();
		long time4 = recordedData.get(3).getTimestamp();
		Quality quality4 = recordedData.get(3).getQuality();

		Assert.assertTrue("Expeced timestamp " + expectedTime1 + " but got " + time1, expectedTime1 == time1);
		Assert.assertTrue("Expeced value " + expectedValue1 + " but got " + value1, expectedValue1 == value1);

		Assert.assertTrue("Expeced timestamp " + expectedTime2 + " but got " + time2, expectedTime2 == time2);
		Assert.assertTrue("Expeced value " + expectedValue2 + " but got " + value2, expectedValue2 == value2);

		Assert.assertTrue("Expeced timestamp " + expectedTime3 + " but got " + time3, expectedTime3 == time3);
		Assert.assertTrue("Expeced value " + expectedValue3 + " but got " + value3, expectedValue3 == value3);

		Assert.assertTrue("Expeced timestamp " + expectedTime4 + " but got " + time4, expectedTime4 == time4);
		Assert.assertTrue("Expeced value " + expectedQuality4 + " but got " + quality4, expectedQuality4 == quality4);
	}

	private void testReductionModeMultiplePartlyIntervalAverage_1000(RecordedDataStorage rds) {
		List<SampledValue> recordedData = rds.getValues(1000, 15001, 5000, ReductionMode.AVERAGE);

		Assert.assertTrue("Expected size = 3 but got " + recordedData.size(), recordedData.size() == 3);

		long expectedTime1 = 1000;
		double expectedValue1 = 4.5;
		long expectedTime2 = 6000;
		double expectedValue2 = 8;
		long expectedTime3 = 11000;
		double expectedValue3 = 11.5;

		long time1 = recordedData.get(0).getTimestamp();
		double value1 = recordedData.get(0).getValue().getDoubleValue();
		long time2 = recordedData.get(1).getTimestamp();
		double value2 = recordedData.get(1).getValue().getDoubleValue();
		long time3 = recordedData.get(2).getTimestamp();
		double value3 = recordedData.get(2).getValue().getDoubleValue();

		Assert.assertTrue("Expeced timestamp " + expectedTime1 + " but got " + time1, expectedTime1 == time1);
		Assert.assertTrue("Expeced value " + expectedValue1 + " but got " + value1, expectedValue1 == value1);

		Assert.assertTrue("Expeced timestamp " + expectedTime2 + " but got " + time2, expectedTime2 == time2);
		Assert.assertTrue("Expeced value " + expectedValue2 + " but got " + value2, expectedValue2 == value2);

		Assert.assertTrue("Expeced timestamp " + expectedTime3 + " but got " + time3, expectedTime3 == time3);
		Assert.assertTrue("Expeced value " + expectedValue3 + " but got " + value3, expectedValue3 == value3);
	}

	private void testReductionModeMultiplePartlyIntervalAverage_2000(RecordedDataStorage rds) {
		List<SampledValue> recordedData = rds.getValues(2000, 15001, 5000, ReductionMode.AVERAGE);

		Assert.assertTrue("Expected size = 3 but got " + recordedData.size(), recordedData.size() == 3);

		long expectedTime1 = 2000;
		double expectedValue1 = 5;
		long expectedTime2 = 7000;
		double expectedValue2 = 9;
		long expectedTime3 = 12000;
		double expectedValue3 = 12;

		long time1 = recordedData.get(0).getTimestamp();
		double value1 = recordedData.get(0).getValue().getDoubleValue();
		long time2 = recordedData.get(1).getTimestamp();
		double value2 = recordedData.get(1).getValue().getDoubleValue();
		long time3 = recordedData.get(2).getTimestamp();
		double value3 = recordedData.get(2).getValue().getDoubleValue();

		Assert.assertTrue("Expeced timestamp " + expectedTime1 + " but got " + time1, expectedTime1 == time1);
		Assert.assertTrue("Expeced value " + expectedValue1 + " but got " + value1, expectedValue1 == value1);

		Assert.assertTrue("Expeced timestamp " + expectedTime2 + " but got " + time2, expectedTime2 == time2);
		Assert.assertTrue("Expeced value " + expectedValue2 + " but got " + value2, expectedValue2 == value2);

		Assert.assertTrue("Expeced timestamp " + expectedTime3 + " but got " + time3, expectedTime3 == time3);
		Assert.assertTrue("Expeced value " + expectedValue3 + " but got " + value3, expectedValue3 == value3);
	}

	private void testReductionModeMultiplePartlyIntervalAverage_3000(RecordedDataStorage rds) {
		List<SampledValue> recordedData = rds.getValues(3000, 15001, 5000, ReductionMode.AVERAGE);

		Assert.assertTrue("Expected size = 3 but got " + recordedData.size(), recordedData.size() == 3);

		long expectedTime1 = 3000;
		double expectedValue1 = 5.5;
		long expectedTime2 = 8000;
		double expectedValue2 = 10;
		long expectedTime3 = 13000;
		Quality expectedQuality3 = Quality.BAD;

		long time1 = recordedData.get(0).getTimestamp();
		double value1 = recordedData.get(0).getValue().getDoubleValue();
		long time2 = recordedData.get(1).getTimestamp();
		double value2 = recordedData.get(1).getValue().getDoubleValue();
		long time3 = recordedData.get(2).getTimestamp();
		Quality quality3 = recordedData.get(2).getQuality();

		Assert.assertTrue("Expeced timestamp " + expectedTime1 + " but got " + time1, expectedTime1 == time1);
		Assert.assertTrue("Expeced value " + expectedValue1 + " but got " + value1, expectedValue1 == value1);

		Assert.assertTrue("Expeced timestamp " + expectedTime2 + " but got " + time2, expectedTime2 == time2);
		Assert.assertTrue("Expeced value " + expectedValue2 + " but got " + value2, expectedValue2 == value2);

		Assert.assertTrue("Expeced timestamp " + expectedTime3 + " but got " + time3, expectedTime3 == time3);
		Assert.assertTrue("Expeced value " + expectedQuality3 + " but got " + quality3, expectedQuality3 == quality3);
	}

}
