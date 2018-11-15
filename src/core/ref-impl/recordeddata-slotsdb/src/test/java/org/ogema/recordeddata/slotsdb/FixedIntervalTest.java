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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class FixedIntervalTest extends DbTest {

	@Test
	/**
	 * Writes a single value to a slotsdb and reads it back
	 *  
	 * @throws DataRecorderException
	 */
	public void writeReadTestTimeRange() throws DataRecorderException, IOException {

		final String storageName = "writeReadTest1";

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage rds = sdb.createRecordedDataStorage(storageName, conf);

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		long firstTimestamp = 1439883170501L;
		System.out.println("firstTimestamp: " + firstTimestamp + "  " + sdf.format(new Date(firstTimestamp)));
		rds.insertValue(new SampledValue(new FloatValue(12.3f), firstTimestamp, Quality.GOOD));

		RecordedDataStorage rdsReadback = sdb.getRecordedDataStorage(storageName);

		List<SampledValue> values = rdsReadback.getValues(firstTimestamp - 20000);
		System.out.println("Values stored: " + values.size());

		if (values.size() > 0) {
			for (SampledValue v : values) {
				System.out.println("timestamp: " + v.getTimestamp() + "  " + sdf.format(new Date(v.getTimestamp()))
						+ " value: " + v.getValue().getFloatValue());
			}
		}
		else {
			//
		}

		SampledValue sv = rdsReadback.getValue(firstTimestamp);

		if (sv != null) {
			Value v = sv.getValue();
			assertEquals(12.3f, v.getFloatValue(), 0);
		}
		else {
			assertTrue("no value available for timestamp " + firstTimestamp, false);
		}
		sdb.deleteRecordedDataStorage(storageName);

	}

	@Test
	/**
	 * Writes a single value to a slotsdb and reads it back
	 *  
	 * @throws DataRecorderException
	 */
	public void writeReadTestSingleTimestamp() throws DataRecorderException, IOException {

		final String storageName = "writeReadTest2";

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage rds = sdb.createRecordedDataStorage(storageName, conf);

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		long firstTimestamp = 1439883170501L;
		System.out.println("firstTimestamp: " + firstTimestamp + "  " + sdf.format(new Date(firstTimestamp)));
		rds.insertValue(new SampledValue(new FloatValue(12.3f), firstTimestamp, Quality.GOOD));

		RecordedDataStorage rdsReadback = sdb.getRecordedDataStorage(storageName);
		SampledValue sv = rdsReadback.getValue(firstTimestamp);

		if (sv != null) {
			Value v = sv.getValue();
			assertEquals(12.3f, v.getFloatValue(), 0);
		}
		else {
			assertTrue("no value available for timestamp " + firstTimestamp, false);
		}

		sdb.deleteRecordedDataStorage(storageName);

	}

	@Test
	/**
	 * Writes a single value which creates the first timestamp of the storage. 
	 * Afterwards the test tries to read a value before that timestamp which 
	 * should result in an error (null)
	 *  
	 * @throws DataRecorderException
	 */
	public void writeReadTimestampNotAvailable() throws DataRecorderException, IOException {

		final String storageName = "writeReadTest3";

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage rds = sdb.createRecordedDataStorage(storageName, conf);

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		long firstTimestamp = 1439883170501L;
		System.out.println("firstTimestamp: " + firstTimestamp + "  " + sdf.format(new Date(firstTimestamp)));
		rds.insertValue(new SampledValue(new FloatValue(12.3f), firstTimestamp, Quality.GOOD));

		RecordedDataStorage rdsReadback = sdb.getRecordedDataStorage(storageName);

		long outsideTimestamp = firstTimestamp - 10000; // timestamp 10 s before the first timestamp
		SampledValue value = rdsReadback.getValue(outsideTimestamp);

		if (value == null) {
			assertTrue("null expected", true);
		}
		else {
			assertTrue("null expected, but it's not", false);
		}

		sdb.deleteRecordedDataStorage(storageName);

	}

	@Test
	/**
	 * Try to write and read in 10 ms interval
	 *  
	 * @throws DataRecorderException
	 */
	public void writeRead10msTest() throws DataRecorderException, IOException {

		final String storageName = "writeReadTest4";

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(10);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage rds = sdb.createRecordedDataStorage(storageName, conf);

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		long firstTimestamp = 1439883170501L;
		long secondTimestamp = 1439883170511L;
		long thirdTimestamp = 1439883170521L;

		rds.insertValue(new SampledValue(new FloatValue(12.0f), firstTimestamp, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(12.1f), secondTimestamp, Quality.GOOD));
		rds.insertValue(new SampledValue(new FloatValue(12.2f), thirdTimestamp, Quality.GOOD));

		RecordedDataStorage rdsReadback = sdb.getRecordedDataStorage(storageName);

		List<SampledValue> values = rdsReadback.getValues(firstTimestamp, thirdTimestamp);
		System.out.println("Values stored: " + values.size());

		if (values.size() == 3) {
			for (SampledValue v : values) {
				System.out.println("timestamp: " + v.getTimestamp() + "  " + sdf.format(new Date(v.getTimestamp()))
						+ " value: " + v.getValue().getFloatValue());
			}
			assertTrue("expected 3 values", true);
		}
		else {
			assertTrue("expected 3 values, but got " + values.size(), false);
		}

		sdb.deleteRecordedDataStorage(storageName);

	}

}
