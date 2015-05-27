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

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.recordeddata.ReductionMode;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class ArgumentTest {

	/**
	 * Test behaviour with wrong arguments
	 */
	@Test
	public void testArguments() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage rds = sdb.createRecordedDataStorage("testArguments", conf);

		rds.insertValue(new SampledValue(new DoubleValue(1.0), 0, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(2.0), 1, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(3.0), 2, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(4.0), 3, Quality.GOOD));
		rds.insertValue(new SampledValue(new DoubleValue(5.0), 4, Quality.GOOD));

		testArgumentsNegativeInterval(rds);
		testArgumentsEndBeforeStart(rds);
	}

	/**
	 * Negative interval is not allowed. getValues(...) should return an empty list in this case
	 */
	private void testArgumentsNegativeInterval(RecordedDataStorage rds) {
		for (ReductionMode mode : ReductionMode.values()) {
			List<SampledValue> recordedData = rds.getValues(0, 1, -1, mode);
			Assert.assertTrue(recordedData.isEmpty());
		}
	}

	/**
	 * End timestamp must be bigger than the start timestamp. If it is not then getValues(...) should return an empty
	 * list.
	 */
	private void testArgumentsEndBeforeStart(RecordedDataStorage rds) {
		for (ReductionMode mode : ReductionMode.values()) {
			List<SampledValue> recordedData = rds.getValues(2, 1, 1, mode);
			Assert.assertTrue(recordedData.isEmpty());
		}
	}

}
