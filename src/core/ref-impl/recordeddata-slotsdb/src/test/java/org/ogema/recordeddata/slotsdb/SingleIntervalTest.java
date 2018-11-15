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
 * Tests of this class only perform aggregation on a single interval e.g. from, to matches with the interval size of an interval
 */
public class SingleIntervalTest extends DbTest {

	private static volatile RecordedDataStorage rds;

	@BeforeClass
	public static void setUp() throws IOException {
		generateTestData();
	}

	/**
	 * Generates demo data
	 * @throws IOException 
	 */
	private static void generateTestData() throws IOException {

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rdsTemp;

		try {
			rdsTemp = sdb.createRecordedDataStorage("testSingleInterval", conf);
			rdsTemp.insertValue(new SampledValue(DoubleValues.of(1.0), 1000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(DoubleValues.of(2.0), 2000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(DoubleValues.of(3.0), 3000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(DoubleValues.of(4.0), 4000, Quality.GOOD));
			rdsTemp.insertValue(new SampledValue(DoubleValues.of(5.0), 5000, Quality.GOOD));

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
