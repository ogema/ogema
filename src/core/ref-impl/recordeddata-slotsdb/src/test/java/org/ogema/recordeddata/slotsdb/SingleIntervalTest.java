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

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class SingleIntervalTest {

	/**
	 * Creates a test dataset with 5 values. Those values are read back by all available reduction modes in a single
	 * interval. Quality is GOOD, Interval is equidistant, Interval fits with recorded data - no rest.
	 */
	@Test
	public void testAllReductionModesOnSingleInterval() throws DataRecorderException {
		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rds = sdb.createRecordedDataStorage("testSingleInterval", conf);

		// NOTE: setFixedInterval has to match witch timestamps
		rds.insertValue(new SampledValue(new DoubleValue(1.0), 1000, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(2.0), 2000, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(3.0), 3000, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(4.0), 4000, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(5.0), 5000, Quality.GOOD));

		RecordedDataStorage rds2 = sdb.getRecordedDataStorage("testSingleInterval");
		testReductionModeNoneOnSingleInterval(rds2);
		testReductionModeAverageOnSingleInterval(rds2);
		testReductionModeMinOnSingleInterval(rds2);
		testReductionModeMaxOnSingleInterval(rds2);
		testReductionModeMinMaxOnSingleInterval(rds2);
	}

	private void testReductionModeAverageOnSingleInterval(RecordedDataStorage rds) {
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

	private void testReductionModeMinOnSingleInterval(RecordedDataStorage rds) {
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

	private void testReductionModeMaxOnSingleInterval(RecordedDataStorage rds) {
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

	private void testReductionModeMinMaxOnSingleInterval(RecordedDataStorage rds) {
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

	private void testReductionModeNoneOnSingleInterval(RecordedDataStorage rds) {
		List<SampledValue> recordedData = rds.getValues(1000, 5001);
		Assert.assertTrue("Expected size = 5 but got " + recordedData.size(), recordedData.size() == 5);

		// read back the values
		for (int i = 0; i < 5; i++) {
			// System.out.println("time : " + recordedData.get(i).getTimestamp());
			// System.out.println("value: " + recordedData.get(i).getValue().getDoubleValue());
			Assert.assertTrue(recordedData.get(i).getTimestamp() == ((i + 1) * 1000));
			Assert.assertTrue(recordedData.get(i).getValue().getDoubleValue() == i + 1);
		}
	}

}
