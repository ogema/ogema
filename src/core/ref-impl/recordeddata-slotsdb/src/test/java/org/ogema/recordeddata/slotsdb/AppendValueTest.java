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
		rds.insertValue(new SampledValue(new DoubleValue(1.0), System.currentTimeMillis(), Quality.GOOD));

		size = rds.getValues(Long.MIN_VALUE).size() - size;
		Assert.assertEquals("Unexpected number of log data entries", 1, size);
	}

}
