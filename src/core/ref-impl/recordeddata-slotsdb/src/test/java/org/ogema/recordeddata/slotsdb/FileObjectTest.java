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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.junit.Test;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;

public class FileObjectTest extends SlotsDbTest {

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
		Path path = Paths.get(SlotsDb.DB_TEST_ROOT_FOLDER);
		Files.createDirectories(path);
		FileObjectProxy proxy = new FileObjectProxy(path.toString());
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
