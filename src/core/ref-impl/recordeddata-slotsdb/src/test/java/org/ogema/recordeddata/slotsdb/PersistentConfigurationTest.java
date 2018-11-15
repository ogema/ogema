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
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class PersistentConfigurationTest extends DbTest {

	private static final String TEST_ID_1 = "test_id_1";
	private static final String TEST_ID_2 = "test_id_2";

	private static final long INTERVAL_1 = 1000;

	private static final long INTERVAL_2 = 2000;
	private static final long INTERVAL_2_UPDATED = 5000;

	@BeforeClass
	public static void setup() throws DataRecorderException, IOException {

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
	 * @throws IOException 
	 */
	@Test
	public void testGetAllRecordedDataStorageIDs() throws DataRecorderException, IOException {

		List<String> ids = sdb.getAllRecordedDataStorageIDs();

		if (ids.size() != 2) {
			Assert.assertTrue(false);
		}

		Assert.assertTrue(ids.contains(TEST_ID_1));
		Assert.assertTrue(ids.contains(TEST_ID_2));
	}

	@Test
	public void testGetRecordedDataStorage() throws IOException {
		RecordedDataStorage rds = sdb.getRecordedDataStorage(TEST_ID_1);
		RecordedDataConfiguration rdc = rds.getConfiguration();
		Assert.assertEquals(rdc.getFixedInterval(), INTERVAL_1);
	}

	@Test
	public void testUpdateConfiguration() throws DataRecorderException, IOException, InterruptedException {
		// update config
		RecordedDataStorage rds = sdb.getRecordedDataStorage(TEST_ID_2);
		RecordedDataConfiguration rdc = rds.getConfiguration();
		rdc.setFixedInterval(INTERVAL_2_UPDATED);
		rds.update(rdc);

		//read back config
		SlotsDb sdb2 = new SlotsDb(SlotsDb.DB_TEST_ROOT_FOLDER);
		RecordedDataStorage rds2 = sdb2.getRecordedDataStorage(TEST_ID_2);
		RecordedDataConfiguration rdc2 = rds2.getConfiguration(); // NullPointer; maybe not flushed yet?
		Assert.assertEquals(rdc2.getFixedInterval(), INTERVAL_2_UPDATED);
	}

}
