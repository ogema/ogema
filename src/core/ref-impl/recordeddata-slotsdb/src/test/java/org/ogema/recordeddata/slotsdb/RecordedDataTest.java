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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.recordeddata.DataRecorderException;
import org.ogema.recordeddata.RecordedDataStorage;

public class RecordedDataTest {

	@BeforeClass
	public static void deleteSlotsDBData() {
		try {
			FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/data/slotsdb"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void writeReadTest() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(10000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);
		RecordedDataStorage sdbs = null;

		sdbs = sdb.createRecordedDataStorage("test", conf);

		sdbs.insertValue(new SampledValue(new FloatValue(12.3f), System.currentTimeMillis(), Quality.GOOD));

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("test");
		assertEquals(12.3f, sdbs2.getValues(0).get(0).getValue().getFloatValue(), 0);

		sdb.deleteRecordedDataStorage("test");

	}

	@Test
	public void intervallTest() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage sdbs = sdb.createRecordedDataStorage("test2", conf);

		sdbs.insertValue(new SampledValue(new FloatValue(1.0f), 10000, Quality.GOOD));
		// sdbs.insertValue(new SampledValue(new FloatValue(0.0f), 10250, Quality.GOOD)); // ignor
		sdbs.insertValue(new SampledValue(new FloatValue(2.0f), 12000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(3.0f), 13000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(0.0f), 13100, Quality.GOOD)); // ignor
		sdbs.insertValue(new SampledValue(new FloatValue(4.0f), 14000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(5.0f), 16000, Quality.GOOD));

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("test2");

		List<SampledValue> list = new LinkedList<SampledValue>();
		list = sdbs2.getValues(10000, 16001);
		assertEquals(5, list.size());
		int i = 1;
		for (SampledValue element : list) {
			assertEquals(1.0f * i, element.getValue().getFloatValue(), 0);
			i++;
		}
		if (sdbs2.getValue(10000) == null) {
			assertEquals(0, 1);
		}

		sdb.deleteRecordedDataStorage("test2");

	}

	@Test
	public void flexibleReadWriteTest() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.ON_VALUE_UPDATE);

		RecordedDataStorage sdbs = sdb.createRecordedDataStorage("flexibleTest1", conf);
		sdbs.insertValue(new SampledValue(new FloatValue(1.0f), 10000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(2.0f), 10250, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(3.0f), 10290, Quality.GOOD));

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("flexibleTest1");

		List<SampledValue> list = new LinkedList<SampledValue>();
		list = sdbs2.getValues(10000, 10251);
		assertEquals(2, list.size());
		int i = 1;
		for (SampledValue element : list) {
			assertEquals(1.0f * i, element.getValue().getFloatValue(), 0);
			i++;
		}

		if (sdbs2.getValue(10000) == null) {
			assertEquals(1, 0);
		}

		sdb.deleteRecordedDataStorage("test2");

	}

	// sometimes fails
	@Test
	public void writeReadBigData() throws DataRecorderException, InterruptedException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.ON_VALUE_UPDATE);

		RecordedDataStorage sdbs = sdb.createRecordedDataStorage("flexibleTest3", conf);

		int numberOfValues = 50000;
		for (int i = 1; i <= numberOfValues; i++) {
			sdbs.insertValue(new SampledValue(new FloatValue(i), i + 10000, Quality.GOOD));
		}

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("flexibleTest3");

		List<SampledValue> list = new LinkedList<SampledValue>();
		list = sdbs2.getValues(0, System.currentTimeMillis());

		assertEquals(numberOfValues, list.size());
		sdb.deleteRecordedDataStorage("flexibleTest3");

	}

	@Test
	public void writeReadBigDataConstant() throws DataRecorderException, InterruptedException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(100);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage sdbs = sdb.createRecordedDataStorage("flexibleTest4", conf);
		int numberOfValues = 50000;
		for (int i = 1; i <= numberOfValues; i++) {
			sdbs.insertValue(new SampledValue(new FloatValue(i), i * 100, Quality.GOOD));
		}

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("flexibleTest4");

		List<SampledValue> list = new LinkedList<SampledValue>();
		list = sdbs2.getValues(0, System.currentTimeMillis());

		assertEquals(numberOfValues, list.size());
		sdb.deleteRecordedDataStorage("flexibleTest4");

	}

	@Test
	public void nextValueTest() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.FIXED_INTERVAL);

		RecordedDataStorage sdbs = sdb.createRecordedDataStorage("test2", conf);

		sdbs.insertValue(new SampledValue(new FloatValue(1.0f), 10000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(0.0f), 10250, Quality.GOOD)); // ignor
		sdbs.insertValue(new SampledValue(new FloatValue(2.0f), 12000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(3.0f), 13000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(0.0f), 13100, Quality.GOOD)); // ignor
		sdbs.insertValue(new SampledValue(new FloatValue(4.0f), 14000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(5.0f), 16000, Quality.GOOD));

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("test2");

		assertEquals(2.0f, sdbs2.getNextValue(10450).getValue().getFloatValue(), 0f);

		sdb.deleteRecordedDataStorage("test2");

	}

	@Test
	public void nextValueTestFlexibleInterval() throws DataRecorderException {

		SlotsDb sdb = new SlotsDb();
		RecordedDataConfiguration conf = new RecordedDataConfiguration();
		conf.setFixedInterval(1000);
		conf.setStorageType(StorageType.ON_VALUE_CHANGED);

		RecordedDataStorage sdbs = sdb.createRecordedDataStorage("test9", conf);

		sdbs.insertValue(new SampledValue(new FloatValue(1.0f), 10000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(1.5f), 10250, Quality.GOOD)); // ignor
		sdbs.insertValue(new SampledValue(new FloatValue(2.0f), 12000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(3.0f), 13000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(0.0f), 13100, Quality.GOOD)); // ignor
		sdbs.insertValue(new SampledValue(new FloatValue(4.0f), 14000, Quality.GOOD));
		sdbs.insertValue(new SampledValue(new FloatValue(5.0f), 16000, Quality.GOOD));

		RecordedDataStorage sdbs2 = sdb.getRecordedDataStorage("test9");

		assertEquals(1.5f, sdbs2.getNextValue(10100).getValue().getFloatValue(), 0f);

		sdb.deleteRecordedDataStorage("test9");

	}
}
