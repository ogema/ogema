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
public class MultiplePartlyIntervalsTest extends DbTest {

	private static final String STORAGE_NAME = "testMultiplePartlyIntervals";

	private static RecordedDataStorage rds;

	@BeforeClass
	public static void onlyOnce() throws DataRecorderException, IOException {

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rds_setup = sdb.createRecordedDataStorage(STORAGE_NAME, conf);

		// NOTE: setFixedInterval has to match witch timestamps
		rds_setup.insertValue(new SampledValue(DoubleValues.of(4.0), 4000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(5.0), 5000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(6.0), 6000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(7.0), 7000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(8.0), 8000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(9.0), 9000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(10.0), 10000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(11.0), 11000, Quality.GOOD));
		rds_setup.insertValue(new SampledValue(DoubleValues.of(12.0), 12000, Quality.GOOD));

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
