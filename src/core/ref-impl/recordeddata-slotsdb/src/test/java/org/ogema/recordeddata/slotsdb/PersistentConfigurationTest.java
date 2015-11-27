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
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class PersistentConfigurationTest extends SlotsDbTest {

	private static final String TEST_ID_1 = "test_id_1";
	private static final String TEST_ID_2 = "test_id_2";

	private static final long INTERVAL_1 = 1000;

	private static final long INTERVAL_2 = 2000;
	private static final long INTERVAL_2_UPDATED = 5000;

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	@BeforeClass
	public static void setup() throws DataRecorderException {

		deleteTestFiles();

		SlotsDb sdb = new SlotsDb();

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(INTERVAL_1);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rds1 = sdb.createRecordedDataStorage(TEST_ID_1, conf);

		RecordedDataConfiguration conf2 = new RecordedDataConfiguration();
		conf2.setFixedInterval(INTERVAL_2);
		conf2.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rds2 = sdb.createRecordedDataStorage(TEST_ID_2, conf2);
	}

	/**
	 * Test behaviour with wrong arguments
	 */
	@Test
	public void testGetAllRecordedDataStorageIDs() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		List<String> ids = sdb.getAllRecordedDataStorageIDs();

		if (ids.size() != 2) {
			Assert.assertTrue(false);
		}

		Assert.assertTrue(ids.contains(TEST_ID_1));
		Assert.assertTrue(ids.contains(TEST_ID_2));
	}

	@Test
	public void testGetRecordedDataStorage() {
		SlotsDb sdb = new SlotsDb();
		RecordedDataStorage rds = sdb.getRecordedDataStorage(TEST_ID_1);
		RecordedDataConfiguration rdc = rds.getConfiguration();
		Assert.assertEquals(rdc.getFixedInterval(), INTERVAL_1);
	}

	@Test
	public void testUpdateConfiguration() throws DataRecorderException {
		// update config
		SlotsDb sdb = new SlotsDb();
		RecordedDataStorage rds = sdb.getRecordedDataStorage(TEST_ID_2);
		RecordedDataConfiguration rdc = rds.getConfiguration();
		rdc.setFixedInterval(INTERVAL_2_UPDATED);
		rds.update(rdc);
		sdb = null;

		//read back config
		SlotsDb sdb2 = new SlotsDb();
		RecordedDataStorage rds2 = sdb2.getRecordedDataStorage(TEST_ID_2);
		RecordedDataConfiguration rdc2 = rds2.getConfiguration();
		Assert.assertEquals(rdc2.getFixedInterval(), INTERVAL_2_UPDATED);
	}

}
