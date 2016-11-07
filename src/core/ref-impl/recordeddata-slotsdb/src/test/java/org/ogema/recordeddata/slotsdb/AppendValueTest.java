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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;

/**
 * 
 */
public class AppendValueTest extends SlotsDbTest {

	@BeforeClass
	public static void setUp() {
		deleteTestFiles();
	}

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	/**
	 * Test behaviour with wrong arguments
	 */
	@Test
	public void testArguments() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		sdb.activate(null, null);
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		SlotsDbStorage rds = (SlotsDbStorage) sdb.createRecordedDataStorage("appendValue", conf);

		rds.insertValue(new SampledValue(new DoubleValue(1.0), System.currentTimeMillis(), Quality.GOOD));

		Assert.assertTrue(true);

	}

}
