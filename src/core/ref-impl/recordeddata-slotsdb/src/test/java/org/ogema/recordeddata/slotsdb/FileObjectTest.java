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

import java.io.IOException;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;

public class FileObjectTest extends SlotsDbTest {

	@BeforeClass
	public static void setUp() {
		deleteTestFiles();
	}

	@AfterClass
	public static void tearDown() {
		deleteTestFiles();
	}

	/*
	 * Verifies that the start time stamp of a new log file is always smaller than
	 * the time stamp of the first value written to the file.
	 */
	@Test
	public void createSampleLogFiles() throws IOException {
		startTimeStampTest(1446140458580L); // specific time stamp that used to fail...
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < 30; i++) {
			long nl = Math.abs(rand.nextLong());
			if (nl == 0)
				nl = 1;
			startTimeStampTest(nl);
		}
	}

	private void startTimeStampTest(long timestamp) throws IOException {
        @SuppressWarnings("deprecation")
		FileObjectProxy proxy = new FileObjectProxy(SlotsDb.DEFAULT_DB_ROOT_FOLDER);

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(5000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		try {
			System.out.println("  Trying to create new FileObject with start time stamp " + timestamp);
			proxy.appendValue("testId", 24.1, timestamp, (byte) 1, conf);
		} finally {
			proxy.flush();
		}
	}
	
	/*
	 * Verifies that there is no value overflow
	 */
	@Test
	public void checkInternalRoundingMethod() {
		RecordedDataConfiguration rdc = new RecordedDataConfiguration();
		rdc.setStorageType(StorageType.FIXED_INTERVAL);
		for (long i=0;i<50;i++) {
			rdc.setFixedInterval(3 + 17*i);
			long roundedTimestampMax = FileObjectProxy.getRoundedTimestamp(Long.MAX_VALUE, rdc);
			long roundedTimestampMin = FileObjectProxy.getRoundedTimestamp(Long.MIN_VALUE, rdc);
			assert (roundedTimestampMax > 0);
			assert (roundedTimestampMin < 0);
		}
	}

}
