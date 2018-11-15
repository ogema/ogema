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
import org.junit.Assert;
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
public class AppendValueTest extends DbTest {

	/**
	 * Test behaviour with wrong arguments -> nothing wrong here?
	 * @throws IOException 
	 */
	@Test
	public void testArguments() throws DataRecorderException, IOException {

		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		SlotsDbStorage rds = (SlotsDbStorage) sdb.createRecordedDataStorage("appendValue", conf);

		int size = rds.getValues(Long.MIN_VALUE).size();
		rds.insertValue(new SampledValue(DoubleValues.of(1.0), System.currentTimeMillis(), Quality.GOOD));

		size = rds.getValues(Long.MIN_VALUE).size() - size;
		Assert.assertEquals("Unexpected number of log data entries", 1, size);
	}

}
