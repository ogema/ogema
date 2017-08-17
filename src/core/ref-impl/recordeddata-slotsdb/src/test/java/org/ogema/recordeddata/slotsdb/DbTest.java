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

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class DbTest extends SlotsDbTest {

	protected static volatile SlotsDb sdb;
	
	@BeforeClass
	public static void setupDb() {
		deleteTestFiles();
		sdb = new SlotsDb(SlotsDb.DB_TEST_ROOT_FOLDER);
	}
	
	@AfterClass
	public static void closeDb() {
		if (sdb != null)
			sdb.deactivate(null);
		sdb = null;
		deleteTestFiles();
	}
	
	protected static Value[] createValues(float[] values) {
		Value[] v = new Value[values.length];
		for (int i=0;i<values.length;i++) {
			v[i] = new FloatValue(values[i]);
		}
		return v;
	}
	
	protected static void addValues(RecordedDataStorage rds, long[] timestamps, Value[] values) throws DataRecorderException {
		if (timestamps.length != values.length)
			throw new IllegalArgumentException("Number of timestamps must match number of values, got " + timestamps.length + " and " + values.length);
		List<SampledValue> svs = new ArrayList<>();
		long lastT = Long.MIN_VALUE;
		long t;
		for (int i=0;i<timestamps.length;i++) {
			t = timestamps[i];
			if (i > 0 && t <= lastT)
				throw new IllegalArgumentException("Timestamps not ordered chronologically");
			lastT = t;
			svs.add(new SampledValue(values[i],t, Quality.GOOD));
		}
		rds.insertValues(svs);
	}
	
}
