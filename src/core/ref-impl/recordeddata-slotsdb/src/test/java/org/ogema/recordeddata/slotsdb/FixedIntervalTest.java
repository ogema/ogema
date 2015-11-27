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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class FixedIntervalTest extends SlotsDbTest {

	@BeforeClass
	public static void setUp() {
		deleteTestFiles();
	}

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	@Test
	/**
	 * Writes a single value to a slotsdb and reads it back
	 *  
	 * @throws DataRecorderException
	 */
	public void writeReadTestTimeRange() throws DataRecorderException {

		final String storageName = "writeReadTest1";

		SlotsDb sdb = new SlotsDb();
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
	public void writeReadTestSingleTimestamp() throws DataRecorderException {

		final String storageName = "writeReadTest2";

		SlotsDb sdb = new SlotsDb();
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
	public void writeReadTimestampNotAvailable() throws DataRecorderException {

		final String storageName = "writeReadTest3";

		SlotsDb sdb = new SlotsDb();
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
	public void writeRead10msTest() throws DataRecorderException {

		final String storageName = "writeReadTest4";

		SlotsDb sdb = new SlotsDb();
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
