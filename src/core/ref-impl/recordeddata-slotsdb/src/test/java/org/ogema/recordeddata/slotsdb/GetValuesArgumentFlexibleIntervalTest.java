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

public class GetValuesArgumentFlexibleIntervalTest extends DbTest {

	private static RecordedDataStorage rds;

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
		conf.setStorageType(StorageType.ON_VALUE_UPDATE);

		try {
			rds = sdb.createRecordedDataStorage("testArguments", conf);
			rds.insertValue(new SampledValue(DoubleValues.of(1.0), 0, Quality.GOOD));
			rds.insertValue(new SampledValue(DoubleValues.of(2.0), 1, Quality.GOOD));
			rds.insertValue(new SampledValue(DoubleValues.of(3.0), 2, Quality.GOOD));
			rds.insertValue(new SampledValue(DoubleValues.of(5.0), 4, Quality.GOOD));
			rds.insertValue(new SampledValue(DoubleValues.of(7.0), System.currentTimeMillis(), Quality.GOOD));
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
	
	@Test
	public void valuesAvailable() {
		assert(rds.getValues(-1).size()>0) : "log data not found";
		assert(rds.getNextValue(-7) != null) : "log data not found";
		assert(rds.getPreviousValue(100000) != null) : "log data not found";
	}
	
	/*
	 * This test is important, because the framework methods often pass
	 * Long.MIN_VALUE or Long.MAX_VALUE to recorded data
	 */
	@Test
	public void testLargeRequestArguments() {
		List<SampledValue> values = rds.getValues(Long.MIN_VALUE);
		assert (values.size() > 0) : "log data not found";
		values = rds.getValues(Long.MIN_VALUE,5);
		assert (values.size() > 0) : "log data not found";
		values = rds.getValues(Long.MIN_VALUE, Long.MAX_VALUE);
		assert (values.size() > 0) : "log data not found";
		values = rds.getValues(-1, Long.MAX_VALUE);
		assert (values.size() > 0) : "log data not found";
		SampledValue sv = rds.getNextValue(Long.MIN_VALUE);
		assert (sv != null) : "log data not found";
		sv = rds.getPreviousValue(Long.MAX_VALUE);
		assert (sv != null) : "log data not found";
		assert !rds.isEmpty() : "log data erroneously reported as empty";
		assert !rds.isEmpty(Long.MIN_VALUE, 5) : "log data erroneously reported as empty";
	}

}
